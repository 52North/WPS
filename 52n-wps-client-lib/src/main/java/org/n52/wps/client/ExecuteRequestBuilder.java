/*****************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.client;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.w3c.dom.Node;
/**
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
	
	public ExecuteRequestBuilder(ProcessDescriptionType processDesc, ExecuteDocument execute) {
		this.processDesc = processDesc;
		this.execute = execute;
	}
	
	public void addComplexData(String parameterID, IData value, String schema, String encoding, String mimeType) {
		GeneratorFactory fac = StaticDataHandlerRepository.getGeneratorFactory();
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type ComplexData: " + parameterID);			
		}
		
			

		IGenerator generator = fac.getGenerator(schema, mimeType, encoding,
				value.getClass());

		if (generator == null) {
			for (ComplexDataDescriptionType dataDescType : inputDesc
					.getComplexData().getSupported().getFormatArray()) {
				schema = dataDescType.getSchema();
				mimeType = dataDescType.getMimeType();
				encoding = dataDescType.getEncoding();
				generator = fac.getGenerator(schema, mimeType, encoding,
						value.getClass());
				if (generator != null) {
					break;
				}
			}
		}
		if (generator == null) {
			// generator is still null
			throw new IllegalArgumentException("problem finding appropriate generator for parameter: " + parameterID);
		}

		if (generator instanceof AbstractXMLGenerator) {
			AbstractXMLGenerator xmlGenerator = (AbstractXMLGenerator) generator;
			Node node = xmlGenerator.generateXML(value, null);
			InputType input = execute.getExecute().getDataInputs().addNewInput();
			input.addNewIdentifier().setStringValue(inputDesc.getIdentifier().getStringValue());

			try {
				ComplexDataType data = input.addNewData().addNewComplexData();
				data.set(XmlObject.Factory.parse(node));
				if (schema != null) {
					data.setSchema(schema);
				}
				if (mimeType != null) {
					data.setMimeType(mimeType);
				}
				if (encoding != null) {
					data.setEncoding(encoding);
				}
			}
			catch(XmlException e) {
				throw new IllegalArgumentException("problem inserting node into execute request", e);
			}
		}
	}

	public void addLiteralData(String parameterID, String value) {
		InputDescriptionType inputDesc = this.getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getLiteralData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type literalData: " + parameterID);			
		}
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewData().addNewLiteralData().setStringValue(value);
		DomainMetadataType dataType = inputDesc.getLiteralData().getDataType();
		if (dataType != null) {
			input.getData().getLiteralData().setDataType(dataType.getReference());
		}
	}

	/**
	 * this sets the complexdataReference, if the process description also refers to this schema:
	 * http://schemas.opengis.net/gml/2.1.2/feature.xsd
	 * @param parameterID
	 * @param value
	 */
	public void addComplexDataReference(String parameterID, String value, String schema, String encoding, String mimetype) {
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type complexData: " + parameterID);
		}
			
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewReference().setHref(value);
		if (schema != null) {
			input.getReference().setSchema(schema);
		}

		if (encoding != null) {
			input.getReference().setEncoding(encoding);
		}
		if (mimetype != null) {
			input.getReference().setMimeType(mimetype);
		}
	}

	/**
	 * checks, if the execute, which has been build is valid according to the process description.
	 * @return
	 */
	public boolean isExecuteValid() {
		return true;
	}

	/**
	 * this sets store for the specific output.
	 * @param parentInput
	 * @return
	 */
	public boolean setStoreSupport(String outputName) {
		DocumentOutputDefinitionType outputDef = null;
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		for(DocumentOutputDefinitionType outputDefTemp: execute.getExecute().getResponseForm().getResponseDocument().getOutputArray()) {
			if(outputDefTemp.getIdentifier().getStringValue().equals(outputName)) {
				outputDef = outputDefTemp;
				break;
			}
		}
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
		}
		for (OutputDescriptionType outputDesc : processDesc.getProcessOutputs().getOutputArray()) {
			if (outputDesc.getIdentifier().getStringValue().equals(outputName)) {
				outputDef.setIdentifier(outputDesc.getIdentifier());
				ComplexDataDescriptionType format = outputDesc.getComplexOutput().getDefault().getFormat();
				if (format.getMimeType() != null) {
					outputDef.setMimeType(format.getMimeType());
				}
				if (format.getEncoding() != null) {
					outputDef.setEncoding(format.getEncoding());
				}
				if (format.getSchema() != null) {
					outputDef.setSchema(format.getSchema());
				}
				outputDef.setAsReference(true);
			}
		}
		return true;
	}

	public boolean setSchemaForOutput(String schema, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}
		String defaultSchema = outputDesc.getComplexOutput().getDefault()
				.getFormat().getSchema();
		if ((defaultSchema != null && defaultSchema.equals(schema))
				|| (defaultSchema == null && schema == null)) {
			return true;
		} else {
			for (ComplexDataDescriptionType data : outputDesc
					.getComplexOutput().getSupported().getFormatArray()) {
				if (data.getSchema() != null && data.getSchema().equals(schema)) {
					outputDef.setSchema(schema);
					return true;
				} else if ((data.getSchema() == null && schema == null)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean setMimeTypeForOutput(String mimeType, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}

		String defaultMimeType = outputDesc.getComplexOutput().getDefault()
				.getFormat().getMimeType();
		if (defaultMimeType == null) {
			defaultMimeType = IOHandler.DEFAULT_MIMETYPE;
		}
		if (defaultMimeType.equals(mimeType)) {
			return true;
		} else {
			for (ComplexDataDescriptionType data : outputDesc
					.getComplexOutput().getSupported().getFormatArray()) {
				String m = data.getMimeType();
				if (m != null && m.equals(mimeType)) {
					outputDef.setMimeType(mimeType);
					return true;
				}
			}
		}

		return false;
	}

	public boolean setEncodingForOutput(String encoding, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}

		String defaultEncoding = outputDesc.getComplexOutput().getDefault()
				.getFormat().getEncoding();
		if (defaultEncoding == null) {
			defaultEncoding = IOHandler.DEFAULT_ENCODING;
		}
		if (defaultEncoding.equals(encoding)) {
			return true;
		} else {
			ComplexDataDescriptionType[] supportedFormats = outputDesc
					.getComplexOutput().getSupported().getFormatArray();
			for (ComplexDataDescriptionType data : supportedFormats) {
				String e = data.getEncoding();
				if (e != null && e.equals(encoding)) {
					outputDef.setEncoding(encoding);
					return true;
				}
			}
		}

		return false;
	}

	private OutputDescriptionType getOutputDescription(String outputName) {
		for (OutputDescriptionType outputDesc : processDesc.getProcessOutputs()
				.getOutputArray()) {
			if (outputDesc.getIdentifier().getStringValue().equals(outputName)) {
				return outputDesc;
			}
		}

		return null;
	}

	private DocumentOutputDefinitionType getOutputDefinition(String outputName) {
		DocumentOutputDefinitionType[] outputs = execute.getExecute()
				.getResponseForm().getResponseDocument().getOutputArray();
		for (DocumentOutputDefinitionType outputDef : outputs) {
			if (outputDef.getIdentifier().getStringValue().equals(outputName)) {
				return outputDef;
			}
		}

		return null;
	}

	public boolean setRawData(String schema, String encoding, String mimeType) {
		if (processDesc.getProcessOutputs().getOutputArray().length != 1) {
			return false;
		}
		OutputDefinitionType output = execute.getExecute().addNewResponseForm().addNewRawDataOutput();
		ComplexDataDescriptionType complexDesc = processDesc.getProcessOutputs().getOutputArray(0).getComplexOutput().getDefault().getFormat();
		output.setIdentifier(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier());
		
		if (schema != null) {
			output.setSchema(schema);
		}
		if (mimeType != null) {
			output.setMimeType(mimeType);
		}
		if (encoding != null) {
			output.setEncoding(encoding);
		}
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
		for (InputDescriptionType inputDesc : inputDescs) {
			if(inputDesc.getIdentifier().getStringValue().equals(id))
			{
				return inputDesc;
			}
		}
		return null;
	}
}
