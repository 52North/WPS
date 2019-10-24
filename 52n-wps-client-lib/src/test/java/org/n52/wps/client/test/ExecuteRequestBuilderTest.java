/**
 * ﻿Copyright (C) 2007 - 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.junit.Test;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm;

public class ExecuteRequestBuilderTest {

	private ProcessDescriptionType processDescriptionType = new MultiReferenceBinaryInputAlgorithm().getDescription();	
	private String inputID = processDescriptionType.getDataInputs().getInputArray(0).getIdentifier().getStringValue();
	private String outputID = processDescriptionType.getProcessOutputs().getOutputArray(0).getIdentifier().getStringValue();
	private String url = "http://xyz.test.data";
	private String complexDataString = "testString";
	
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
