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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x20.ValueType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x20.ComplexDataDocument;
import net.opengis.wps.x20.ComplexDataType;
import net.opengis.wps.x20.LiteralDataDocument;
import net.opengis.wps.x20.LiteralDataDomainType;
import net.opengis.wps.x20.LiteralDataType;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.wps.algorithm.descriptor.AlgorithmDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.InputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import org.n52.wps.algorithm.descriptor.LiteralDataOutputDescriptor;
import org.n52.wps.algorithm.descriptor.OutputDescriptor;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.webapp.api.FormatEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public abstract class AbstractDescriptorAlgorithm implements IAlgorithm, ISubject {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractDescriptorAlgorithm.class);
    
    private AlgorithmDescriptor descriptor;
    private ProcessDescription description;
    
    public AbstractDescriptorAlgorithm() {
        super();
    }

    @Override
    public synchronized ProcessDescription getDescription() {
        if (description == null) {
            description = createProcessDescription();
        }
        return description;
    }

    @Override
    public String getWellKnownName() {
        return getAlgorithmDescriptor().getIdentifier();
    }

    private ProcessDescription createProcessDescription() {

        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();

		ProcessDescription superProcessDescription = new ProcessDescription();
		
		superProcessDescription.addProcessDescriptionForVersion(createProcessDescription100(algorithmDescriptor), WPSConfig.VERSION_100);
		
		superProcessDescription.addProcessDescriptionForVersion(createProcessDescription200(algorithmDescriptor), WPSConfig.VERSION_200);
		
		return superProcessDescription;
    }

    private void describeComplexDataInputType(SupportedComplexDataType complexData, Class dataTypeClass) {
        List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
        List<IParser> foundParsers = new ArrayList<IParser>();
        for (IParser parser : parsers) {
// /*2.0*/    Class[] supportedClasses = parser.getSupportedInternalOutputDataType();
 /*3.0*/    Class[] supportedClasses = parser.getSupportedDataBindings();
            for (Class clazz : supportedClasses) {
                if (dataTypeClass.isAssignableFrom(clazz)) {
                    foundParsers.add(parser);
                }
            }
        }
        describeComplexDataType(complexData, foundParsers);
    }

    private void describeComplexDataOutputType(SupportedComplexDataType complexData, Class dataTypeClass) {

        List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
        List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
        for (IGenerator generator : generators) {
// /*2.0*/    Class[] supportedClasses = generator.getSupportedInternalInputDataType(); // appears to have been removed in 52n WPS 3.0
 /*3.0*/    Class[] supportedClasses = generator.getSupportedDataBindings();
            for (Class clazz : supportedClasses) {
                if (clazz.isAssignableFrom(dataTypeClass)) {
                    foundGenerators.add(generator);
                }
            }
        }
        describeComplexDataType(complexData, foundGenerators);
    }

    private void describeComplexDataType(
            SupportedComplexDataType complexData,
            List<? extends IOHandler> handlers)
    {
        ComplexDataCombinationType defaultFormatType = complexData.addNewDefault();
        ComplexDataCombinationsType supportedFormatType = complexData.addNewSupported();

        boolean needDefault = true;
        for (IOHandler handler : handlers) {
            
            List<FormatEntry> fullFormats = handler.getSupportedFullFormats();
            if (fullFormats != null && fullFormats.size() > 0) {
                if (needDefault) {
                    needDefault = false;
                    describeComplexDataFormat(
                            defaultFormatType.addNewFormat(),
                            fullFormats.get(0));
                }
                for (int formatIndex = 0, formatCount = fullFormats.size(); formatIndex < formatCount; ++formatIndex) {
                    describeComplexDataFormat(
                            supportedFormatType.addNewFormat(),
                            fullFormats.get(formatIndex));
                }
            } else {
                
                String[] formats = handler.getSupportedFormats();
                
                if (formats == null || formats.length == 0) {
                    LOGGER.warn("Skipping IOHandler {} in ProcessDescription generation for {}, no formats specified",
                            handler.getClass().getSimpleName(),
                            getWellKnownName());
                } else {
                    // if formats, encodings or schemas arrays are 'null' or empty, create
                    // new array with single 'null' element.  We do this so we can utilize
                    // a single set of nested loops to process all permutations.  'null'
                    // values will not be output...
                    String[] encodings = handler.getSupportedEncodings();
                    if (encodings == null || encodings.length == 0) {
                        encodings = new String[] { null };
                    }
                    String[] schemas = handler.getSupportedSchemas();
                    if (schemas == null || schemas.length == 0) {
                        schemas = new String[] { null };
                    }

                    for (String format : formats) {
                        for (String encoding : encodings) {
                            for (String schema : schemas) {
                                if (needDefault) {
                                    needDefault = false;
                                    describeComplexDataFormat(
                                            defaultFormatType.addNewFormat(),
                                            format, encoding, schema);
                                }
                                describeComplexDataFormat(
                                        supportedFormatType.addNewFormat(),
                                        format, encoding, schema);
                            }
                        }
                    }
                }
            }
        }
    }

    private void describeComplexDataFormat(
            ComplexDataDescriptionType description,
            FormatEntry format)
    {
        describeComplexDataFormat(description,
                format.getMimeType(),
                format.getEncoding(),
                format.getSchema());
    }
    
    private void describeComplexDataFormat(ComplexDataDescriptionType description,
                                           String format,
                                           String encoding,
                                           String schema) {
        if ( !Strings.isNullOrEmpty(format)) {
            description.setMimeType(format);
        }
        if ( !Strings.isNullOrEmpty(encoding)) {
            description.setEncoding(encoding);
        }
        if ( !Strings.isNullOrEmpty(schema)) {
            description.setSchema(schema);
        }
    }
    
    private void describeComplexDataInputType200(ComplexDataType complexDataType, Class dataTypeClass) {
    	List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
    	List<IParser> foundParsers = new ArrayList<IParser>();
    	for (IParser parser : parsers) {
// /*2.0*/    Class[] supportedClasses = parser.getSupportedInternalOutputDataType();
    		/*3.0*/    Class[] supportedClasses = parser.getSupportedDataBindings();
    		for (Class clazz : supportedClasses) {
    			if (dataTypeClass.isAssignableFrom(clazz)) {
    				foundParsers.add(parser);
    			}
    		}
    	}
    	describeComplexDataType200(complexDataType, foundParsers);
    }
    
    private void describeComplexDataOutputType200(ComplexDataType complexData, Class dataTypeClass) {
    	
    	List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
    	List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
    	for (IGenerator generator : generators) {
// /*2.0*/    Class[] supportedClasses = generator.getSupportedInternalInputDataType(); // appears to have been removed in 52n WPS 3.0
    		/*3.0*/    Class[] supportedClasses = generator.getSupportedDataBindings();
    		for (Class clazz : supportedClasses) {
    			if (clazz.isAssignableFrom(dataTypeClass)) {
    				foundGenerators.add(generator);
    			}
    		}
    	}
    	describeComplexDataType200(complexData, foundGenerators);
    }
    
    private void describeComplexDataType200(
    		ComplexDataType complexDataType,
    		List<? extends IOHandler> handlers)
    {
    	net.opengis.wps.x20.FormatDocument.Format defaultFormatType = complexDataType.addNewFormat();
    	
    	defaultFormatType.setDefault(true);
    	
    	boolean needDefault = true;
    	for (IOHandler handler : handlers) {
    		
    		List<FormatEntry> fullFormats = handler.getSupportedFullFormats();
    		if (fullFormats != null && fullFormats.size() > 0) {
    			if (needDefault) {
    				needDefault = false;
    				describeComplexDataFormat200(
    						defaultFormatType,
    						fullFormats.get(0));
    			}
    			for (int formatIndex = 0, formatCount = fullFormats.size(); formatIndex < formatCount; ++formatIndex) {
    				describeComplexDataFormat200(
    						complexDataType.addNewFormat(),
    						fullFormats.get(formatIndex));
    			}
    		} else {
    			
    			String[] formats = handler.getSupportedFormats();
    			
    			if (formats == null || formats.length == 0) {
    				LOGGER.warn("Skipping IOHandler {} in ProcessDescription generation for {}, no formats specified",
    						handler.getClass().getSimpleName(),
    						getWellKnownName());
    			} else {
    				// if formats, encodings or schemas arrays are 'null' or empty, create
    				// new array with single 'null' element.  We do this so we can utilize
    				// a single set of nested loops to process all permutations.  'null'
    				// values will not be output...
    				String[] encodings = handler.getSupportedEncodings();
    				if (encodings == null || encodings.length == 0) {
    					encodings = new String[] { null };
    				}
    				String[] schemas = handler.getSupportedSchemas();
    				if (schemas == null || schemas.length == 0) {
    					schemas = new String[] { null };
    				}
    				
    				for (String format : formats) {
    					for (String encoding : encodings) {
    						for (String schema : schemas) {
    							if (needDefault) {
    								needDefault = false;
    								describeComplexDataFormat200(
    										defaultFormatType,
    										format, encoding, schema);
    							}
    							describeComplexDataFormat200(
    									complexDataType.addNewFormat(),
    									format, encoding, schema);
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    private void describeComplexDataFormat200(
    		net.opengis.wps.x20.FormatDocument.Format supportedFormatType,
    		FormatEntry format)
    {
    	describeComplexDataFormat200(supportedFormatType,
    			format.getMimeType(),
    			format.getEncoding(),
    			format.getSchema());
    }
    
    private void describeComplexDataFormat200(net.opengis.wps.x20.FormatDocument.Format supportedFormatType,
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

    @Override
    public boolean processDescriptionIsValid(String version) {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(xmlValidationErrorList);
        boolean valid = getDescription().getProcessDescriptionType(version).validate(xmlOptions);
        if (!valid) {
            LOGGER.error("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                LOGGER.error("\tMessage: {}", xmlValidationError.getMessage());
                LOGGER.error("\tLocation of invalid XML: {}",
                     xmlValidationError.getCursorLocation().xmlText());
            }
        }
        return valid;
    }

    protected final synchronized AlgorithmDescriptor getAlgorithmDescriptor() {
        if (descriptor == null) {
            descriptor = createAlgorithmDescriptor();
        }
        return descriptor;
    }
    
    protected abstract AlgorithmDescriptor createAlgorithmDescriptor();
    
    private ProcessDescriptionType createProcessDescription100(AlgorithmDescriptor algorithmDescriptor){
    	
        ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
        ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
        ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();

        if (algorithmDescriptor == null) {
            throw new IllegalStateException("Instance must have an algorithm descriptor");
        } else {

            // 1. Identifier
            processDescription.setStatusSupported(algorithmDescriptor.getStatusSupported());
            processDescription.setStoreSupported(algorithmDescriptor.getStoreSupported());
            processDescription.setProcessVersion(algorithmDescriptor.getVersion());
            processDescription.addNewIdentifier().setStringValue(algorithmDescriptor.getIdentifier());
            processDescription.addNewTitle().setStringValue( algorithmDescriptor.hasTitle() ?
                    algorithmDescriptor.getTitle() :
                    algorithmDescriptor.getIdentifier());
            if (algorithmDescriptor.hasAbstract()) {
                processDescription.addNewAbstract().setStringValue(algorithmDescriptor.getAbstract());
            }

            // 2. Inputs
            Collection<InputDescriptor> inputDescriptors = algorithmDescriptor.getInputDescriptors();
            DataInputs dataInputs = null;
            if (inputDescriptors.size() > 0) {
                dataInputs = processDescription.addNewDataInputs();
            }
            for (InputDescriptor inputDescriptor : inputDescriptors) {

                InputDescriptionType dataInput = dataInputs.addNewInput();
                dataInput.setMinOccurs(inputDescriptor.getMinOccurs());
                dataInput.setMaxOccurs(inputDescriptor.getMaxOccurs());

                dataInput.addNewIdentifier().setStringValue(inputDescriptor.getIdentifier());
                dataInput.addNewTitle().setStringValue( inputDescriptor.hasTitle() ?
                        inputDescriptor.getTitle() :
                        inputDescriptor.getIdentifier());
                if (inputDescriptor.hasAbstract()) {
                    dataInput.addNewAbstract().setStringValue(inputDescriptor.getAbstract());
                }

                if (inputDescriptor instanceof LiteralDataInputDescriptor) {
                    LiteralDataInputDescriptor<?> literalDescriptor = (LiteralDataInputDescriptor)inputDescriptor;

                    LiteralInputType literalData = dataInput.addNewLiteralData();
                    literalData.addNewDataType().setReference(literalDescriptor.getDataType());

                    if (literalDescriptor.hasDefaultValue()) {
                        literalData.setDefaultValue(literalDescriptor.getDefaultValue());
                    }
                    if (literalDescriptor.hasAllowedValues()) {
                        AllowedValues allowed = literalData.addNewAllowedValues();
                        for (String allowedValue : literalDescriptor.getAllowedValues()) {
                            allowed.addNewValue().setStringValue(allowedValue);
                        }
                    } else {
                        literalData.addNewAnyValue();
                    }

                } else if (inputDescriptor instanceof ComplexDataInputDescriptor) {
                    SupportedComplexDataInputType complexDataType = dataInput.addNewComplexData();
                    ComplexDataInputDescriptor complexInputDescriptor =
                            (ComplexDataInputDescriptor)inputDescriptor;
                    if (complexInputDescriptor.hasMaximumMegaBytes()) {
                        complexDataType.setMaximumMegabytes(complexInputDescriptor.getMaximumMegaBytes());
                    }
                    describeComplexDataInputType(complexDataType, inputDescriptor.getBinding());
                }
            }

            // 3. Outputs
            ProcessOutputs dataOutputs = processDescription.addNewProcessOutputs();
            Collection<OutputDescriptor> outputDescriptors = algorithmDescriptor.getOutputDescriptors();
            if (outputDescriptors.size() < 1) {
               LOGGER.error("No outputs found for algorithm {}", algorithmDescriptor.getIdentifier());
            }
            for (OutputDescriptor outputDescriptor : outputDescriptors) {

                OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
                dataOutput.addNewIdentifier().setStringValue(outputDescriptor.getIdentifier());
                dataOutput.addNewTitle().setStringValue( outputDescriptor.hasTitle() ?
                        outputDescriptor.getTitle() :
                        outputDescriptor.getIdentifier());
                if (outputDescriptor.hasAbstract()) {
                    dataOutput.addNewAbstract().setStringValue(outputDescriptor.getAbstract());
                }

                if (outputDescriptor instanceof LiteralDataOutputDescriptor) {
                    LiteralDataOutputDescriptor<?> literalDescriptor = (LiteralDataOutputDescriptor)outputDescriptor;
                    dataOutput.addNewLiteralOutput().addNewDataType().
                            setReference(literalDescriptor.getDataType());
                } else if (outputDescriptor instanceof ComplexDataOutputDescriptor) {
                    describeComplexDataOutputType(dataOutput.addNewComplexOutput(), outputDescriptor.getBinding());
               }
            }
        }
        
        return processDescription;
    	
    }

    private ProcessOffering createProcessDescription200(AlgorithmDescriptor algorithmDescriptor){
    	
    	/*
    	 * We need to use the ProcessOffering here, because it holds some information that will be 
    	 * shown in the ProcessSummary of the Capabilities
    	 */
    	ProcessOffering processOffering = ProcessOffering.Factory.newInstance();

    	net.opengis.wps.x20.ProcessDescriptionType processDescription = processOffering.addNewProcess();
    	
        if (algorithmDescriptor == null) {
            throw new IllegalStateException("Instance must have an algorithm descriptor");
        } else {

        	processOffering.setProcessVersion(algorithmDescriptor.getVersion());
        	
        	//TODO check options
        	List<String> jobControlOptions = new ArrayList<>();
        	
        	jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_SYNC_EXECUTE);
        	
        	if(algorithmDescriptor.getStatusSupported()){
        		jobControlOptions.add(WPSConfig.JOB_CONTROL_OPTION_ASYNC_EXECUTE);
        	}
        	
        	processOffering.setJobControlOptions(jobControlOptions);
        	
        	List<String> outputTransmissionModes = new ArrayList<>();
        	
        	outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_VALUE);
        	outputTransmissionModes.add(WPSConfig.OUTPUT_TRANSMISSION_REFERENCE);
        	
        	processOffering.setOutputTransmission(outputTransmissionModes);
            // 1. Identifier
            processDescription.addNewIdentifier().setStringValue(algorithmDescriptor.getIdentifier());
            processDescription.addNewTitle().setStringValue( algorithmDescriptor.hasTitle() ?
                    algorithmDescriptor.getTitle() :
                    algorithmDescriptor.getIdentifier());
            if (algorithmDescriptor.hasAbstract()) {
                processDescription.addNewAbstract().setStringValue(algorithmDescriptor.getAbstract());
            }

            // 2. Inputs
            Collection<InputDescriptor> inputDescriptors = algorithmDescriptor.getInputDescriptors();
            DataInputs dataInputs = null;
            for (InputDescriptor inputDescriptor : inputDescriptors) {

                net.opengis.wps.x20.InputDescriptionType dataInput = processDescription.addNewInput();
                dataInput.setMinOccurs(inputDescriptor.getMinOccurs());
                dataInput.setMaxOccurs(inputDescriptor.getMaxOccurs());

                dataInput.addNewIdentifier().setStringValue(inputDescriptor.getIdentifier());
                dataInput.addNewTitle().setStringValue( inputDescriptor.hasTitle() ?
                        inputDescriptor.getTitle() :
                        inputDescriptor.getIdentifier());
                if (inputDescriptor.hasAbstract()) {
                    dataInput.addNewAbstract().setStringValue(inputDescriptor.getAbstract());
                }

                if (inputDescriptor instanceof LiteralDataInputDescriptor) {
                    LiteralDataInputDescriptor<?> literalDescriptor = (LiteralDataInputDescriptor)inputDescriptor;

                    LiteralDataType literalData = LiteralDataType.Factory.newInstance();
                    
                    net.opengis.wps.x20.FormatDocument.Format defaultFormat =  literalData.addNewFormat();
                    
                    defaultFormat.setDefault(true);
                    
                    defaultFormat.setMimeType("text/plain");
                    
                    net.opengis.wps.x20.FormatDocument.Format textXMLFormat =  literalData.addNewFormat();
                    
                    textXMLFormat.setMimeType("text/xml");
                    
                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();
                    
                    literalDataDomainType.addNewDataType().setReference(literalDescriptor.getDataType());

                    if (literalDescriptor.hasDefaultValue()) {
                    	
                    	ValueType defaultValue = ValueType.Factory.newInstance();
                    	
                    	defaultValue.setStringValue(literalDescriptor.getDefaultValue());
                    	
                    	literalDataDomainType.setDefaultValue(defaultValue);
                    }
                    if (literalDescriptor.hasAllowedValues()) {
                        net.opengis.ows.x20.AllowedValuesDocument.AllowedValues allowed = literalDataDomainType.addNewAllowedValues();
                        for (String allowedValue : literalDescriptor.getAllowedValues()) {
                            allowed.addNewValue().setStringValue(allowedValue);
                        }
                    } else {
                    	literalDataDomainType.addNewAnyValue();
                    }
                    
                    dataInput.setDataDescription(literalData);
                    
                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), LiteralDataDocument.type.getDocumentElementName(), null);

                } else if (inputDescriptor instanceof ComplexDataInputDescriptor) {
                	
                	ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();  
                	
                    ComplexDataInputDescriptor complexInputDescriptor =
                            (ComplexDataInputDescriptor)inputDescriptor;
                    
                    //TODO this is now defined per format..
//                    if (complexInputDescriptor.hasMaximumMegaBytes()) {
//                        dataInput.setMaximumMegabytes(complexInputDescriptor.getMaximumMegaBytes());
//                    }
                    describeComplexDataInputType200(complexDataType, inputDescriptor.getBinding());
                    
                    dataInput.setDataDescription(complexDataType);
                    
                    XMLUtil.qualifySubstitutionGroup(dataInput.getDataDescription(), ComplexDataDocument.type.getDocumentElementName(), null);
                }
            }

            // 3. Outputs
            Collection<OutputDescriptor> outputDescriptors = algorithmDescriptor.getOutputDescriptors();
            if (outputDescriptors.size() < 1) {
               LOGGER.error("No outputs found for algorithm {}", algorithmDescriptor.getIdentifier());
            }
            for (OutputDescriptor outputDescriptor : outputDescriptors) {

                net.opengis.wps.x20.OutputDescriptionType dataOutput = processDescription.addNewOutput();
                dataOutput.addNewIdentifier().setStringValue(outputDescriptor.getIdentifier());
                dataOutput.addNewTitle().setStringValue( outputDescriptor.hasTitle() ?
                        outputDescriptor.getTitle() :
                        outputDescriptor.getIdentifier());
                if (outputDescriptor.hasAbstract()) {
                    dataOutput.addNewAbstract().setStringValue(outputDescriptor.getAbstract());
                }

                if (outputDescriptor instanceof LiteralDataOutputDescriptor) {
                    LiteralDataOutputDescriptor<?> literalDescriptor = (LiteralDataOutputDescriptor)outputDescriptor;
                    
                    LiteralDataType literalData = LiteralDataType.Factory.newInstance(); 
                    
                    net.opengis.wps.x20.FormatDocument.Format defaultFormat =  literalData.addNewFormat();
                    
                    defaultFormat.setDefault(true);
                    
                    defaultFormat.setMimeType("text/plain");
                    
                    net.opengis.wps.x20.FormatDocument.Format textXMLFormat =  literalData.addNewFormat();
                    
                    textXMLFormat.setMimeType("text/xml");
                    
                    LiteralDataDomainType literalDataDomainType = literalData.addNewLiteralDataDomain();
                    
                    literalDataDomainType.addNewDataType().setReference(literalDescriptor.getDataType());
                	
                    literalDataDomainType.addNewAnyValue();
                    
                    dataOutput.setDataDescription(literalData);
                    
                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), LiteralDataDocument.type.getDocumentElementName(), null);
                    
                } else if (outputDescriptor instanceof ComplexDataOutputDescriptor) {
                	
                	ComplexDataType complexDataType = ComplexDataType.Factory.newInstance();  
                    describeComplexDataOutputType200(complexDataType, outputDescriptor.getBinding());
                    
                    dataOutput.setDataDescription(complexDataType);
                    
                    XMLUtil.qualifySubstitutionGroup(dataOutput.getDataDescription(), ComplexDataDocument.type.getDocumentElementName(), null);
                }
            }
        }
        
        return processOffering;
    }
    
    @Override
    public Class<? extends IData> getInputDataType(String identifier) {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return getAlgorithmDescriptor().getInputDescriptor(identifier).getBinding();
        } else {
            throw new IllegalStateException("Instance must have an algorithm descriptor");
        }
    }

    @Override
    public Class<? extends IData> getOutputDataType(String identifier) {
        AlgorithmDescriptor algorithmDescriptor = getAlgorithmDescriptor();
        if (algorithmDescriptor != null) {
            return getAlgorithmDescriptor().getOutputDescriptor(identifier).getBinding();
        } else {
            throw new IllegalStateException("Instance must have an algorithm descriptor");
        }
    }

    private List observers = new ArrayList();
    private Object state = null;

    @Override
    public Object getState() {
        return state;
    }

    @Override
    public void update(Object state) {
        this.state = state;
        notifyObservers();
    }

    @Override
    public void addObserver(IObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    public void notifyObservers() {
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            IObserver o = (IObserver) i.next();
            o.update(this);
        }
    }

    List<String> errorList = new ArrayList();
    protected List<String> addError(String error) {
        errorList.add(error);
        return errorList;
    }

    @Override
    public List<String> getErrors() {
        return errorList;
    }
}
