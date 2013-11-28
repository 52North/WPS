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
