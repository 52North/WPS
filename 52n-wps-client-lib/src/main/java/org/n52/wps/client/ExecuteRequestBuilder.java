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

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.xml.AbstractXMLGenerator;
import org.w3c.dom.Node;

/*
 * ExecuteRequestBuilder support WPS version 1.0.0
 * @author foerster
 * TODO: this does not handle referenced datasets
 */

public class ExecuteRequestBuilder {
	ProcessDescriptionType processDesc;
	ExecuteDocument execute;
	String SUPPORTED_VERSION = "1.0.0";

	public ExecuteRequestBuilder(ProcessDescriptionType processDesc) {
		this.processDesc = processDesc;
		execute = ExecuteDocument.Factory.newInstance();
		Execute ex = execute.addNewExecute();
		ex.setVersion(SUPPORTED_VERSION);
		ex.addNewIdentifier().setStringValue(processDesc.getIdentifier().getStringValue());
		ex.addNewDataInputs();
	}
	
	public void addComplexData(String parameterID, Object value) {
		GeneratorFactory fac = StaticDataHandlerRepository.getGeneratorFactory();
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if(inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if(inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type ComplexData: " + parameterID);			
		}
		String schemaURL = inputDesc.getComplexData().getDefault().getFormat().getSchema();
		IGenerator generator = fac.getGenerator(schemaURL, IOHandler.DEFAULT_MIMETYPE, IOHandler.DEFAULT_ENCODING);
		if(generator instanceof AbstractXMLGenerator) {
			AbstractXMLGenerator xmlGenerator = (AbstractXMLGenerator) generator;
			Node node = xmlGenerator.generateXML(value, null);
			InputType input = execute.getExecute().getDataInputs().addNewInput();
			input.addNewIdentifier().setStringValue(inputDesc.getIdentifier().getStringValue());
			try {
				input.addNewData().addNewComplexData().set(XmlObject.Factory.parse(node));
			}
			catch(XmlException e) {
				throw new IllegalArgumentException("problem inserting node into execute request", e);
			}
		}
	}
	
	public void addLiteralData(String parameterID, String value) {
		InputDescriptionType inputDesc = this.getParameterDescription(parameterID);
		if(inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if(inputDesc.getLiteralData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type literalData: " + parameterID);			
		}
		String reference = inputDesc.getLiteralData().getDataType().getReference();
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewData().addNewLiteralData().setStringValue(value);
		input.getData().getLiteralData().setDataType(reference);
	}
	/**
	 * this sets the complexdataReference, if the process description also refers to this schema:
	 * http://schemas.opengis.net/gml/2.1.2/feature.xsd
	 * @param parameterID
	 * @param value
	 */
	public void addComplexDataReference(String parameterID, String value) {
		String supportedSchema = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if(inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if(inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type complexData: " + parameterID);
		}
		boolean isSupportedSchema = false;
		if(inputDesc.getComplexData().getDefault().getFormat().getSchema().equals(supportedSchema)){
			isSupportedSchema = true;
		}
		if(inputDesc.getComplexData().getSupported() != null && !isSupportedSchema) {
			for(ComplexDataDescriptionType dataDesc: inputDesc.getComplexData().getSupported().getFormatArray()) {
				if(!isSupportedSchema && dataDesc.getSchema().equals(supportedSchema)) {
					isSupportedSchema = true;
				}
			}
		}
		if(!isSupportedSchema) {
			throw new IllegalArgumentException("complexparameter does not support the default dataEncoding GML2");
		}
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewReference().setHref(value);
		input.getReference().setSchema(supportedSchema);
	}
	
	/** 
	 * checks, if the execute, which has been build is valid according to the process description.
	 * @return
	 */
	public boolean isExecuteValid() {
		return true;
	}
	
	
	public ExecuteDocument getExecute() {
		return execute;
	}
	/**
	 * 
	 * @param id
	 * @return the specified parameterdescription. if not available it returns null.
	 */
	private InputDescriptionType getParameterDescription(String id) {
		InputDescriptionType[] inputDescs = processDesc.getDataInputs().getInputArray();
		for(InputDescriptionType inputDesc : inputDescs) {
			if(inputDesc.getIdentifier().getStringValue().equals(id))
			{
				return inputDesc;
			}
		}
		return null;
	}
}
