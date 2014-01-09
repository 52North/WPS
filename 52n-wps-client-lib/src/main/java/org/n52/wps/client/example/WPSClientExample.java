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
package org.n52.wps.client.example;

import java.io.IOException;
import java.util.HashMap;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

public class WPSClientExample {

	public void testExecute() {

		String wpsURL = "http://localhost:8080/wps/WebProcessingService";

		String processID = "org.n52.wps.server.algorithm.SimpleBufferAlgorithm";

		try {
			ProcessDescriptionType describeProcessDocument = requestDescribeProcess(
					wpsURL, processID);
			System.out.println(describeProcessDocument);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			CapabilitiesDocument capabilitiesDocument = requestGetCapabilities(wpsURL);
			ProcessDescriptionType describeProcessDocument = requestDescribeProcess(
					wpsURL, processID);
			// define inputs
			HashMap<String, Object> inputs = new HashMap<String, Object>();
			// complex data by reference
			inputs.put(
					"data",
					"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=topp:tasmania_roads&outputFormat=GML3");
			// literal data
			inputs.put("width", "0.05");
			IData data = executeProcess(wpsURL, processID,
					describeProcessDocument, inputs);

			if (data instanceof GTVectorDataBinding) {
				FeatureCollection featureCollection = ((GTVectorDataBinding) data)
						.getPayload();
				System.out.println(featureCollection.size());
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CapabilitiesDocument requestGetCapabilities(String url)
			throws WPSClientException {

		WPSClientSession wpsClient = WPSClientSession.getInstance();

		wpsClient.connect(url);

		CapabilitiesDocument capabilities = wpsClient.getWPSCaps(url);

		ProcessBriefType[] processList = capabilities.getCapabilities()
				.getProcessOfferings().getProcessArray();

		for (ProcessBriefType process : processList) {
			System.out.println(process.getIdentifier().getStringValue());
		}
		return capabilities;
	}

	public ProcessDescriptionType requestDescribeProcess(String url,
			String processID) throws IOException {

		WPSClientSession wpsClient = WPSClientSession.getInstance();

		ProcessDescriptionType processDescription = wpsClient
				.getProcessDescription(url, processID);

		InputDescriptionType[] inputList = processDescription.getDataInputs()
				.getInputArray();

		for (InputDescriptionType input : inputList) {
			System.out.println(input.getIdentifier().getStringValue());
		}
		return processDescription;
	}

	public IData executeProcess(String url, String processID,
			ProcessDescriptionType processDescription,
			HashMap<String, Object> inputs) throws Exception {
		org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(
				processDescription);

		for (InputDescriptionType input : processDescription.getDataInputs()
				.getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			if (input.getLiteralData() != null) {
				if (inputValue instanceof String) {
					executeBuilder.addLiteralData(inputName,
							(String) inputValue);
				}
			} else if (input.getComplexData() != null) {
				// Complexdata by value
				if (inputValue instanceof FeatureCollection) {
					IData data = new GTVectorDataBinding(
							(FeatureCollection) inputValue);
					executeBuilder
							.addComplexData(
									inputName,
									data,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									"UTF-8", "text/xml");
				}
				// Complexdata Reference
				if (inputValue instanceof String) {
					executeBuilder
							.addComplexDataReference(
									inputName,
									(String) inputValue,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									"UTF-8", "text/xml");
				}

				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: "
							+ inputName);
				}
			}
		}
		executeBuilder.setMimeTypeForOutput("text/xml", "result");
		executeBuilder.setSchemaForOutput(
				"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
				"result");
		ExecuteDocument execute = executeBuilder.getExecute();
		execute.getExecute().setService("WPS");
		WPSClientSession wpsClient = WPSClientSession.getInstance();
		Object responseObject = wpsClient.execute(url, execute);
		if (responseObject instanceof ExecuteResponseDocument) {
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
			ExecuteResponseAnalyser analyser = new ExecuteResponseAnalyser(
					execute, response, processDescription);
			IData data = (IData) analyser.getComplexDataByIndex(0,
					GTVectorDataBinding.class);
			return data;
		}
		throw new Exception("Exception: " + responseObject.toString());
	}

	public static void main(String[] args) {
		WPSClientExample client = new WPSClientExample();
		client.testExecute();
	}

}