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

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;

import net.opengis.wps.x100.InputType;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	Logger logger = LoggerFactory.getLogger(DefaultReferenceStrategy.class);
	
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
	public ReferenceInputStream fetchData(InputType input) throws ExceptionReport {
		
		String href = input.getReference().getHref();
		String mimeType = input.getReference().getMimeType();
		
		try {
			// Handling POST with referenced document
			if(input.getReference().isSetBodyReference()) {
				
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
	private ReferenceInputStream httpGet(final String dataURLString, final String mimeType) throws IOException {
		HttpClient backend = new DefaultHttpClient();
		DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);
		
		HttpGet httpget = new HttpGet(dataURLString);
		
		if (mimeType != null){
			httpget.addHeader(new BasicHeader("Content-type", mimeType));
		}
		        
		return processResponse(httpclient.execute(httpget));
	}
	
	/**
	 * Make a POST request using mimeType and href
	 * 
	 * TODO: add support for autoretry, proxy
	 */
	private ReferenceInputStream httpPost(final String dataURLString, final String body, final String mimeType) throws IOException {
		HttpClient backend = new DefaultHttpClient();
		
		DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);
		
		HttpPost httppost = new HttpPost(dataURLString);
		
		if (mimeType != null){
			httppost.addHeader(new BasicHeader("Content-type", mimeType));
		}
		
		// set body entity
		HttpEntity postEntity = new StringEntity(body);
		httppost.setEntity(postEntity);
		
		return processResponse(httpclient.execute(httppost));
	}

    private ReferenceInputStream processResponse(HttpResponse response) throws IOException {
        
        HttpEntity entity = response.getEntity();
        Header header;
        
        header = entity.getContentType();
        String mimeType = header == null ? null : header.getValue();
        
        header = entity.getContentEncoding();
        String encoding = header == null ? null : header.getValue();
        
        return new ReferenceInputStream(entity.getContent(), mimeType, encoding);
    }
}
