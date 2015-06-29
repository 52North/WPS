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

package org.n52.wps.client.test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.ResponseFormType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ExecuteRequestBuilderTest extends AbstractITClass{

	private ProcessDescription processDescription;	
	private ProcessDescriptionType processDescriptionType;	
	private String inputID;
	private String outputID;
	private String url = "http://xyz.test.data";
	private String complexDataString = "testString";
	
	@Before
	public void setUp(){
		MockMvcBuilders.webAppContextSetup(this.wac).build();
//		WPSConfig.getInstance().setConfigurationManager(this.wac.getBean(ConfigurationManager.class));	
		processDescription = new MultiReferenceBinaryInputAlgorithm().getDescription();
		processDescriptionType = ((ProcessDescriptionType)processDescription.getProcessDescriptionType(WPSConfig.VERSION_100));	
		inputID = processDescriptionType.getDataInputs().getInputArray(0).getIdentifier().getStringValue();
		outputID = processDescriptionType.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue();
	}
	
    @Test
    public void addComplexDataInputByReference() {

        ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(processDescriptionType);
    	
    	addTestDataByReference(executeRequestBuilder);

        ExecuteDocument request = executeRequestBuilder.getExecute();

        Assert.assertThat("generated doc contains input id", request.toString(), containsString(inputID));
        Assert.assertThat("generated doc contains input url", request.toString(), containsString(url));
        Assert.assertThat("document is valid", request.validate(), is(true));
    }
    
    @Test
    public void addComplexDataInputString() {
    	
    	ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(processDescriptionType);
    	
    	addTestDataString(executeRequestBuilder);
    	
    	ExecuteDocument request = executeRequestBuilder.getExecute();
    	
    	Assert.assertThat("generated doc contains input id", request.toString(), containsString(inputID));
    	Assert.assertThat("generated doc contains input string", request.toString(), containsString(complexDataString));
    	Assert.assertThat("document is valid", request.validate(), is(true));
    }
    
    @Test
    public void setSupportedMimeTypeForOutput(){
        ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(processDescriptionType);
    	
    	addTestDataByReference(executeRequestBuilder);
        
        String mimeType = getMimeType(processDescriptionType, false);
        
        executeRequestBuilder.setMimeTypeForOutput(mimeType, outputID);
        
        ExecuteDocument request = executeRequestBuilder.getExecute();
        
        checkOutputIdentifier(request.getExecute(), outputID);
        checkOutputMimeType(request.getExecute(), mimeType);
        Assert.assertThat("document is valid", request.validate(), is(true));
    	
    }
    
    @Test
    public void setDefaultMimeTypeForOutput(){
    	ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(processDescriptionType);
    	
    	addTestDataByReference(executeRequestBuilder);
    	
    	String mimeType = getMimeType(processDescriptionType, true);
    			
    	ExecuteDocument request = executeRequestBuilder.getExecute();
        
        executeRequestBuilder.setMimeTypeForOutput(mimeType, outputID);
    	
        checkOutputIdentifier(request.getExecute(), outputID);
        checkOutputMimeType(request.getExecute(), mimeType);
    	Assert.assertThat("document is valid", request.validate(), is(true));
    	
    }
    
    private void addTestDataByReference(ExecuteRequestBuilder executeRequestBuilder){
        
    	InputType inputType = InputType.Factory.newInstance();

        inputType.addNewIdentifier().setStringValue(inputID);
        inputType.addNewReference().setHref(url);

        executeRequestBuilder.addComplexData(inputType);
    	
    }
    
    private void addTestDataString(ExecuteRequestBuilder executeRequestBuilder){
    	
    	try {
			executeRequestBuilder.addComplexData(inputID, complexDataString, "", "", "text/plain");
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    	
    }
    
    private String getMimeType(ProcessDescriptionType processDescriptionType, boolean isGetDefaultMimeType){
    	
    	String result = "";
    	
    	ProcessOutputs processOutputs = processDescriptionType.getProcessOutputs();
    	
    	assertNotNull(processOutputs);
    	
    	OutputDescriptionType outputDescriptionType = processOutputs.getOutputArray(0);
    	
    	assertNotNull(outputDescriptionType);
    	
    	SupportedComplexDataType complexDataType = outputDescriptionType.getComplexOutput();
    	
    	assertNotNull(complexDataType);
    	
    	if(isGetDefaultMimeType){
    		ComplexDataCombinationType defaultFormat = complexDataType.getDefault();
    		
    		assertNotNull(defaultFormat);
    		
    		ComplexDataDescriptionType format = defaultFormat.getFormat();
    		
    		assertNotNull(format);
    		
    		result = format.getMimeType();    		
    	}else{
    		ComplexDataCombinationsType supportedFormats = complexDataType.getSupported();
    		
    		assertNotNull(supportedFormats);
    		
    		ComplexDataDescriptionType format = supportedFormats.getFormatArray(0);
    		
    		assertNotNull(format);
    		
    		result = format.getMimeType();   
    	}
    	
    	return result;
    }

    private void checkOutputMimeType(Execute execute, String mimeType){
    	
    	DocumentOutputDefinitionType outputDefinitionType = getOutputDefinitionType(execute);
    	
    	assertTrue(outputDefinitionType.getMimeType() != null && outputDefinitionType.getMimeType().equals(mimeType)); 
    	
    }
    
    private void checkOutputIdentifier(Execute execute, String identifier){
    	
    	DocumentOutputDefinitionType outputDefinitionType = getOutputDefinitionType(execute);
    	
    	assertTrue(outputDefinitionType.getIdentifier() != null && outputDefinitionType.getIdentifier().getStringValue().equals(identifier)); 
    	
    }
    
    private DocumentOutputDefinitionType getOutputDefinitionType(Execute execute){
    	
    	ResponseFormType responseFormType = execute.getResponseForm();
    	
    	assertNotNull(responseFormType);
    	
    	ResponseDocumentType responseDocumentType = responseFormType.getResponseDocument();
    	
    	assertNotNull(responseDocumentType);
    	
    	DocumentOutputDefinitionType outputDefinitionType = responseDocumentType.getOutputArray(0);
    	
    	assertNotNull(outputDefinitionType);  
    	
    	return outputDefinitionType;
    	
    }
    
}
