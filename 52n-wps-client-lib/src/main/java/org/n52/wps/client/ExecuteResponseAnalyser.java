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

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.xml.AbstractXMLParser;

/*
 * 
 * returns the processOutputs according to the encoding of the process.
 * @author foerster
 *
 */
public class ExecuteResponseAnalyser {
	
	ExecuteResponseDocument response;
	ProcessDescriptionType processDesc;
	public ExecuteResponseAnalyser(ExecuteResponseDocument response, ProcessDescriptionType processDesc) {
		this.response = response;
		this.processDesc= processDesc;
	}
	
	public Object getComplexData(String name) {
		OutputDataType[] outputs = response.getExecuteResponse().getProcessOutputs().getOutputArray();
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
		OutputDataType[] outputs = response.getExecuteResponse().getProcessOutputs().getOutputArray();
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
	
	private Object parseProcessOutput(OutputDataType output) {
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
		IParser parser = StaticDataHandlerRepository.getParserFactory().getParser(schemaURL, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING);
		if(parser instanceof AbstractXMLParser) {
			AbstractXMLParser xmlParser = (AbstractXMLParser) parser;
			return xmlParser.parseXML(output.getData().getComplexData().newInputStream());
		}
		// parser is not of type AbstractXMLParser
		return null;
	}
}
