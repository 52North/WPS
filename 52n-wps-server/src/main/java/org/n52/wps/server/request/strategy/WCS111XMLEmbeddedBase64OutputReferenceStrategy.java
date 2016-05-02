/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server.request.strategy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import net.opengis.wps.x100.InputType;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.ExceptionReport;

public class WCS111XMLEmbeddedBase64OutputReferenceStrategy implements IReferenceStrategy{
    
    private String fetchedMimeType;
    private String fetchedEncoding;

	@Override
	public boolean isApplicable(InputType input) {

		if(input.getReference().isSetBody()) {			
			XmlObject xo =  input.getReference().getBody();			
			return xo.toString().contains("http://www.opengis.net/wcs/1.1.1");
		}else{
			String dataURLString = input.getReference().getHref();
			return (dataURLString.contains("=GetCoverage") && dataURLString.contains("=1.1.1"));		
		}
	}

	@Override
	public ReferenceInputStream fetchData(InputType input) throws ExceptionReport {

		String dataURLString = input.getReference().getHref();
	
		String schema = input.getReference().getSchema();
		String encoding = input.getReference().getEncoding();
		String mimeType = input.getReference().getMimeType();
		
		try {
			URL dataURL = new URL(dataURLString);
			// Do not give a direct inputstream.
			// The XML handlers cannot handle slow connections
			URLConnection conn = dataURL.openConnection();
			conn.setRequestProperty("Accept-Encoding", "gzip");
			conn.setRequestProperty("Content-type", "multipart/mixed");
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
			
			BufferedReader bRead = new BufferedReader(new InputStreamReader(inputStream));
			
			String line = "";
			
			//boundary between different content types
			String boundary = "";
			
			boolean boundaryFound = false;
			
			boolean encodedImagepart = false;
			
			String encodedImage = "";
			
			//e.g. base64
			String contentTransferEncoding = "";
			
			String imageContentType = "";
			
			int boundaryCount = 0;
			
			while((line = bRead.readLine()) != null){
				
				if(line.contains("boundary")){
					boundary = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
					boundaryFound = true;
					continue;
				}
				if(boundaryFound){					
					if(line.contains(boundary)){
						boundaryCount++;
						continue;
					}
				}
				
				if(encodedImagepart){
					encodedImage = encodedImage.concat(line);
				}				
				//is the image always the third part?!
				else if(boundaryCount == 2){
					if(line.contains("Content-Type")){
						imageContentType = line.substring(line.indexOf(":") +1).trim();
					}else if(line.contains("Content-Transfer-Encoding")){
						contentTransferEncoding = line.substring(line.indexOf(":") +1).trim();					
					}else if(line.contains("Content-ID")){
						/*	just move further one line (which is hopefully empty)
						 * 	and start parsing the encoded image 				
						 */
						line = bRead.readLine();
						encodedImagepart = true;
					}
				}
				
			}
			
			return new ReferenceInputStream(
                    new Base64InputStream(new ByteArrayInputStream(encodedImage.getBytes())),
                    imageContentType,
                    null); // encoding is null since encoding was removed
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
