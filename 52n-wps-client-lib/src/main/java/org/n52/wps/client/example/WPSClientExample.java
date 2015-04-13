/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.client.example;

import java.io.IOException;
import java.util.HashMap;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.client.ExecuteResponseAnalyser;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.commons.WPSConfig;
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
			e.printStackTrace();
		}
		try {
            requestGetCapabilities(wpsURL);

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
                FeatureCollection< ? , ? > featureCollection = ((GTVectorDataBinding) data)
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

        System.out.println("Processes in capabilities:");
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
(FeatureCollection< ? , ? >) inputValue);
					executeBuilder
							.addComplexData(
									inputName,
									data,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									null, "text/xml");
				}
				// Complexdata Reference
				if (inputValue instanceof String) {
					executeBuilder
							.addComplexDataReference(
									inputName,
									(String) inputValue,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									null, "text/xml");
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
		
		//TODO find way to initialize parsers/generators
		
		WPSClientExample client = new WPSClientExample();
		client.testExecute();
	}

}