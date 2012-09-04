/***************************************************************
Copyright (C) 2012
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.
***************************************************************/

package org.n52.wps.server.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * This class handles input playlists by fetching, reading, and, if necessary, 
 * 	reloading them. It also fetches the chunks listed in the playlist and 
 * 	notifies observers as soon as chunks are available to be processed. 
 * 
 * @author German Carrillo
 *
 */
public class PlaylistInputHandler implements ISubject {

	private static Logger LOGGER = Logger.getLogger(PlaylistInputHandler.class);
	
	private String playlistURL;
	private boolean playlistFinished = false;
	private int loadedChunks = 0;
	private Timer timer = new Timer(true);
	private boolean timerIsActive = false;
	private long lastPlaylistResponseTime;
	private boolean bFirstRequest = true;
	private AsyncHttpClient client;	
	private int totalRequestsMade = 0; 
	private int totalRequestsCompleted = 0;
	
	private int period; // Check the playlist every ... seconds
	private int maxTimeIdle; // Max. time of not receiving new chunks
	private IParser chunkParser;
	private String schema;
	private String mimeType;
	private String encoding;
	final public String endTag = "#PLAYLIST-END"; 
	private List<IObserver> observers = new ArrayList<IObserver>();
	
	/**
	 * Creates a PlaylistInputHandler
	 * 
	 */
	public PlaylistInputHandler(IParser chunkParser, String mimeType, String schema, String encoding) {
		this.chunkParser = chunkParser;
		this.mimeType = mimeType;
		this.schema = schema;
		this.encoding = encoding;
	}
	
	/**
	 * Starts reading the playlist
	 * 
	 * @param playlistURL
	 * 						URL of the playlist to be read
	 * @param period
	 * 						Period in milliseconds to reload the playlist
	 * @param maxTimeIdle
	 * 						Max time in milliseconds not receiving new data 
	 */
	public void start(String playlistURL, int period, int maxTimeIdle) {
		
		this.period = period;
		this.maxTimeIdle = maxTimeIdle;
		this.playlistURL = playlistURL;
		
		/* Configure the asynchronous http client */
		AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
		builder.setAllowPoolingConnection(true)
			//.setExecutorService(Executors.newFixedThreadPool(20)) // Bounded pool if preferred
			.setIdleConnectionInPoolTimeoutInMs(maxTimeIdle)			
			.setIdleConnectionTimeoutInMs(maxTimeIdle)
			.setFollowRedirects(true);
		client = new AsyncHttpClient(builder.build());
		
		readPlaylist();
	}
	
	/**
	 * Ensures timer and client are stopped properly 
	 */
	public void stop() {
		
		if (timerIsActive) {
			timerIsActive = false;
			timer.cancel();
		}
		
		// Check if all requests are complete before closing the client
		if (totalRequestsCompleted != totalRequestsMade) {
			LOGGER.warn((totalRequestsMade - totalRequestsCompleted) + "/" + 
					totalRequestsMade + " requests are not completed. The client was not closed!");
			return;
		}
		
		/* If the client is closed, a "Callback" thread is left, 
		    if not, a "Reaper" thread is left, how to terminate all threads? */
		//client.closeAsynchronously(); // Close pending HTTP connections
		//LOGGER.info("Async Http Client closed!");
	}
	
	/**
	 * Fetches the playlist asynchronously
	 */
	private void readPlaylist() {
		try {			
			/* Create the asynchronous request */
			totalRequestsMade += 1;
		    final ListenableFuture<Response> f =
		        client.prepareGet(playlistURL).execute(new AsyncCompletionHandler<Response>() {

		            @Override
		            public Response onCompleted(Response response) throws Exception {
		            	LOGGER.info("Playlist response Complete! "); //+ response.getResponseBody().length());
		                return response;
		            }

		            @Override
		            public void onThrowable(Throwable t) {
		                super.onThrowable(t);
		                LOGGER.error("Error executing request: " + t);
		                throw new RuntimeException(
		                		"There went something wrong with the network connection while fetching the playlist.", t);
		            }
		        });
		    
		    /* Define its callback handler */
		    f.addListener(new Runnable() {
		        public void run() {		    
		        	Response response;
					try {
						response = f.get();
					} catch (InterruptedException e) {
						throw new RuntimeException("Could not read from a playlist item's response.", e);
					} catch (ExecutionException e) {
						throw new RuntimeException("Could not read from a playlist item's response.", e);
					}

		        	LOGGER.info("Playlist request Complete!");
		            totalRequestsCompleted += 1;
		            
		            if (!playlistFinished) {
		            	InputStream stream = null;
						
						try {
							stream = response.getResponseBodyAsStream();
						} catch (IOException e) {
							throw new RuntimeException("Could not read from a playlist item's response.", e);
						}
						
		            	handlePlaylist(stream);
		            }
		        }
		    }, client.getConfig().executorService());
		    
		} catch (IOException ioe) {
			update(new RuntimeException(
					"There went something wrong with the network connection while fetching the playlist.", ioe));
		} catch (RuntimeException e) {
			update(e);
		}
	}
	
