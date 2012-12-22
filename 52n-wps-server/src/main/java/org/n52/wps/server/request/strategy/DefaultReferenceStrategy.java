package org.n52.wps.server.request.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;

import net.opengis.wps.x100.InputType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;

/**
 * 
 * @author Matthias Mueller
 * 
 * Basic methods to retrieve input data using HTTP/GET, HTTP/POST or HTTP/POST with href'd body
 *
 */
public class DefaultReferenceStrategy implements IReferenceStrategy{
	
	// TODO: follow HTTP redirects with LaxRedirectStrategy
	
	Logger logger = Logger.getLogger(DefaultReferenceStrategy.class);
	
	//TODO: get proxy from config
	//static final HttpHost proxy = new HttpHost("127.0.0.1", 8080, "http");
	static final HttpHost proxy = null;
	
	@Override
	public boolean isApplicable(InputType input) {
		// TODO Auto-generated method stub
		return true;
	}
	
	// TODO: follow references, e..g 
	
	@Override
	public InputStream fetchData(InputType input) throws ExceptionReport {
		
		String href = input.getReference().getHref();
		String mimeType = input.getReference().getMimeType();
		
		try {
			
//			URL dataURL = new URL(dataURLString);
			// Do not give a direct inputstream.
			// The XML handlers cannot handle slow connections
//			URLConnection conn = dataURL.openConnection();
//			conn.setRequestProperty("Accept-Encoding", "gzip");
//			conn.setRequestProperty("Content-type", mimeType);
			
			// Handling POST with referenced document
			if(input.getReference().isSetBodyReference()) {
				
				/**
				 * Old code - did we ever test this?
				 */
//				URL dataURL = new URL(href);
//				URLConnection conn = dataURL.openConnection();
//				conn.setRequestProperty("Content-type", mimeType);
//				conn.setRequestProperty("Accept-Encoding", "gzip");
//				String bodyReference = input.getReference().getBodyReference().getHref();
//				URL bodyReferenceURL = new URL (bodyReference);
//				URLConnection bodyReferenceConn = bodyReferenceURL.openConnection();
//				bodyReferenceConn.setRequestProperty("Accept-Encoding", "gzip");
//				InputStream referenceInputStream = retrievingZippedContent(bodyReferenceConn);
//				IOUtils.copy(referenceInputStream, conn.getOutputStream());
//				InputStream inputStream = retrievingZippedContent(conn);
//				return inputStream;
				
				String bodyHref = input.getReference().getBodyReference().getHref();
				
				// but Body reference into a String
				StringWriter writer = new StringWriter();
				IOUtils.copy(httpGet(bodyHref, null), writer);
				String body = writer.toString();
				
				// trigger POST request
				return httpPost(href, body, mimeType);
				
			}
			
			// Handle POST with inline message
			else if (input.getReference().isSetBody()) {
				String body = input.getReference().getBody().toString();
				return httpPost(href, body, mimeType);
			}
			
			// Handle get request
			else {
				return httpGet(href, mimeType);
			}
			
			
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("Error occured while parsing XML", 
										ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		catch(MalformedURLException e) {
			String inputID = input.getIdentifier().getStringValue();
			throw new ExceptionReport("The inputURL of the execute is wrong: inputID: " + inputID + " | dataURL: " + href, 
										ExceptionReport.INVALID_PARAMETER_VALUE );
		}
		catch(IOException e) {
			 String inputID = input.getIdentifier().getStringValue();
			 throw new ExceptionReport("Error occured while receiving the complexReferenceURL: inputID: " + inputID + " | dataURL: " + href, 
					 				ExceptionReport.INVALID_PARAMETER_VALUE );
		}
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
