package org.n52.wps.server.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.opengis.ows.x11.ExceptionReportDocument;

import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.RasterPlaylistBinding;
import org.n52.wps.io.data.binding.complex.VectorPlaylistBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.FlatFileDatabase;
import org.n52.wps.server.database.FolderDatabase;
import org.n52.wps.server.database.IDatabase;

/**
 * This class handles output playlists by providing methods to create, alter, 
 * 	and close them. 
 *
 * @author gcarrillo
 *
 */
public class PlaylistOutputHandler  {
	
	/* Databases to store playlist and chunks, respectively */
	final private FlatFileDatabase ffdb = (FlatFileDatabase) FlatFileDatabase.getInstance();
	final private IDatabase fdb = FolderDatabase.getInstance();
	
	private String playlistId;
	public boolean isClosed = false;
	final public String startTag = "#SPATIAL-DATA-PLAYLIST";
	final public String endTag = "#PLAYLIST-END";
	final public String exceptionTag = "#EXCEPTION:";
	
	private IGenerator playlistGenerator;
	private IGenerator chunkGenerator;
	private String schema;
	private String mimeType;
	private String encoding;

	/**
	 * Creates a PlaylistOutputHandler 
	 */
	public PlaylistOutputHandler(IGenerator playlistGenerator, IGenerator chunkGenerator, String mimeType, String schema, String encoding) {
		this.playlistGenerator = playlistGenerator;
		this.chunkGenerator = chunkGenerator;
		this.mimeType = mimeType;
		this.schema = schema;
		this.encoding = encoding;
	}
	
	/**
	 * Creates and initializes a playlist in the database. 
	 * 
	 * @return Playlist's URL 
	 */
	public String createPlaylist() {
		InputStream stream;
		String url = "";
		playlistId = "playlist" + String.valueOf(System.currentTimeMillis());
		
		try {
			stream = playlistGenerator.generateStream(null, null, null);
			url = ffdb.storeComplexValue(playlistId, stream, "ComplexDataResponse", 
					IOHandler.MIME_TYPE_PLAYLIST);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		updatePlaylist(startTag);
		return url;
	}
	
	/**
	 * Appends a chunk's URL to the playlist
	 * 
	 * @param resultChunk 
	 * 						Chunk's wrapper 
	 * @param chunkId 
	 * 						Chunk's Id
	 * @return Whether the URL was appended or not
	 */
	public boolean appendChunk(IData resultChunk, String chunkId) {
		String url = storeChunk(resultChunk, playlistId+"_chunk"+chunkId);
		updatePlaylist(url);
		return true;
	}

	/**
	 * Appends an exception item (Tag + URL) to the playlist
	 * 
	 * @param exception 
	 * 					ExceptionReport containing the exception information
	 * @return Whether the exception item was appended or not
	 */
	public boolean appendException(ExceptionReport exception) {	    
		/* Store the exception report */
	    ExceptionReportDocument doc = exception.getExceptionDocument();
	    InputStream docStream = new ByteArrayInputStream(doc.toString().getBytes());
		String exceptionURL = ffdb.storeComplexValue(playlistId+"exception", docStream, null, "text/xml");
		
		try { 
			/* Update the playlist */
			String exceptionEntry = exceptionTag + exceptionURL;
			ffdb.updateComplexValue(playlistId, new ByteArrayInputStream(exceptionEntry.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the playlist by appending the end tag
	 * 
	 * @return Whether the playlist was closed or not
	 */
	public boolean closePlaylist() {
		if (!isClosed) {
			isClosed = true;
			updatePlaylist(endTag);
			return true;
		}
		return false;
	}
	
	/**
	 * Updates the playlist appending a new item (i.e., a new line)
	 * 
	 * @param item 
	 * 				Could be a comment, exception, end tag, or URL 
	 */
	private boolean updatePlaylist(String item) {
		IData data = null;
		if (playlistGenerator.isSupportedDataBinding(VectorPlaylistBinding.class)) {
			data = new VectorPlaylistBinding(item);
		}
		else if (playlistGenerator.isSupportedDataBinding(RasterPlaylistBinding.class)) {
			data = new RasterPlaylistBinding(item);
		}
		
		try {
			InputStream is;
			is = playlistGenerator.generateStream(data, null, null);
			ffdb.updateComplexValue(playlistId, is);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Stores the data chunk 
	 * 
	 * @param data 
	 * 					Chunk's wrapper
	 * @param chunkId 	
	 * 					Chunk's Id
	 * @return Chunk's URL
	 */
	private String storeChunk(IData data, String chunkId) {
		InputStream stream = null;

		try {
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)) {
				stream = chunkGenerator.generateStream(data, mimeType, schema);
			} else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)) {
				stream = chunkGenerator.generateBase64Stream(data, mimeType, schema);
			} else {
				throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (IOException e) {
			try {
				throw new ExceptionReport("Error while generating Complex Data out of the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
			} catch (ExceptionReport e1) {
				e1.printStackTrace();
				return null;
			}
		} catch (ExceptionReport e) {
			e.printStackTrace();
			return null;
		}
		
		String url = fdb.storeComplexValue(chunkId, stream, "ComplexDataResponse", mimeType);
		return url;
	}
	
}
