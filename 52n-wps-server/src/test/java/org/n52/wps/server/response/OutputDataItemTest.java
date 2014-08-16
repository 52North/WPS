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
package org.n52.wps.server.response;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.Request;

/**
 * @author BenjaminPross(bpross-52n)
 * 
 * This class is for testing the updateResponseForLiteralData() method of the class OutputDataItem.java.
 *
 */
public class OutputDataItemTest {
	
	private ProcessDescriptionType descriptionsType;
	private String processID = "org.n52.wps.server.response.OutputDataItemTest";
	private ExecuteResponseDocument mockupResponseDocument;
	private LanguageStringType outputTitle = LanguageStringType.Factory
			.newInstance();
	private LanguageStringType processTitle = LanguageStringType.Factory
			.newInstance();
	private Random random = new Random();
	private List<ILiteralData> literalDataList;

	@Before
	public void setUp() {

		literalDataList = new ArrayList<ILiteralData>();

		String url = "";
		try {
			url = "http://52north.org";
			literalDataList.add(new LiteralAnyURIBinding(new URL(url).toURI()));
		} catch (Exception e1) {
			System.out.println(url + " caused " + e1);
		}

		String uuid = UUID.randomUUID().toString();
		
		literalDataList.add(new LiteralBase64BinaryBinding(uuid.getBytes()));
		literalDataList.add(new LiteralBooleanBinding(true));
		literalDataList.add(new LiteralByteBinding((byte) 127));
		literalDataList.add(new LiteralDateTimeBinding(Calendar.getInstance()
				.getTime()));
		literalDataList.add(new LiteralDoubleBinding(random.nextDouble()));
		literalDataList.add(new LiteralFloatBinding(random.nextFloat()));
		literalDataList.add(new LiteralIntBinding(random.nextInt()));
		literalDataList.add(new LiteralLongBinding(random.nextLong()));
		literalDataList.add(new LiteralShortBinding((short) random.nextInt(Short.MAX_VALUE + 1)));
		literalDataList.add(new LiteralStringBinding(uuid));

		outputTitle.setStringValue("output title");
		processTitle.setStringValue("process title");

		ProcessDescriptionsDocument descriptionsDocument = ProcessDescriptionsDocument.Factory
				.newInstance();
		descriptionsType = descriptionsDocument.addNewProcessDescriptions()
				.addNewProcessDescription();

		descriptionsType.addNewIdentifier().setStringValue(processID);

		mockupResponseDocument = createExecuteResponseDocument();
	}

	private ExecuteResponseDocument createExecuteResponseDocument() {

		ExecuteResponseDocument doc = ExecuteResponseDocument.Factory
				.newInstance();
		ExecuteResponse responseElem = doc.addNewExecuteResponse();
		XmlCursor c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(
				new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
						"schemaLocation"),
				"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
        responseElem.setServiceInstance(WPSConfig.getInstance().getServiceEndpoint()
				+ "?REQUEST=GetCapabilities&SERVICE=WPS");
		responseElem.setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseElem.setService("WPS");
		responseElem.setVersion(Request.SUPPORTED_VERSION);
		ProcessBriefType process = responseElem.addNewProcess();
		process.addNewIdentifier().setStringValue(processID);
		process.setProcessVersion("1.0.0");
		process.setTitle(processTitle);
		responseElem.addNewStatus().setProcessSucceeded("Process successful");
		responseElem.getStatus().setCreationTime(Calendar.getInstance());

		responseElem.addNewProcessOutputs();

		return doc;

	}

	@Test
	public void testUpdateResponseForLiteralData() {

		for (ILiteralData literalData : literalDataList) {

			try {
				testLiteralOutput(literalData);
			} catch (Exception e) {
				System.out.println("Test failed for " + literalData.getClass()
						+ " " + e);
			}

			mockupResponseDocument.getExecuteResponse().getProcessOutputs()
					.removeOutput(0);
		}
	}

	private void testLiteralOutput(ILiteralData literalDataBinding)
			throws Exception {

		String startText = "Testing " + literalDataBinding.getClass()
				+ " and value ";

		String endText = "ResponseDocument valid for "
				+ literalDataBinding.getClass() + " and value ";

		if (literalDataBinding.getPayload() instanceof byte[]) {

			byte[] bytes = (byte[]) literalDataBinding.getPayload();

			String bytesAsIntegerValues = "[";

			for (int i = 0; i < bytes.length; i++) {
				if(i < bytes.length -1){
				bytesAsIntegerValues = bytesAsIntegerValues
						.concat((int) bytes[i] + ", ");
				}else{
					bytesAsIntegerValues = bytesAsIntegerValues
							.concat((int) bytes[i] + "]");					
				}
			}
			startText = startText.concat("" + bytesAsIntegerValues);
			endText = endText.concat("" + bytesAsIntegerValues);
		} else {
			startText = startText.concat("" + literalDataBinding.getPayload());
			endText = endText.concat("" + literalDataBinding.getPayload());
		}

		System.out.println(startText);

		ProcessOutputs processOutputs = descriptionsType.addNewProcessOutputs();
		OutputDescriptionType outputDescType = processOutputs.addNewOutput();
		outputDescType.addNewIdentifier().setStringValue("output");
		LiteralOutputType outputType = outputDescType.addNewLiteralOutput();

		String dataTypeAsString = BasicXMLTypeFactory
				.getXMLDataTypeforBinding(literalDataBinding.getClass());

		outputType.addNewDataType().setStringValue(dataTypeAsString);

		OutputDataItem ouDI = new OutputDataItem(literalDataBinding, "output",
				null, null, null, outputTitle, processID, descriptionsType);

		ouDI.updateResponseForLiteralData(mockupResponseDocument,
				dataTypeAsString);

		assertTrue(validateResponseDocument(mockupResponseDocument));

		System.out.println(endText);
		System.out.println();
	}

	private boolean validateResponseDocument(ExecuteResponseDocument doc) {
		XmlOptions xmlOptions = new XmlOptions();
		List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
		xmlOptions.setErrorListener(xmlValidationErrorList);
		boolean valid = doc.validate(xmlOptions);
		if (!valid) {
			System.err
					.println("Error validating ExecuteResponseDocument for data type"
							+ doc.getExecuteResponse().getProcessOutputs()
									.getOutputArray(0).getData()
									.getLiteralData().getDataType());
			for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
				System.err.println("\tMessage: "
						+ xmlValidationError.getMessage());
				System.err.println("\tLocation of invalid XML: "
						+ xmlValidationError.getCursorLocation().xmlText());
			}
		}
		return valid;
	}

}
