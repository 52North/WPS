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

package org.n52.wps.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

/*
 * 
 * returns the processOutputs according to the encoding of the process.
 * @author foerster
 *
 */
public class ExecuteResponseAnalyser {
	
	
	
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
	public IData getComplexData(String outputID, Class binding) throws WPSClientException {
		return parseProcessOutput(outputID, binding);
		
	}
	
	/**
	 * delivers the parsed ComplexData by index
	 * @param index index of the output element starting with 0
	 * @param binding Class of the output binding, e.g. GenericFileDataBinding
	 * @return the parsed ComplexData in the requested format defined by the binding
	 * @throws WPSClientException
	 */
	public IData getComplexDataByIndex(int index, Class binding) throws WPSClientException {
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
		throw new RuntimeException("No reference found in response");
	}
		
	
		
	
	/**
	 * parses a specific WPS output
	 * 
	 * @param outputID id of the output
	 * @param outputDataBindingClass class of the desired output binding
	 * @return parsed WPS output as IData
	 * @throws WPSClientException
	 */
	private IData parseProcessOutput(String outputID, Class outputDataBindingClass) throws WPSClientException {
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
						is = processOutput.getData().getComplexData().newInputStream();
					}
					
				}
			}
			
		}else{
			throw new WPSClientException("Wrong output type");
		}
		
		
		if(parser != null) {
			if(encoding.equalsIgnoreCase("base64")){
				return parser.parseBase64(is, mimeType, schema);
			}else{
				return parser.parse(is, mimeType, schema);
			}
		}

		throw new RuntimeException("Could not find suitable parser");
	}
}
