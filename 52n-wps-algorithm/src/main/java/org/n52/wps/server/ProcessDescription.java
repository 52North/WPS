/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x20.ValueType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x200.ComplexDataDocument;
import net.opengis.wps.x200.ComplexDataType;
import net.opengis.wps.x200.FormatDocument.Format;
import net.opengis.wps.x200.LiteralDataDocument;
import net.opengis.wps.x200.LiteralDataDomainType;
import net.opengis.wps.x200.LiteralDataType;
import net.opengis.wps.x200.ProcessOfferingDocument.ProcessOffering;

import org.apache.xmlbeans.XmlObject;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;

import com.google.common.base.Strings;

/**
 * TODO: javadoc
 * 
 * @author Benjamin Pross
 *
 */
public class ProcessDescription {

	private Map<String, XmlObject> versionDescriptionTypeMap = new HashMap<String, XmlObject>();

	public XmlObject getProcessDescriptionType(String version) {
		
		XmlObject result = versionDescriptionTypeMap.get(version);
		
		if(result == null && version.equals(WPSConfig.VERSION_200)){
			ProcessDescriptionType processDescriptionV100 = (ProcessDescriptionType) versionDescriptionTypeMap.get(WPSConfig.VERSION_100);
			if(processDescriptionV100 != null){
			    result = createProcessDescriptionV200fromV100(processDescriptionV100);
			}
		}
		return result;
	}

	public void addProcessDescriptionForVersion(XmlObject processDescription, String version){
		versionDescriptionTypeMap.put(version, processDescription);		
	}
	
	public ProcessOffering createProcessDescriptionV200fromV100(ProcessDescriptionType processDescriptionV100){
		
		ProcessOffering processOffering = ProcessOffering.Factory.newInstance();
		
    	net.opengis.wps.x200.ProcessDescriptionType processDescription = processOffering.addNewProcess();
    	
        	processOffering.setProcessVersion(processDescriptionV100.getProcessVersion());
        	
        	//TODO check options
        	List<String> jobControlOptions = new ArrayList<>();
        	
        	jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_SYNC_EXECUTE);
        	
