/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.io.datahandler.generator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class GeoServerUploader {

	private String username;
	private String password;
	private String host;
	private String port;

	public GeoServerUploader(String username, String password, String host,
			String port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	public String uploadGeotiff(File file, String storeName)
			throws HttpException, IOException {
		String target = "http://" + host + ":" + port
				+ "/geoserver/rest/workspaces/N52/coveragestores/" + storeName
				+ "/external.geotiff?configure=first&coverageName=" + storeName;
		String request;
		if (file.getAbsolutePath().startsWith("/")) { // tried with
														// request.replaceAll("//","/");
														// but didn't seem to
														// work...
			request = "file:" + file.getAbsolutePath();
		} else {
			request = "file:/" + file.getAbsolutePath();
		}
		String result = sendRasterRequest(target, request, "PUT", username,
				password);
		return result;
	}

	public String uploadShp(File file, String storeName) throws HttpException,
			IOException {
		String target = "http://" + host + ":" + port
				+ "/geoserver/rest/workspaces/N52/datastores/" + storeName
				+ "/file.shp";
		InputStream request = new BufferedInputStream(new FileInputStream(file));
		String result = sendShpRequest(target, request, "PUT", username,
				password);
		return result;

	}

	public String createWorkspace() throws HttpException, IOException {
		String target = "http://" + host + ":" + port
				+ "/geoserver/rest/workspaces";
		String request = "<workspace><name>N52</name></workspace>";
		String result = sendRasterRequest(target, request, "POST", username,
				password);
		return result;
	}

	private String sendRasterRequest(String target, String request,
			String method, String username, String password)
			throws HttpException, IOException {
		HttpClient client = new HttpClient();
		EntityEnclosingMethod requestMethod = null;
		if (method.equalsIgnoreCase("POST")) {
			requestMethod = new PostMethod(target);
			requestMethod.setRequestHeader("Content-type", "application/xml");
		}
		if (method.equalsIgnoreCase("PUT")) {
			requestMethod = new PutMethod(target);
			requestMethod.setRequestHeader("Content-type", "text/plain");

		}

		requestMethod.setRequestBody(request);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				username, password);
		client.getState().setCredentials(null, null, creds);

		int statusCode = client.executeMethod(requestMethod);

		if (!((statusCode == HttpStatus.SC_OK) || (statusCode == HttpStatus.SC_CREATED))) {
			System.err.println("Method failed: "
					+ requestMethod.getStatusLine());
		}

		// Read the response body.
		byte[] responseBody = requestMethod.getResponseBody();
		return new String(responseBody);
	}

	private String sendShpRequest(String target, InputStream request,
			String method, String username, String password)
			throws HttpException, IOException {
		HttpClient client = new HttpClient();
		EntityEnclosingMethod requestMethod = null;
		if (method.equalsIgnoreCase("POST")) {
			requestMethod = new PostMethod(target);
			requestMethod.setRequestHeader("Content-type", "text/xml");
		}
		if (method.equalsIgnoreCase("PUT")) {
			requestMethod = new PutMethod(target);
			requestMethod.setRequestHeader("Content-type", "application/zip");

		}

		requestMethod.setRequestBody(request);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				username, password);
		client.getState().setCredentials(null, null, creds);

		int statusCode = client.executeMethod(requestMethod);

		if (!((statusCode == HttpStatus.SC_OK) || (statusCode == HttpStatus.SC_CREATED))) {
			System.err.println("Method failed: "
					+ requestMethod.getStatusLine());
		}

		// Read the response body.
		byte[] responseBody = requestMethod.getResponseBody();
		return new String(responseBody);
	}
}
