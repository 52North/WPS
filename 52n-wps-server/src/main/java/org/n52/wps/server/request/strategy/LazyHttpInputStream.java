package org.n52.wps.server.request.strategy;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

/**
 * An extension of an Input Stream with HTTP Connection abilities.
 * Uses an {@link InputStream} internally. HTTP connection is established
 * in a lazy fashion, i.e. on first read attempt.
 * 
 * This class shall prevent timeout issues with I/O streaming in the WPS framework.
 * 
 * @author Matthias Mueller, TU Dresden
 *
 */
public class LazyHttpInputStream extends InputStream {

	private InputStream is;
	private boolean initDone = false;
	
	// connection parameters
	final boolean useHttpGet;
	final String dataURLString;
	final String body;
	final String mimeType;
	
	/**
	 * Constructor for HTTP/POST
	 * 
	 * @param dataURLString
	 * @param body
	 * @param mimeType
	 */
	public LazyHttpInputStream (final String dataURLString, final String body, final String mimeType){
		this.dataURLString = dataURLString;
		this.body = body;
		this.mimeType = mimeType;
		useHttpGet = false;
	}
	
	/**
	 * Constructor for HTTP/GET
	 * 
	 * @param dataURLString
	 * @param body
	 * @param mimeType
	 */
	public LazyHttpInputStream (final String dataURLString, final String mimeType){
		this.dataURLString = dataURLString;
		this.body = null;
		this.mimeType = mimeType;
		useHttpGet = true;
	}
	
	/**
	 * Private init method that makes HTTP connections.
	 * 
	 * @throws IOException
	 */
	private final void init() throws IOException{
		if (useHttpGet){
			is = httpGet(dataURLString, mimeType);
		} else {
			is = httpPost(dataURLString, body, mimeType);
		}
		
		initDone = true;
	}


	@Override
	public int read() throws IOException {
		if (!initDone){
			init();
		}

		return is.read();
	}

	@Override
	public int available() throws IOException {
		if (!initDone){
			init();
		}
		return is.available();
	}

	@Override
	public void close() throws IOException {
		if (!initDone){
			init();
		}
		is.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		if (!initDone){
			try {
				init();
			} catch (IOException e) {
				// silent catch
			}
		}
		is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		if (!initDone){
			init();
		}
		is.reset();
	}

	@Override
	public boolean markSupported() {
		if (!initDone){
			// silent catch
		}
		return is.markSupported();
	}
	
	/**
	 * Make a GET request using mimeType and href
	 * 
	 * TODO: add support for autoretry, proxy
	 */
	private static InputStream httpGet(final String dataURLString, final String mimeType) throws IOException {
		HttpClient backend = new DefaultHttpClient();
		DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);
		
		HttpGet httpget = new HttpGet(dataURLString);
		
		if (mimeType != null){
			httpget.addHeader(new BasicHeader("Content-type", mimeType));
		}
		
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		return entity.getContent();
	}
	
	/**
	 * Make a POST request using mimeType and href
	 * 
	 * TODO: add support for autoretry, proxy
	 */
	private static InputStream httpPost(final String dataURLString, final String body, final String mimeType) throws IOException {
		HttpClient backend = new DefaultHttpClient();
		
		DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);
		
		HttpPost httppost = new HttpPost(dataURLString);
		
		if (mimeType != null){
			httppost.addHeader(new BasicHeader("Content-type", mimeType));
		}
		
		// set body entity
		HttpEntity postEntity = new StringEntity(body);
		httppost.setEntity(postEntity);
		
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resultEntity = response.getEntity();
		return resultEntity.getContent();
	}

}