        	if(processDescriptionV100.getStatusSupported()){
        		jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_ASYNC_EXECUTE);
        	}
        	
        	processOffering.setJobControlOptions(jobControlOptions);
        	
        	List<String> outputTransmissionModes = new ArrayList<>();
        	
        	outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_VALUE);
        	outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_REFERENCE);
        	
        	processOffering.setOutputTransmission(outputTransmissionModes);
            // 1. Identifier
            processDescription.addNewIdentifier().setStringValue(processDescriptionV100.getIdentifier().getStringValue());
            processDescription.addNewTitle().setStringValue(processDescriptionV100.getTitle() != null ?
            		processDescriptionV100.getTitle().getStringValue() :
            			processDescriptionV100.getIdentifier().getStringValue());
            if (processDescriptionV100.getAbstract() != null) {
                processDescription.addNewAbstract().setStringValue(processDescriptionV100.getAbstract().getStringValue());
            }

            InputDescriptionType[] inputDescriptionTypes = processDescriptionV100.getDataInputs().getInputArray();
            
            for (InputDescriptionType inputDescriptionType : inputDescriptionTypes) {
				
                net.opengis.wps.x200.InputDescriptionType dataInput = processDescription.addNewInput();
                dataInput.setMinOccurs(inputDescriptionType.getMinOccurs());
                dataInput.setMaxOccurs(inputDescriptionType.getMaxOccurs());

                dataInput.addNewIdentifier().setStringValue(inputDescriptionType.getIdentifier().getStringValue());
                dataInput.addNewTitle().setStringValue( inputDescriptionType.getTitle() != null ?
                		inputDescriptionType.getTitle().getStringValue() :
                			inputDescriptionType.getIdentifier().getStringValue());
                if (inputDescriptionType.getAbstract() != null) {
                    dataInput.addNewAbstract().setStringValue(inputDescriptionType.getAbstract().getStringValue());
                }

                if (inputDescriptionType.getLiteralData() != null) {
                    LiteralInputType literalInputType = inputDescriptionType.getLiteralData();
                    
                    LiteralDataType literalData = LiteralDataType.Factory.newInstance();
                    
                    net.opengis.wps.x200.FormatDocument.Format defaultFormat =  literalData.addNewFormat();
                    
                    defaultFormat.setDefault(true);
                    
                    defaultFormat.setMimeType("text/plain");
                    
                    net.opengis.wps.x200.FormatDocument.Format textXMLFormat =  literalData.addNewFormat();
                    
                    textXMLFormat.setMimeType("text/xml");
                    
                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();
                    
                    literalDataDomainType.addNewDataType().setReference(literalInputType.getDataType().getStringValue());

                    if (literalInputType.getDefaultValue() != null) {
                    	
                    	ValueType defaultValue = ValueType.Factory.newInstance();
                    	
                    	defaultValue.setStringValue(literalInputType.getDefaultValue());
                    	
                    	literalDataDomainType.setDefaultValue(defaultValue);
                    }
                    if (literalInputType.getAllowedValues() != null) {
                        net.opengis.ows.x20.AllowedValuesDocument.AllowedValues allowed = literalDataDomainType.addNewAllowedValues();
                        for (net.opengis.ows.x11.ValueType allowedValue : literalInputType.getAllowedValues().getValueArray()) {
                            allowed.addNewValue().setStringValue(allowedValue.getStringValue());
                        }
                    } else {
                    	literalDataDomainType.addNewAnyValue();
                    }
                    
                    dataInput.setDataDescription(literalData);
                    
                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), LiteralDataDocument.type.getDocumentElementName(), null);

                } else if (inputDescriptionType.getComplexData() != null) {
                	
                	ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();  

                    transformComplexDataFromV100ToV200(complexDataType, inputDescriptionType.getComplexData());
                    
                    dataInput.setDataDescription(complexDataType);
                    
                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), ComplexDataDocument.type.getDocumentElementName(), null);
                }
            	
			}

            // 3. Outputs            
            OutputDescriptionType[] outputDescriptions = processDescriptionV100.getProcessOutputs().getOutputArray();

            for (OutputDescriptionType outputDescription : outputDescriptions) {

                net.opengis.wps.x200.OutputDescriptionType dataOutput = processDescription.addNewOutput();
                dataOutput.addNewIdentifier().setStringValue(outputDescription.getIdentifier().getStringValue());
                dataOutput.addNewTitle().setStringValue( outputDescription.getTitle() != null ?
                		outputDescription.getTitle().getStringValue() :
                			outputDescription.getIdentifier().getStringValue());
                if (outputDescription.getAbstract() != null) {
                    dataOutput.addNewAbstract().setStringValue(outputDescription.getAbstract().getStringValue());
                }

                if (outputDescription.getLiteralOutput() != null) {
                    LiteralOutputType literalOutputType = outputDescription.getLiteralOutput();
                    
                    LiteralDataType literalData = LiteralDataType.Factory.newInstance(); 
                    
                    net.opengis.wps.x200.FormatDocument.Format defaultFormat =  literalData.addNewFormat();
                    
                    defaultFormat.setDefault(true);
                    
                    defaultFormat.setMimeType("text/plain");
                    
                    net.opengis.wps.x200.FormatDocument.Format textXMLFormat =  literalData.addNewFormat();
                    
                    textXMLFormat.setMimeType("text/xml");
                    
                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();
                    
                    literalDataDomainType.addNewDataType().setReference(literalOutputType.getDataType().getStringValue());
                	
                    literalDataDomainType.addNewAnyValue();
                    
                    dataOutput.setDataDescription(literalData);
                    
                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), LiteralDataDocument.type.getDocumentElementName(), null);
                    
                } else if (outputDescription.getComplexOutput() != null) {
                	
                	ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();  
                	
                	transformComplexOutputDataFromV100ToV200(complexDataType, outputDescription.getComplexOutput());
                    
                    dataOutput.setDataDescription(complexDataType);
                    
                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), ComplexDataDocument.type.getDocumentElementName(), null);
                }
            }
	
	    return processOffering;
    }

	private void transformComplexOutputDataFromV100ToV200(
			ComplexDataType complexDataType,
			SupportedComplexDataType complexData) {
		
		ComplexDataCombinationType defaultFormat = complexData.getDefault();
		
		Format defaultFormatV200 = complexDataType.addNewFormat();
		
		defaultFormatV200.setDefault(true);
		
		describeComplexDataFormat200(defaultFormatV200, defaultFormat.getFormat().getMimeType(), defaultFormat.getFormat().getEncoding(), defaultFormat.getFormat().getSchema());
		
		ComplexDataDescriptionType[] supportedFormats = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {
			Format supportedFormat = complexDataType.addNewFormat();
			describeComplexDataFormat200(supportedFormat, complexDataDescriptionType.getMimeType(), complexDataDescriptionType.getEncoding(), complexDataDescriptionType.getSchema());
		}
	}

	private void transformComplexDataFromV100ToV200(
			ComplexDataType complexDataType,
			SupportedComplexDataInputType complexData) {
		
		ComplexDataCombinationType defaultFormat = complexData.getDefault();
		
		Format defaultFormatV200 = complexDataType.addNewFormat();
		
		defaultFormatV200.setDefault(true);
		
		describeComplexDataFormat200(defaultFormatV200, defaultFormat.getFormat().getMimeType(), defaultFormat.getFormat().getEncoding(), defaultFormat.getFormat().getSchema());
		
		ComplexDataDescriptionType[] supportedFormats = complexData.getSupported().getFormatArray();
		
		for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {
			Format supportedFormat = complexDataType.addNewFormat();
			describeComplexDataFormat200(supportedFormat, complexDataDescriptionType.getMimeType(), complexDataDescriptionType.getEncoding(), complexDataDescriptionType.getSchema());
		}
	}
    
    private void describeComplexDataFormat200(Format supportedFormatType,
    		String format,
    		String encoding,
    		String schema) {
    	if ( !Strings.isNullOrEmpty(format)) {
    		supportedFormatType.setMimeType(format);
    	}
    	if ( !Strings.isNullOrEmpty(encoding)) {
    		supportedFormatType.setEncoding(encoding);
    	}
    	if ( !Strings.isNullOrEmpty(schema)) {
    		supportedFormatType.setSchema(schema);
    	}
    }
	
	
}