	/**
	 * Handles the result of the request for the playlist. If necessary, sets 
	 * 	a timer for reloading the playlist, checks whether maxTimeIdle was 
	 *  exceeded, and finally dispatches chunks' URLs
	 * 
	 * @param stream
	 * 					Request response as InputStream
	 */
	private void handlePlaylist(InputStream stream) {
		
		Map<Integer, String> newURLs = parseURLs(stream);
		
		/* Account for new URLs and check if maxTimeIdle was exceeded */
		if (newURLs.size() > 0) {
			loadedChunks += newURLs.size();
			lastPlaylistResponseTime = System.currentTimeMillis(); // Reset 
			LOGGER.info("Playlist last response time reset to " + lastPlaylistResponseTime);
		} else { 
			if (bFirstRequest) {
				lastPlaylistResponseTime = System.currentTimeMillis(); // Reset
				LOGGER.info("Playlist last response time reset to " + lastPlaylistResponseTime);
			} else {
				if (System.currentTimeMillis() - lastPlaylistResponseTime > maxTimeIdle) {
					LOGGER.info("Time being idle reading the playlist: " + (System.currentTimeMillis() - lastPlaylistResponseTime));
					timerIsActive = false;
					timer.cancel();
					try {
						throw new RuntimeException("Maximum time being idle has been exceeded. The input playlist was not updated as often as expected.");
					} catch (RuntimeException e) {
						update(e);
					}					
				} 					
			}
		}
		bFirstRequest = false;
		
		/* If playlist is not finished, make additional calls */
		if (!playlistFinished) {
			if (!timerIsActive) {
				timerIsActive = true;
				try{
					timer.scheduleAtFixedRate(timerTask, 0, period); // Task executed every period/1000 sec.
				} catch (IllegalStateException e) {
					LOGGER.warn("The timer was already cancelled. Scheduling is no longer possible.");	
				}
			}
		} else {
			timerIsActive = false;
			timer.cancel();			
			update("PLAYLIST_FINISHED:"+loadedChunks); 
		}
		
		if (newURLs.size() > 0) {
			fetchChunks(newURLs.values());
		}
	}

	/**
	 * Parse the chunks' URLs 
	 * 
	 * @param stream
	 * @return HashMap of URLs {1:http://..., 2:http://..., ...}
	 */
	private Map<Integer, String> parseURLs(InputStream stream) {
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
	    
		Map<Integer, String> newURLs = new HashMap<Integer, String>();
		int count = 0;
		
		String line;
		try {
			while ((line = bufferedReader.readLine())!= null) {
				if (line.contains("#")) { // It's a playlist comment
					if (line.contains(endTag)) {
						playlistFinished = true;
					}
				} else {
					if (count >= loadedChunks) { // Filter new urls
						newURLs.put(count, line);
					}
					count += 1;
				}
			}
		} catch (IOException e) {
			update(new RuntimeException(
					"There went something wrong while parsing a playlist item's response.", e));
		}
		
		return newURLs;
	}

	/**
	 * Fetches the chunks asynchronously
	 * 
	 * @param URLs
	 * 				Collection of chunks to be fetched
	 */
	private void fetchChunks(Collection<String> URLs) {
		Iterator<String> iterator = URLs.iterator();
		String chunkURL = null;
		
		while (iterator.hasNext()) {
				chunkURL = iterator.next();
				
				try {
					/* Create the asynchronous request */
					totalRequestsMade += 1;
				    final ListenableFuture<Response> f =
				        client.prepareGet(chunkURL).execute(new AsyncCompletionHandler<Response>() {

				            @Override
				            public Response onCompleted(Response response) throws Exception {
				            	LOGGER.info("Chunk response Complete!");
				            	return response;
				            }

				            @Override
				            public void onThrowable(Throwable t) {
				                super.onThrowable(t);
				                LOGGER.error("Error executing request: " + t);
				                throw new RuntimeException(
				                		"There went something wrong with the network connection while fetching a playlist item.", t);
				            }
				        });
				    
				    /* Define its callback handler */
				    f.addListener(new Runnable() {
				        public void run() {
				        	Response response;
							try {
								response = f.get();
							} catch (InterruptedException e) {
								throw new RuntimeException("Could not read from a playlist item's response.", e);
							} catch (ExecutionException e) {
								throw new RuntimeException("Could not read from a playlist item's response.", e);
							}
							
				        	LOGGER.info("Chunk request Complete!");
				            totalRequestsCompleted += 1;
				            InputStream stream = null;
				            
							try {
								stream = response.getResponseBodyAsStream();
							} catch (IOException e) {
								throw new RuntimeException("Could not read from a playlist item's response.", e);
							}
						
							handleChunk(stream);
				        }
				    }, client.getConfig().executorService());
				    
				} catch (IOException ioe) {
					update(new RuntimeException(
						"There went something wrong with the network connection while fetching a playlist item.", ioe));
				} catch (RuntimeException e) {
					update(e);
				}
		} 
	}

	/**
	 * Handles the result of the request for each chunk. Parses the stream 
	 * 	 and notifies observers attaching its IData wrapper
	 * 
	 * @param stream
	 * 					Request response as InputStream
	 */
	private void handleChunk(InputStream stream) {
		IData data = null;
		
		try{
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)) {
				data = chunkParser.parse(stream, mimeType, schema);
			} else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)) {
				data = chunkParser.parseBase64(stream, mimeType, schema);
			}
		} catch (RuntimeException e) {
			update(new RuntimeException(
					"There went something wrong while parsing a playlist item.", e));
		}
		
		if (data != null){
			update(data);
		}
	}
		
	@Override
	public void addObserver(IObserver o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(IObserver o) {
		observers.remove(o);		
	}

	@Override
	public Object getState() {
	  return null; // Not necessary here
	}

	@Override
	public void update(Object state) {
		notifyObservers(state);
	}
	
	private void notifyObservers(Object state) {
		Iterator<IObserver> i = observers.iterator();
		while (i.hasNext()) {
			IObserver o = (IObserver) i.next();
			o.update(state);
		}
	}

	/**
	 * Task to be executed periodically
	 */
	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			readPlaylist();		
		}
	};

}
