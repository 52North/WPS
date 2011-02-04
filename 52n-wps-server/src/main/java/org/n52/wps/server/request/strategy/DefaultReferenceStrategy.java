package org.n52.wps.server.request.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;

public class DefaultReferenceStrategy implements IReferenceStrategy{

	@Override
	public boolean isApplicable(InputType input) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public InputStream fetchData(InputType input) throws ExceptionReport {
		// OutputStream postContent = null;
		if(input.getReference().isSetBody()) {
			
		}
		String dataURLString = input.getReference().getHref();
	
		String schema = input.getReference().getSchema();
		String encoding = input.getReference().getEncoding();
		String mimeType = input.getReference().getMimeType();
		//URL dataURL = new URL("http", "proxy", 8080, dataURLString);
		
		try {
			URL dataURL = new URL(dataURLString);
			// Do not give a direct inputstream.
			// The XML handlers cannot handle slow connections
			URLConnection conn = dataURL.openConnection();
			conn.setRequestProperty("Accept-Encoding", "gzip");
			conn.setRequestProperty("Content-type", mimeType);
			//Handling POST with referenced document
			if(input.getReference().isSetBodyReference()) {
				String bodyReference = input.getReference().getBodyReference().getHref();
				URL bodyReferenceURL = new URL (bodyReference);
				URLConnection bodyReferenceConn = bodyReferenceURL.openConnection();
				bodyReferenceConn.setRequestProperty("Accept-Encoding", "gzip");
				InputStream referenceInputStream = retrievingZippedContent(bodyReferenceConn);
				IOUtils.copy(referenceInputStream, conn.getOutputStream());
			}
			//Handling POST with inline message
			else if (input.getReference().isSetBody()) {
				conn.setDoOutput(true);
				
				input.getReference().getBody().save(conn.getOutputStream());
			}
			InputStream inputStream = retrievingZippedContent(conn);
			return inputStream;
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("Error occured while parsing XML", 
										ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		catch(MalformedURLException e) {
			String inputID = input.getIdentifier().getStringValue();
			throw new ExceptionReport("The inputURL of the execute is wrong: inputID: " + inputID + " | dataURL: " + dataURLString, 
										ExceptionReport.INVALID_PARAMETER_VALUE );
		}
		catch(IOException e) {
			 String inputID = input.getIdentifier().getStringValue();
			 throw new ExceptionReport("Error occured while receiving the complexReferenceURL: inputID: " + inputID + " | dataURL: " + dataURLString, 
					 				ExceptionReport.INVALID_PARAMETER_VALUE );
		}
	}

	private InputStream retrievingZippedContent(URLConnection conn) throws IOException{
		String contentType = conn.getContentEncoding();
		if(contentType != null && contentType.equals("gzip")) {
			return new GZIPInputStream(conn.getInputStream());
		}
		else{
			return conn.getInputStream();
		}
	}
}
