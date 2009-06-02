/*****************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.client;

import java.io.InputStream;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.AbstractBinaryParser;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;

/*
 * 
 * returns the processOutputs according to the encoding of the process.
 * @author foerster
 *
 */
public class ExecuteResponseAnalyser {
	
	Object response;
	ExecuteResponseDocument responseDoc;
	ProcessDescriptionType processDesc;
	ExecuteDocument exec;
	
	public ExecuteResponseAnalyser(Object response, ExecuteDocument exec, ProcessDescriptionType processDesc) {
		this.processDesc = processDesc;
		this.response = response;
		this.exec= exec;
	}
	
	public ExecuteResponseAnalyser(ExecuteResponseDocument responseDoc, ProcessDescriptionType processDesc) {
		this.responseDoc = responseDoc;
		this.processDesc= processDesc;		
	}
	
	public IData getComplexData(String name) {
		if(response != null) {
			return parseProcessOutput(response);
		}
		OutputDataType[] outputs = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray();
		for(OutputDataType output : outputs) {
			if(output.getIdentifier().getStringValue().equals(name)) {
				if(output.getData().getComplexData() == null) {
					break;
				}
				return this.parseProcessOutput(output);
			}
		}
		throw new IllegalArgumentException("specified output does not exist: " + name);
	}
	
	public Object getComplexDataByIndex(int index) {
		if(response != null) {
			return parseProcessOutput(response);
		}
		OutputDataType[] outputs = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray();
		int counter = 0; 
		for(OutputDataType output : outputs) {
			if(output.getData().getComplexData() != null) {
				if(counter == index) {
					return this.parseProcessOutput(output);
				}
				counter ++;
			}
		}
		return null;
	}
	
	public String getComplexReferenceByIndex(int index) {
		OutputDataType[] outputs = responseDoc.getExecuteResponse().getProcessOutputs().getOutputArray();
		int counter = 0; 
		for(OutputDataType output : outputs) {
			if(output.getReference() != null) {
				if(counter == index) {
					return output.getReference().getHref();
				}
				counter ++;
			}
		}
		return null;
	}
		
	
	
	private IData parseProcessOutput(OutputDataType output) {
		String schemaURL = output.getData().getComplexData().getSchema();
		if(schemaURL == null) {
			for(OutputDescriptionType outputDesc :processDesc.getProcessOutputs().getOutputArray()) {
				if(outputDesc.getIdentifier().getStringValue().equals(output.getIdentifier().getStringValue())) {
					schemaURL = outputDesc.getComplexOutput().getDefault().getFormat().getSchema();
				}
			}
		}
		if(schemaURL == null) {
			throw new IllegalArgumentException("Could not find outputSchemaURL for output: " + output.getIdentifier().getStringValue());
		}
		IParser parser = StaticDataHandlerRepository.getParserFactory().getParser(schemaURL, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING, GTVectorDataBinding.class);
		if(parser instanceof AbstractXMLParser) {
			AbstractXMLParser xmlParser = (AbstractXMLParser) parser;
			return xmlParser.parseXML(output.getData().getComplexData().newInputStream());
		}
		// parser is not of type AbstractXMLParser
		return null;
	}
	/**
	 * this parses rawData output directly.
	 * @param obj
	 * @return
	 */
	private IData parseProcessOutput(Object obj) {
		String schema = exec.getExecute().getResponseForm().getRawDataOutput().getSchema();
		if(schema == null) {
			schema = processDesc.getProcessOutputs().getOutputArray(0).getComplexOutput().getDefault().getFormat().getSchema();
			
		}
		String mimeType = exec.getExecute().getResponseForm().getRawDataOutput().getMimeType();
		if(mimeType == null) {
			mimeType = processDesc.getProcessOutputs().getOutputArray(0).getComplexOutput().getDefault().getFormat().getMimeType();
			if(mimeType == null) {
				throw new IllegalArgumentException("Could not find mimeType for output: " + exec.getExecute().getIdentifier().getStringValue());
			}
		}
		IParser parser = StaticDataHandlerRepository.getParserFactory().getParser(schema, mimeType, IOHandler.DEFAULT_ENCODING, GTVectorDataBinding.class);
		if(parser instanceof AbstractXMLParser) {
			AbstractXMLParser xmlParser = (AbstractXMLParser) parser;
			return xmlParser.parseXML((InputStream)response);
		}
		if(parser instanceof AbstractBinaryParser) {
			AbstractBinaryParser binaryParser = (AbstractBinaryParser) parser;
			return binaryParser.parse((InputStream)response);
		}
		// parser is not of type AbstractXMLParser
		return null;
	}
}
