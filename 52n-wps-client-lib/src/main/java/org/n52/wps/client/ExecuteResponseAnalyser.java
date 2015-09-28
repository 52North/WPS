/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * 
 * returns the processOutputs according to the encoding of the process.
 * @author foerster
 *
 */
public class ExecuteResponseAnalyser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ExecuteResponseAnalyser.class);
	
	ProcessDescriptionType processDesc;
	ExecuteDocument exec;
	Object response;
	
	public ExecuteResponseAnalyser(ExecuteDocument exec, Object response, ProcessDescriptionType processDesc) throws WPSClientException {
		this.processDesc = processDesc;
		this.exec= exec;
		if(response instanceof ExceptionReport){
			throw new WPSClientException("Output is not ComplexData but an Exception Report");
		}
		this.response = response;
	}
	
	
/**
 * delivers the parsed ComplexData by name
 * @param outputID id of the output element
 * @param binding Class of the output binding, e.g. GenericFileDataBinding
 * @return the parsed ComplexData in the requested format defined by the binding
 * @throws WPSClientException
 */
	public IData getComplexData(String outputID, Class<?> binding) throws WPSClientException {
		return parseProcessOutput(outputID, binding);
		
	}
	
	/**
	 * delivers the parsed ComplexData by index
	 * @param index index of the output element starting with 0
	 * @param binding Class of the output binding, e.g. GenericFileDataBinding
	 * @return the parsed ComplexData in the requested format defined by the binding
	 * @throws WPSClientException
	 */
	public IData getComplexDataByIndex(int index, Class<?> binding) throws WPSClientException {
		ExecuteResponseDocument doc = null;
		if(response instanceof ExecuteResponseDocument){
			doc = (ExecuteResponseDocument) response;
		}else{
			throw new WPSClientException("Output cannot be determined by index since it is either raw data or an exception report");
		}
		OutputDataType[] outputs = doc.getExecuteResponse().getProcessOutputs().getOutputArray();
		int counter = 0; 
		for(OutputDataType output : outputs) {
			if(output.getData().getComplexData() != null) {
				if(counter == index) {
					return this.parseProcessOutput(output.getIdentifier().getStringValue(), binding);
				}
				counter ++;
			}
		}
		return null;
	}
	
	/**
	 * delivers just the URL of a referenced output identified by index
	 * @param index index of the output
	 * @return URL of the stored output
	 * @throws WPSClientException 
	 */
	public String getComplexReferenceByIndex(int index) throws WPSClientException {
		ExecuteResponseDocument doc = null;
		if(response instanceof ExecuteResponseDocument){
			doc = (ExecuteResponseDocument) response;
		}else{
			throw new WPSClientException("Output cannot be determined by index since it is either raw data or an exception report");
		}
		OutputDataType[] outputs = doc.getExecuteResponse().getProcessOutputs().getOutputArray();
		int counter = 0; 
		for(OutputDataType output : outputs) {
			if(output.getReference() != null) {
				if(counter == index) {
					return output.getReference().getHref();
				}
				counter ++;
			}
		}
		RuntimeException rte = new RuntimeException("No reference found in response");		
		LOGGER.error(rte.getMessage());
		throw rte;
	}
		
	
		
	
	/**
	 * parses a specific WPS output
	 * 
	 * @param outputID id of the output
	 * @param outputDataBindingClass class of the desired output binding
	 * @return parsed WPS output as IData
	 * @throws WPSClientException
	 */
	private IData parseProcessOutput(String outputID, Class<?> outputDataBindingClass) throws WPSClientException {
		OutputDescriptionType outputDesc = null;
		
		String schema = null;
		String mimeType = null;
		String encoding = null;
		
		if(exec.getExecute().isSetResponseForm() && exec.getExecute().getResponseForm().isSetRawDataOutput()){
			// get data specification from request
			schema = exec.getExecute().getResponseForm().getRawDataOutput().getSchema();
			mimeType = exec.getExecute().getResponseForm().getRawDataOutput().getMimeType();
			encoding = exec.getExecute().getResponseForm().getRawDataOutput().getEncoding();
		}else if(exec.getExecute().isSetResponseForm() && exec.getExecute().getResponseForm().isSetResponseDocument()){
			DocumentOutputDefinitionType[] outputs = exec.getExecute().getResponseForm().getResponseDocument().getOutputArray();
			for(DocumentOutputDefinitionType output : outputs){
				if(output.getIdentifier().getStringValue().equals(outputID)){
					schema = output.getSchema();
					mimeType = output.getMimeType();
					encoding = output.getEncoding();
				}
			}
			
		}
		
		if(mimeType==null){
			for(OutputDescriptionType tempDesc : processDesc.getProcessOutputs().getOutputArray()) {
				if(outputID.equals(tempDesc.getIdentifier().getStringValue())) {
					outputDesc = tempDesc;
					break;
				}
			}
			// get default data spec since mime type is not set
			mimeType = outputDesc.getComplexOutput().getDefault().getFormat().getMimeType();
			encoding = outputDesc.getComplexOutput().getDefault().getFormat().getEncoding();
			schema = outputDesc.getComplexOutput().getDefault().getFormat().getSchema();
		}
		
		IParser parser = StaticDataHandlerRepository.getParserFactory().getParser(schema, mimeType, encoding, outputDataBindingClass);
		InputStream is = null;
		if(response instanceof InputStream){
			is = (InputStream)response;
		}else if (response instanceof ExecuteResponseDocument){
			ExecuteResponseDocument responseDocument = (ExecuteResponseDocument) response;
			OutputDataType[] processOutputs = responseDocument.getExecuteResponse().getProcessOutputs().getOutputArray();
			for(OutputDataType processOutput : processOutputs){
				if(processOutput.getIdentifier().getStringValue().equalsIgnoreCase(outputID)){
					if(processOutput.isSetReference()){
						//request the reference
						String urlString = processOutput.getReference().getHref();
						URL url;
						try {
							url = new URL(urlString);
							is = url.openStream();
						} catch (MalformedURLException e) {
							throw new WPSClientException("Could not fetch response from referenced URL", e);
						} catch (IOException e) {
							throw new WPSClientException("Could not fetch response from referenced URL", e);
						}
						
					}else{
						String complexDataContent;
						try {
							
							NodeList candidateNodes = processOutput.getData().getComplexData().getDomNode().getChildNodes();
							
						    Node complexDataNode = candidateNodes.getLength() > 1 ? candidateNodes.item(1) : candidateNodes.item(0);							
							
							complexDataContent = XMLUtil.nodeToString(complexDataNode);
							is = new ByteArrayInputStream(complexDataContent.getBytes());
						} catch (TransformerFactoryConfigurationError e) {
							LOGGER.error(e.getMessage());
						} catch (TransformerException e) {
							LOGGER.error(e.getMessage());
						}
					}
					
				}
			}
			
		}else{
			throw new WPSClientException("Wrong output type");
		}
		
		
		if(parser != null) {
			if(encoding != null && encoding.equalsIgnoreCase("base64")){
				return parser.parseBase64(is, mimeType, schema);
			}else{
				return parser.parse(is, mimeType, schema);
			}
		}
		RuntimeException rte = new RuntimeException("Could not find suitable parser");		
		LOGGER.error(rte.getMessage());
		throw rte;
	}
}