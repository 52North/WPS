/**
 * ﻿Copyright (C) 2007 - ${currentYear} 52°North Initiative for Geospatial Open Source
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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
