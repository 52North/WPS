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
package org.n52.wps.server.request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.opengis.ows.x11.BoundingBoxType;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.ValueType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x20.DataDescriptionType;
import net.opengis.wps.x20.DataDocument.Data;
import net.opengis.wps.x20.DataInputType;
import net.opengis.wps.x20.FormatDocument.Format;
import net.opengis.wps.x20.LiteralDataType;
import net.opengis.wps.x20.LiteralDataType.LiteralDataDomain;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.BoundingBoxData;
import org.n52.wps.io.data.binding.literal.AbstractLiteralDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.DataInputInterceptors;
import org.n52.wps.server.handler.DataInputInterceptors.DataInputInterceptorImplementations;
import org.n52.wps.server.handler.DataInputInterceptors.InterceptorInstance;
import org.n52.wps.server.request.strategy.ReferenceInputStream;
import org.n52.wps.server.request.strategy.ReferenceStrategyRegister;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.primitives.Doubles;

/**
 * Handles the input of the client and stores it into a Map.
 */
public class InputHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InputHandler.class);
    private static final BigInteger INT_MAX
            = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger INT_MIN
            = BigInteger.valueOf(Integer.MIN_VALUE);
	private Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
	private ProcessDescriptionType processDesc;
	private ProcessOffering processOffering;
	private String algorithmIdentifier = null; // Needed to take care of handling a conflict between different parsers.

        public static class Builder {
            protected Input inputs;
            protected String algorithmIdentifier = null;

            public Builder(Input inputs, String algorithmIdentifier) {
                this.inputs = inputs;
                this.algorithmIdentifier = algorithmIdentifier;
            }

            public Builder inputs(Input val) {
                inputs = val;
                return this;
            }

            public Builder algorithmIdentifier(String val) {
                algorithmIdentifier = val;
                return this;
            }

            public InputHandler build() throws ExceptionReport {
                return new InputHandler(this);
            }
        }

	/**
	 * Initializes a parser that handles each (line of) input based on the type of input.
	 * @see #handleComplexData(IOValueType)
	 * @see #handleComplexValueReference(IOValueType)
	 * @see #handleLiteralData(IOValueType)
	 * @see #handleBBoxValue(IOValueType)
         * @param builder
         * @throws ExceptionReport
	 */
        private InputHandler(Builder builder) throws ExceptionReport {
		this.algorithmIdentifier = builder.algorithmIdentifier;
		this.processDesc = (ProcessDescriptionType) RepositoryManager.getInstance().getProcessDescription(algorithmIdentifier).getProcessDescriptionType(WPSConfig.VERSION_100);
		this.processOffering = (ProcessOffering) RepositoryManager.getInstance().getProcessDescription(algorithmIdentifier).getProcessDescriptionType(WPSConfig.VERSION_200);

		if (processDesc == null) {
                    throw new ExceptionReport("Error while accessing the process description for " + algorithmIdentifier,
						ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		Map<String, InterceptorInstance> inputInterceptors = resolveInputInterceptors(algorithmIdentifier);

		InputType[] inputsV100 = builder.inputs.getInputsV100();
		
		DataInputType[] inputsV200 = builder.inputs.getInputsV200();
		
		if(inputsV100 != null){
		
		for (InputType input : inputsV100) {
			String inputId = input.getIdentifier().getStringValue().trim();
			if (inputInterceptors.containsKey(inputId)) {
				InterceptorInstance interceptor = inputInterceptors.get(inputId);
				List<IData> result = interceptor.applyInterception(input);

				if (result != null && !result.isEmpty()) {
					this.inputData.put(inputId, result);
					continue;
				}
			}

			if(input.getData() != null) {
				if(input.getData().getComplexData() != null) {
					handleComplexData(input, inputId);
				}
				else if(input.getData().getLiteralData() != null) {
					handleLiteralData(input);
				}
				else if(input.getData().getBoundingBoxData() != null) {
					handleBBoxValue(input);
				}
			}
			else if(input.getReference() != null) {
				handleComplexValueReference(input);
			}
			else {
				throw new ExceptionReport("Error while accessing the inputValue: " + inputId,
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
		}else if(inputsV200 != null){
			
			for (DataInputType input : inputsV200) {
				String inputId = input.getId().trim();
				if (inputInterceptors.containsKey(inputId)) {
					InterceptorInstance interceptor = inputInterceptors.get(inputId);
					List<IData> result = interceptor.applyInterception(input);

					if (result != null && !result.isEmpty()) {
						this.inputData.put(inputId, result);
						continue;
					}
				}

				if(input.getData() != null) {
					
					net.opengis.wps.x20.InputDescriptionType inputDescription = XMLBeansHelper.findInputByID(inputId, processOffering.getProcess());
					
					DataDescriptionType dataDesc = inputDescription.getDataDescription();
					
					if(dataDesc instanceof net.opengis.wps.x20.ComplexDataType) {
						handleComplexData(input, inputId);
					}
					else if(dataDesc instanceof LiteralDataType) {
						handleLiteralData(input);
					}
					else if(dataDesc instanceof net.opengis.ows.x20.BoundingBoxType) {
						handleBBoxValue(input);
					}
				}
				else if(input.getReference() != null) {
					handleComplexValueReference(input);
				}
				else {
					throw new ExceptionReport("Error while accessing the inputValue: " + inputId,
							ExceptionReport.INVALID_PARAMETER_VALUE);
				}
			}
		}
		
	}

    Map<String, InterceptorInstance> resolveInputInterceptors(String algorithmClassName) {
		Map<String,InterceptorInstance> result = new HashMap<String, InterceptorInstance>();
		Class<?> clazz;

		try {
			clazz = Class.forName(algorithmClassName, false, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
            LOGGER.warn("Could not find class {}", algorithmClassName);
			return result;
		}

		DataInputInterceptorImplementations annotation = clazz.getAnnotation(DataInputInterceptors.DataInputInterceptorImplementations.class);
		if (annotation != null) {
			Class<?> interceptorClazz;
			try {
				interceptorClazz = Class.forName(annotation.value());
			} catch (ClassNotFoundException e) {
				LOGGER.warn("Could not find class "+ annotation.value(), e);
				return result;
			}

			if (DataInputInterceptors.class.isAssignableFrom(interceptorClazz)) {
				DataInputInterceptors instance;
				try {
					instance = (DataInputInterceptors) interceptorClazz.newInstance();
				} catch (InstantiationException e) {
					LOGGER.warn("Could not instantiate class "+ interceptorClazz, e);
					return result;
				} catch (IllegalAccessException e) {
					LOGGER.warn("Could not access class "+ interceptorClazz, e);
					return result;
				}

				return instance.getInterceptors();
			}
		}
		return result;
	}

    ComplexDataDescriptionType getNonDefaultFormat(InputDescriptionType inputRefDesc, String dataMimeType, String dataSchema, String dataEncoding) {
        if (inputRefDesc.getComplexData() == null) {
            return null; // No complex data within inputs
        }

        ComplexDataDescriptionType[] formats = inputRefDesc.getComplexData().getSupported().getFormatArray();
        for (ComplexDataDescriptionType potentialFormat : formats) {
            String pFormatSchema = potentialFormat.getSchema();
            String pFormatEncoding = potentialFormat.getEncoding();
            if (potentialFormat.getMimeType().equalsIgnoreCase(dataMimeType)) {
                if (dataSchema != null && dataEncoding == null) {
                    if (dataSchema.equalsIgnoreCase(pFormatSchema)) {
                        return potentialFormat;
                    }
                }
                if (dataSchema == null && dataEncoding != null) {
                    if (dataEncoding.equalsIgnoreCase(pFormatEncoding)) {
                        return potentialFormat;
                    }

                }
                if (dataSchema != null && dataEncoding != null) {
                    if (dataSchema.equalsIgnoreCase(pFormatSchema)
                            && dataEncoding.equalsIgnoreCase(pFormatEncoding)) {
                        return potentialFormat;
                    }

                }
                if (dataSchema == null && dataEncoding == null) {
                    return potentialFormat;
                }
            }
        }
        return null;
    }

    protected String getComplexValueNodeString(Node complexValueNode) {
        String complexValue;
        try {
            complexValue = XMLUtil.nodeToString(complexValueNode);
            complexValue = complexValue.substring(complexValue.indexOf(">") + 1, complexValue.lastIndexOf("</"));
        } catch (TransformerFactoryConfigurationError e1) {
            throw new TransformerFactoryConfigurationError("Could not parse inline data. Reason " + e1);
        } catch (TransformerException e1) {
            throw new TransformerFactoryConfigurationError("Could not parse inline data. Reason " + e1);
        }
        return complexValue;
    }

	/**
	 * Handles the complexValue, which in this case should always include XML
	 * which can be parsed into a FeatureCollection.
	 * @param input The client input
         * @param inputId
         * @throws ExceptionReport If error occured while parsing XML
	 */
	 protected void handleComplexData(InputType input, String inputId) throws ExceptionReport{
		String complexValue;
		InputDescriptionType inputReferenceDesc;
		ComplexDataType data;
		Node complexValueNode;
                ComplexDataDescriptionType format = null;
                String dataSchema;
                String dataEncoding;
		String dataMimeType = null;
		String formatSchema = null;
		String formatEncoding = null;
                String potentialFormatSchema = null;
                String potentialFormatEncoding = null;

		inputReferenceDesc = XMLBeansHelper.findInputByID(inputId, processDesc.getDataInputs());
		if(inputReferenceDesc == null) {
                    LOGGER.debug("Input cannot be found in description for " + processDesc.getIdentifier().getStringValue() + "," + inputId);
		}

                data = input.getData().getComplexData();

                dataSchema = data.getSchema();
                dataMimeType = data.getMimeType();
                dataEncoding = data.getEncoding();

                complexValueNode =  input.getData().getComplexData().getDomNode();
                complexValue = getComplexValueNodeString(complexValueNode);

                //select parser
		//1. mimeType set?
		//yes--> set it
			//1.1 schema/encoding set?
			//yes-->set it
			//not-->set default values for parser with matching mime type

		//no--> schema or/and encoding are set?
					//yes-->use it, look if only one mime type can be found;
					//not-->use default values

		// overwrite with data format from request if appropriate
		if (data.isSetMimeType() && dataMimeType != null){
                    format = findComplexDataDescriptionType(inputReferenceDesc, dataMimeType, dataSchema, dataEncoding, potentialFormatSchema, potentialFormatEncoding);

                    if(format == null){
                        throw new ExceptionReport("Could not determine intput format", ExceptionReport.INVALID_PARAMETER_VALUE);
                    }

                    dataMimeType = format.getMimeType();

                    //no encoding provided--> select default one for mimeType
                    if(format.isSetEncoding()){
                        formatEncoding = format.getEncoding();
                    }

                    //no encoding provided--> select default one for mimeType
                    if(format.isSetSchema()){
                        formatSchema = format.getSchema();
                    }
		} else {
			//mimeType not in request
			if(StringUtils.isBlank(dataMimeType) && !data.isSetEncoding() && !data.isSetSchema()){
                            //nothing set, use default values
                            formatSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
                            dataMimeType = inputReferenceDesc.getComplexData().getDefault().getFormat().getMimeType();
                            formatEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
			}else{
                                //do a smart search an look if a mimeType can be found for either schema and/or encoding
				if(StringUtils.isBlank(dataMimeType)){

					if(data.isSetEncoding() && !data.isSetSchema()){
							//encoding set only
							int foundCount = 0;
							String defaultEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
							ComplexDataDescriptionType encodingFormat = null;
							String foundEncoding = null;
							if(defaultEncoding.equalsIgnoreCase(data.getEncoding())){
								foundEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
								encodingFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
								foundCount++;
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getEncoding())){
										 foundEncoding = tempFormat.getEncoding();
										 encodingFormat = tempFormat;
										 foundCount++;
									 }
								 }
							}

							if(foundCount == 1){
								formatEncoding = foundEncoding;
								dataMimeType = encodingFormat.getMimeType();
								if(encodingFormat.isSetSchema()){
                                                                    formatSchema = encodingFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						} else if(data.isSetSchema() && !data.isSetEncoding()){
							//schema set only
							ComplexDataDescriptionType schemaFormat = null;
							String defaultSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
							int found = 0;
							String foundSchema = null;
							//TODO: please review change
							//Old version causes NullPointerException if default input is given by mimetype and not by schema:
							/*
							 if(defaultSchema != null && defaultSchema.equalsIgnoreCase(data.getSchema())){
							 	...
							 }

							 * */
							if(!StringUtils.isBlank(defaultSchema) && defaultSchema.equalsIgnoreCase(data.getSchema())){
								foundSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
								schemaFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
								found++;
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 //TODO: please review change
									 //Old if-clause wouldn't be true ever and causes NullPointerException if one of the supported types is given by mimetype and not by schema:
									 /*
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getSchema())){
										 foundSchema = tempFormat.getSchema();
										 schemaFormat =tempFormat;
										 found = found +1;
									 }

									 */
									 if(tempFormat.isSetSchema() && tempFormat.getSchema().equalsIgnoreCase(data.getSchema())){
										 foundSchema = tempFormat.getSchema();
										 schemaFormat =tempFormat;
										 found++;
									 }
								 }
							}

							if(found == 1){
								formatSchema = foundSchema;
								dataMimeType = schemaFormat.getMimeType();
								if(schemaFormat.isSetEncoding()){
									formatEncoding = schemaFormat.getEncoding();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						} else if(data.isSetEncoding() && data.isSetSchema()){
							//schema and encoding set
							//encoding
							String defaultEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();

							List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
							if(defaultEncoding.equalsIgnoreCase(data.getEncoding())){
								foundEncodingList.add(inputReferenceDesc.getComplexData().getDefault().getFormat());
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getEncoding())){
										 foundEncodingList.add(tempFormat);
									 }
                                                                }

							//schema
							List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
							String defaultSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
							//TODO: please review change
							//Old version causes NullPointerException if default input is given by mimetype and not by schema:
							//
							//if(defaultSchema.equalsIgnoreCase(data.getSchema())){...
							//
							if(defaultSchema!= null && defaultSchema.equalsIgnoreCase(data.getSchema())){
								foundSchemaList.add(inputReferenceDesc.getComplexData().getDefault().getFormat());
							}else{
								 formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 /*
									  * TODO please review Change
									  * Old if-clause wouldn't be true ever and causes NullPointerException if one of the supported types is given by mimetype and not by schema:
									  *
									  * old code:
									  if(tempFormat.getEncoding().equalsIgnoreCase(data.getSchema())){
										 foundSchemaList.add(tempFormat);
									 }
									 */
									 if(tempFormat.getSchema()!=null && tempFormat.getSchema().equalsIgnoreCase(data.getSchema())){
										 foundSchemaList.add(tempFormat);
									 }
								 }
							}


							//results
							ComplexDataDescriptionType foundCommonFormat = null;
							for(ComplexDataDescriptionType encodingFormat : foundEncodingList){
								for(ComplexDataDescriptionType schemaFormat : foundSchemaList){
									if(encodingFormat.equals(schemaFormat)){
										foundCommonFormat = encodingFormat;
									}
								}
							}

							if(foundCommonFormat!=null){
								dataMimeType = foundCommonFormat.getMimeType();
								if(foundCommonFormat.isSetEncoding()){
									formatEncoding = foundCommonFormat.getEncoding();
								}
								if(foundCommonFormat.isSetSchema()){
									formatSchema = foundCommonFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
						}
					}
				}
			}
		}

		IParser parser = null;
		try {
                    LOGGER.debug("Looking for matching Parser ..." +
					" schema: " + formatSchema +
					" mimeType: " + dataMimeType +
					" encoding: " + formatEncoding);

			Class<?> algorithmInput = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputId);
			parser = ParserFactory.getInstance().getParser(formatSchema, dataMimeType, formatEncoding, algorithmInput);
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data", ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		if(parser == null) {
                    throw new ExceptionReport("Error. No applicable parser found for " + formatSchema + "," + dataMimeType + "," + formatEncoding, ExceptionReport.NO_APPLICABLE_CODE);
		}

		IData collection = parseComplexValue(formatEncoding, complexValue, dataMimeType, formatSchema, parser);

		//enable maxoccurs of parameters with the same name.
                List<IData> list = new ArrayList<IData>();
		if(inputData.containsKey(inputId)) {
                    list = inputData.get(inputId);
		}
                list.add(collection);
                inputData.put(inputId, list);
	}

         protected ComplexDataDescriptionType findComplexDataDescriptionType(InputDescriptionType inputReferenceDesc, String dataMimeType, String dataSchema, String dataEncoding, String potentialFormatSchema, String potentialFormatEncoding) {
             ComplexDataDescriptionType result = null;
             boolean canUseDefault = false;
			String defaultMimeType = inputReferenceDesc.getComplexData().getDefault().getFormat().getMimeType();

			if(defaultMimeType.equalsIgnoreCase(dataMimeType)){
				ComplexDataDescriptionType potentialFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
				if(dataSchema != null && dataEncoding == null){
					if(dataSchema.equalsIgnoreCase(potentialFormatSchema)){
						canUseDefault = true;
						result = potentialFormat;
					}
				} else if(dataSchema != null && dataEncoding != null) {
					if(dataSchema.equalsIgnoreCase(potentialFormatSchema)
                                                && dataEncoding.equalsIgnoreCase(potentialFormatEncoding)){
						canUseDefault = true;
						result = potentialFormat;
					}
				} else if(dataSchema == null && dataEncoding != null){
					if(dataEncoding.equalsIgnoreCase(potentialFormatEncoding)){
						canUseDefault = true;
						result = potentialFormat;
					}
				} else {
					canUseDefault = true;
					result = potentialFormat;
				}
			}

                        if (!canUseDefault) {
                            result = getNonDefaultFormat(inputReferenceDesc, dataMimeType, dataSchema, dataEncoding);
                        }
             return result;
         }

         
	/**
	 * Handles the complexValue, which in this case should always include XML
	 * which can be parsed into a FeatureCollection.
	 * 
	 * @param input
	 *            The client input
	 * @param inputId
	 * @throws ExceptionReport
	 *             If error occured while parsing XML
	 */
	private void handleComplexData(DataInputType input, String inputId)
			throws ExceptionReport {
		String complexValue;
		net.opengis.wps.x20.InputDescriptionType inputReferenceDesc;
		Data data;
		Node complexValueNode;
		Format format = null;
		String dataSchema;
		String dataEncoding;
		String dataMimeType = null;
		String formatSchema = null;
		String formatEncoding = null;
		String potentialFormatSchema = null;
		String potentialFormatEncoding = null;

		inputReferenceDesc = XMLBeansHelper.findInputByID(inputId,
				processOffering.getProcess());
		if (inputReferenceDesc == null) {
			LOGGER.debug("Input cannot be found in description for "
					+ processDesc.getIdentifier().getStringValue() + ","
					+ inputId);
		}

		data = input.getData();

		dataSchema = data.getSchema();
		dataMimeType = data.getMimeType();
		dataEncoding = data.getEncoding();

		complexValueNode = data.getDomNode();
		complexValue = getComplexValueNodeString(complexValueNode);

		// select parser
		// 1. mimeType set?
		// yes--> set it
		// 1.1 schema/encoding set?
		// yes-->set it
		// not-->set default values for parser with matching mime type

		// no--> schema or/and encoding are set?
		// yes-->use it, look if only one mime type can be found;
		// not-->use default values

		// overwrite with data format from request if appropriate
		if (data.isSetMimeType() && dataMimeType != null) {
			format = findFormat(inputReferenceDesc,
					dataMimeType, dataSchema, dataEncoding,
					potentialFormatSchema, potentialFormatEncoding);

			if (format == null) {
				throw new ExceptionReport("Could not determine intput format",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}

			dataMimeType = format.getMimeType();

			// no encoding provided--> select default one for mimeType
			if (format.isSetEncoding()) {
				formatEncoding = format.getEncoding();
			}

			// no encoding provided--> select default one for mimeType
			if (format.isSetSchema()) {
				formatSchema = format.getSchema();
			}
		} else {
			
			Format[] formatArray =  inputReferenceDesc.getDataDescription().getFormatArray();
			
			Format defaultFormat = getDefaultFormat(formatArray);
			
			// mimeType not in request
			if (StringUtils.isBlank(dataMimeType) && !data.isSetEncoding()
					&& !data.isSetSchema()) {
				// nothing set, use default values
				formatSchema = defaultFormat.getSchema();
				dataMimeType = defaultFormat.getMimeType();
				formatEncoding = defaultFormat.getEncoding();
			} else {
				// do a smart search an look if a mimeType can be found for
				// either schema and/or encoding
				if (StringUtils.isBlank(dataMimeType)) {

					if (data.isSetEncoding() && !data.isSetSchema()) {
						// encoding set only
						int foundCount = 0;
						String defaultEncoding = defaultFormat
								.getEncoding();
						Format encodingFormat = null;
						String foundEncoding = null;
						if (defaultEncoding
								.equalsIgnoreCase(data.getEncoding())) {
							foundEncoding = defaultFormat.getEncoding();
							encodingFormat = defaultFormat;
							foundCount++;
						} else {
							Format[] formats = inputReferenceDesc.getDataDescription().getFormatArray();
							for (Format tempFormat : formats) {
								if (tempFormat.getEncoding().equalsIgnoreCase(
										data.getEncoding())) {
									foundEncoding = tempFormat.getEncoding();
									encodingFormat = tempFormat;
									foundCount++;
								}
							}
						}

						if (foundCount == 1) {
							formatEncoding = foundEncoding;
							dataMimeType = encodingFormat.getMimeType();
							if (encodingFormat.isSetSchema()) {
								formatSchema = encodingFormat.getSchema();
							}
						} else {
							throw new ExceptionReport(
									"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]",
									ExceptionReport.MISSING_PARAMETER_VALUE);
						}

					} else if (data.isSetSchema() && !data.isSetEncoding()) {
						// schema set only
						Format schemaFormat = null;
						String defaultSchema = defaultFormat.getSchema();
						int found = 0;
						String foundSchema = null;
						// TODO: please review change
						// Old version causes NullPointerException if default
						// input is given by mimetype and not by schema:
						/*
						 * if(defaultSchema != null &&
						 * defaultSchema.equalsIgnoreCase(data.getSchema())){
						 * ... }
						 */
						if (!StringUtils.isBlank(defaultSchema)
								&& defaultSchema.equalsIgnoreCase(data
										.getSchema())) {
							foundSchema = defaultFormat.getSchema();
							schemaFormat = defaultFormat;
							found++;
						} else {
							Format[] formats = inputReferenceDesc.getDataDescription().getFormatArray();
							for (Format tempFormat : formats) {
								// TODO: please review change
								// Old if-clause wouldn't be true ever and
								// causes NullPointerException if one of the
								// supported types is given by mimetype and not
								// by schema:
								/*
								 * if(tempFormat.getEncoding().equalsIgnoreCase(data
								 * .getSchema())){ foundSchema =
								 * tempFormat.getSchema(); schemaFormat
								 * =tempFormat; found = found +1; }
								 */
								if (tempFormat.isSetSchema()
										&& tempFormat.getSchema()
												.equalsIgnoreCase(
														data.getSchema())) {
									foundSchema = tempFormat.getSchema();
									schemaFormat = tempFormat;
									found++;
								}
							}
						}

						if (found == 1) {
							formatSchema = foundSchema;
							dataMimeType = schemaFormat.getMimeType();
							if (schemaFormat.isSetEncoding()) {
								formatEncoding = schemaFormat.getEncoding();
							}
						} else {
							throw new ExceptionReport(
									"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]",
									ExceptionReport.MISSING_PARAMETER_VALUE);
						}

					} else if (data.isSetEncoding() && data.isSetSchema()) {
						// schema and encoding set
						// encoding
						String defaultEncoding = defaultFormat.getEncoding();

						List<Format> foundEncodingList = new ArrayList<>();
						if (defaultEncoding
								.equalsIgnoreCase(data.getEncoding())) {
							foundEncodingList.add(defaultFormat);
						} else {
							Format[] formats = inputReferenceDesc.getDataDescription().getFormatArray();
							for (Format tempFormat : formats) {
								if (tempFormat.getEncoding().equalsIgnoreCase(
										data.getEncoding())) {
									foundEncodingList.add(tempFormat);
								}
							}

							// schema
							List<Format> foundSchemaList = new ArrayList<>();
							String defaultSchema = defaultFormat.getSchema();
							// TODO: please review change
							// Old version causes NullPointerException if
							// default input is given by mimetype and not by
							// schema:
							//
							// if(defaultSchema.equalsIgnoreCase(data.getSchema())){...
							//
							if (defaultSchema != null
									&& defaultSchema.equalsIgnoreCase(data
											.getSchema())) {
								foundSchemaList.add(defaultFormat);
							} else {
								formats = inputReferenceDesc.getDataDescription().getFormatArray();
								for (Format tempFormat : formats) {
									/*
									 * TODO please review Change Old if-clause
									 * wouldn't be true ever and causes
									 * NullPointerException if one of the
									 * supported types is given by mimetype and
									 * not by schema:
									 * 
									 * old code:
									 * if(tempFormat.getEncoding().equalsIgnoreCase
									 * (data.getSchema())){
									 * foundSchemaList.add(tempFormat); }
									 */
									if (tempFormat.getSchema() != null
											&& tempFormat.getSchema()
													.equalsIgnoreCase(
															data.getSchema())) {
										foundSchemaList.add(tempFormat);
									}
								}
							}

							// results
							Format foundCommonFormat = null;
							for (Format encodingFormat : foundEncodingList) {
								for (Format schemaFormat : foundSchemaList) {
									if (encodingFormat.equals(schemaFormat)) {
										foundCommonFormat = encodingFormat;
									}
								}
							}

							if (foundCommonFormat != null) {
								dataMimeType = foundCommonFormat.getMimeType();
								if (foundCommonFormat.isSetEncoding()) {
									formatEncoding = foundCommonFormat
											.getEncoding();
								}
								if (foundCommonFormat.isSetSchema()) {
									formatSchema = foundCommonFormat
											.getSchema();
								}
							} else {
								throw new ExceptionReport(
										"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]",
										ExceptionReport.MISSING_PARAMETER_VALUE);
							}
						}
					}
				}
			}
		}

		IParser parser = null;
		try {
			LOGGER.debug("Looking for matching Parser ..." + " schema: "
					+ formatSchema + " mimeType: " + dataMimeType
					+ " encoding: " + formatEncoding);

			Class<?> algorithmInput = RepositoryManager.getInstance()
					.getInputDataTypeForAlgorithm(this.algorithmIdentifier,
							inputId);
			parser = ParserFactory.getInstance().getParser(formatSchema,
					dataMimeType, formatEncoding, algorithmInput);
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		if (parser == null) {
			throw new ExceptionReport("Error. No applicable parser found for "
					+ formatSchema + "," + dataMimeType + "," + formatEncoding,
					ExceptionReport.NO_APPLICABLE_CODE);
		}

		IData collection = parseComplexValue(formatEncoding, complexValue,
				dataMimeType, formatSchema, parser);

		// enable maxoccurs of parameters with the same name.
		List<IData> list = new ArrayList<IData>();
		if (inputData.containsKey(inputId)) {
			list = inputData.get(inputId);
		}
		list.add(collection);
		inputData.put(inputId, list);
	}
	
	/**
	 * Handles the ComplexValueReference
	 * 
	 * @param input
	 *            The client input
	 * @throws ExceptionReport
	 *             If the input (as url) is invalid, or there is an error while
	 *             parsing the XML.
	 */
	private void handleComplexValueReference(DataInputType input)
			throws ExceptionReport {
		String inputID = input.getId();

		ReferenceStrategyRegister register = ReferenceStrategyRegister
				.getInstance();
		ReferenceInputStream stream = register.resolveReference(new InputReference(input));

		String dataURLString = input.getReference().getHref();
		// dataURLString = URLDecoder.decode(dataURLString);
		// dataURLString = dataURLString.replace("&amp;", "");
		LOGGER.debug("Loading data from: " + dataURLString);

		/**
		 * initialize data format with default values defaults and overwrite
		 * with defaults from request if applicable
		 */
		InputDescriptionType inputPD = null;
		for (InputDescriptionType tempDesc : this.processDesc.getDataInputs()
				.getInputArray()) {
			if (inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputPD = tempDesc;
				break;
			}
		}
		if (inputPD == null) { // check if there is a corresponding input
								// identifier in the process description
			LOGGER.debug("Input cannot be found in description for "
					+ this.processDesc.getIdentifier().getStringValue() + ","
					+ inputID);
			throw new RuntimeException(
					"Input cannot be found in description for "
							+ this.processDesc.getIdentifier().getStringValue()
							+ "," + inputID);
		}

		// select parser

		// 1. mimeType set?
		// yes--> set it
		// 1.1 schema/encoding set?
		// yes-->set it
		// not-->set default values for parser with matching mime type

		// no--> look in http stream
		// 2. mimeType set in http stream
		// yes -->set it
		// 2.1 schema/encoding set?
		// yes-->set it
		// not-->set default values for parser with matching mime type
		// no--> schema or/and encoding are set?
		// yes-->use it, look if only one mime type can be found
		// not-->use default values

		String schema = null;
		String mimeType = null;
		String encoding = null;

		// overwrite with data format from request if appropriate
		net.opengis.wps.x20.ReferenceType referenceData = input.getReference();

		if (referenceData.isSetMimeType()
				&& referenceData.getMimeType() != null) {
			// mime type in request
			mimeType = referenceData.getMimeType();
			ComplexDataDescriptionType format = null;

			String defaultMimeType = inputPD.getComplexData().getDefault()
					.getFormat().getMimeType();

			boolean canUseDefault = false;
			if (defaultMimeType.equalsIgnoreCase(mimeType)) {
				ComplexDataDescriptionType potentialFormat = inputPD
						.getComplexData().getDefault().getFormat();
				if (referenceData.getSchema() != null
						&& referenceData.getEncoding() == null) {
					if (referenceData.getSchema().equalsIgnoreCase(
							potentialFormat.getSchema())) {
						canUseDefault = true;
						format = potentialFormat;
					}
				}
				if (referenceData.getSchema() == null
						&& referenceData.getEncoding() != null) {
					if (referenceData.getEncoding().equalsIgnoreCase(
							potentialFormat.getEncoding())) {
						canUseDefault = true;
						format = potentialFormat;
					}

				}
				if (referenceData.getSchema() != null
						&& referenceData.getEncoding() != null) {
					if (referenceData.getSchema().equalsIgnoreCase(
							potentialFormat.getSchema())
							&& referenceData.getEncoding().equalsIgnoreCase(
									potentialFormat.getEncoding())) {
						canUseDefault = true;
						format = potentialFormat;
					}

				}
				if (referenceData.getSchema() == null
						&& referenceData.getEncoding() == null) {
					canUseDefault = true;
					format = potentialFormat;
				}

			}
			if (!canUseDefault) {
				ComplexDataDescriptionType[] formats = inputPD.getComplexData()
						.getSupported().getFormatArray();
				for (ComplexDataDescriptionType potentialFormat : formats) {
					if (potentialFormat.getMimeType()
							.equalsIgnoreCase(mimeType)) {
						if (referenceData.getSchema() != null
								&& referenceData.getEncoding() == null) {
							if (referenceData.getSchema().equalsIgnoreCase(
									potentialFormat.getSchema())) {
								format = potentialFormat;
							}
						}
						if (referenceData.getSchema() == null
								&& referenceData.getEncoding() != null) {
							if (referenceData.getEncoding().equalsIgnoreCase(
									potentialFormat.getEncoding())) {
								format = potentialFormat;
							}

						}
						if (referenceData.getSchema() != null
								&& referenceData.getEncoding() != null) {
							if (referenceData.getSchema().equalsIgnoreCase(
									potentialFormat.getSchema())
									&& referenceData.getEncoding()
											.equalsIgnoreCase(
													potentialFormat
															.getEncoding())) {
								format = potentialFormat;
							}

						}
						if (referenceData.getSchema() == null
								&& referenceData.getEncoding() == null) {
							format = potentialFormat;
						}
					}
				}
			}
			if (format == null) {
				throw new ExceptionReport(
						"Possibly multiple or none matching generators found for the input data with id = \""
								+ inputPD.getIdentifier().getStringValue()
								+ "\". Is the MimeType (\""
								+ referenceData.getMimeType()
								+ "\") correctly set?",
						ExceptionReport.INVALID_PARAMETER_VALUE);
				// throw new
				// ExceptionReport("Could not determine format of the input data (id= \""
				// + inputPD.getIdentifier().getStringValue() +
				// "\"), given the mimetype \"" + referenceData.getMimeType() +
				// "\"", ExceptionReport.INVALID_PARAMETER_VALUE);

			}

			mimeType = format.getMimeType();

			if (format.isSetEncoding()) {
				// no encoding provided--> select default one for mimeType
				encoding = format.getEncoding();
			}

			if (format.isSetSchema()) {
				// no encoding provided--> select default one for mimeType
				schema = format.getSchema();
			}

		} else {
			// mimeType not in request, fetch mimetype from reference response
			mimeType = stream.getMimeType();
			if (mimeType.contains("GML2")) {
				mimeType = "text/xml; subtype=gml/2.0.0";
			}
			if (mimeType.contains("GML3")) {
				mimeType = "text/xml; subtype=gml/3.0.0";
			}
			ComplexDataDescriptionType format = null;

			if (mimeType != null) {
				String defaultMimeType = inputPD.getComplexData().getDefault()
						.getFormat().getMimeType();

				boolean canUseDefault = false;
				if (defaultMimeType.equalsIgnoreCase(mimeType)) {
					ComplexDataDescriptionType potentialFormat = inputPD
							.getComplexData().getDefault().getFormat();
					if (referenceData.getSchema() != null
							&& referenceData.getEncoding() == null) {
						if (referenceData.getSchema().equalsIgnoreCase(
								potentialFormat.getSchema())) {
							canUseDefault = true;
							format = potentialFormat;
						}
					}
					if (referenceData.getSchema() == null
							&& referenceData.getEncoding() != null) {
						if (referenceData.getEncoding().equalsIgnoreCase(
								potentialFormat.getEncoding())) {
							canUseDefault = true;
							format = potentialFormat;
						}

					}
					if (referenceData.getSchema() != null
							&& referenceData.getEncoding() != null) {
						if (referenceData.getSchema().equalsIgnoreCase(
								potentialFormat.getSchema())
								&& referenceData.getEncoding()
										.equalsIgnoreCase(
												potentialFormat.getEncoding())) {
							canUseDefault = true;
							format = potentialFormat;
						}

					}
					if (referenceData.getSchema() == null
							&& referenceData.getEncoding() == null) {
						canUseDefault = true;
						format = potentialFormat;
					}

				}
				if (!canUseDefault) {
					ComplexDataDescriptionType[] formats = inputPD
							.getComplexData().getSupported().getFormatArray();
					for (ComplexDataDescriptionType potentialFormat : formats) {
						if (!StringUtils.isBlank(potentialFormat.getMimeType())
								&& potentialFormat.getMimeType()
										.equalsIgnoreCase(mimeType)) {
							if (referenceData.getSchema() != null
									&& referenceData.getEncoding() == null) {
								if (referenceData.getSchema().equalsIgnoreCase(
										potentialFormat.getSchema())) {
									format = potentialFormat;
								}
							}
							if (referenceData.getSchema() == null
									&& referenceData.getEncoding() != null) {
								if (referenceData.getEncoding()
										.equalsIgnoreCase(
												potentialFormat.getEncoding())) {
									format = potentialFormat;
								}

							}
							if (referenceData.getSchema() != null
									&& referenceData.getEncoding() != null) {
								if (referenceData.getSchema().equalsIgnoreCase(
										potentialFormat.getSchema())
										&& referenceData.getEncoding()
												.equalsIgnoreCase(
														potentialFormat
																.getEncoding())) {
									format = potentialFormat;
								}

							}
							if (referenceData.getSchema() == null
									&& referenceData.getEncoding() == null) {
								format = potentialFormat;
							}
						}
					}
				}
				if (format == null) {
					// throw new
					// ExceptionReport("Could not determine intput format. Possibly multiple or none matching generators found. MimeType Set?",
					// ExceptionReport.INVALID_PARAMETER_VALUE);
					// TODO Review error message
					throw new ExceptionReport(
							"Could not determine input format because none of the supported formats match the given schema (\""
									+ referenceData.getSchema()
									+ "\") and encoding (\""
									+ referenceData.getEncoding()
									+ "\"). (A mimetype was not specified)",
							ExceptionReport.INVALID_PARAMETER_VALUE);

				}

				mimeType = format.getMimeType();

				if (format.isSetEncoding()) {
					// no encoding provided--> select default one for mimeType
					encoding = format.getEncoding();
				}

				if (format.isSetSchema()) {
					// no encoding provided--> select default one for mimeType
					schema = format.getSchema();
				}
			}

			if (mimeType == null && !referenceData.isSetEncoding()
					&& !referenceData.isSetSchema()) {
				// nothing set, use default values
				schema = inputPD.getComplexData().getDefault().getFormat()
						.getSchema();
				mimeType = inputPD.getComplexData().getDefault().getFormat()
						.getMimeType();
				encoding = inputPD.getComplexData().getDefault().getFormat()
						.getEncoding();

			} else {
				// do a smart search an look if a mimeType can be found for
				// either schema and/or encoding

				if (mimeType == null) {
					if (referenceData.isSetEncoding()
							&& !referenceData.isSetSchema()) {
						// encoding set only
						ComplexDataDescriptionType encodingFormat = null;
						String defaultEncoding = inputPD.getComplexData()
								.getDefault().getFormat().getEncoding();
						int found = 0;
						String foundEncoding = null;
						if (defaultEncoding.equalsIgnoreCase(referenceData
								.getEncoding())) {
							foundEncoding = inputPD.getComplexData()
									.getDefault().getFormat().getEncoding();
							encodingFormat = inputPD.getComplexData()
									.getDefault().getFormat();
							found += 1;
						} else {
							ComplexDataDescriptionType[] formats = inputPD
									.getComplexData().getSupported()
									.getFormatArray();
							for (ComplexDataDescriptionType tempFormat : formats) {
								if (tempFormat.getEncoding().equalsIgnoreCase(
										referenceData.getEncoding())) {
									foundEncoding = tempFormat.getEncoding();
									encodingFormat = tempFormat;
									found += 1;
								}
							}
						}

						if (found == 1) {
							encoding = foundEncoding;
							mimeType = encodingFormat.getMimeType();
							if (encodingFormat.isSetSchema()) {
								schema = encodingFormat.getSchema();
							}
						} else {
							throw new ExceptionReport(
									"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]",
									ExceptionReport.MISSING_PARAMETER_VALUE);
						}

					}
					if (referenceData.isSetSchema()
							&& !referenceData.isSetEncoding()) {
						// schema set only
						ComplexDataDescriptionType schemaFormat = null;
						String defaultSchema = inputPD.getComplexData()
								.getDefault().getFormat().getSchema();
						int found = 0;
						String foundSchema = null;
						if (defaultSchema.equalsIgnoreCase(referenceData
								.getSchema())) {
							foundSchema = inputPD.getComplexData().getDefault()
									.getFormat().getSchema();
							schemaFormat = inputPD.getComplexData()
									.getDefault().getFormat();
							found += 1;
						} else {
							ComplexDataDescriptionType[] formats = inputPD
									.getComplexData().getSupported()
									.getFormatArray();
							for (ComplexDataDescriptionType tempFormat : formats) {
								if (tempFormat.getEncoding().equalsIgnoreCase(
										referenceData.getSchema())) {
									foundSchema = tempFormat.getSchema();
									schemaFormat = tempFormat;
									found += 1;
								}
							}
						}

						if (found == 1) {
							schema = foundSchema;
							mimeType = schemaFormat.getMimeType();
							if (schemaFormat.isSetEncoding()) {
								encoding = schemaFormat.getEncoding();
							}
						} else {
							throw new ExceptionReport(
									"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]",
									ExceptionReport.MISSING_PARAMETER_VALUE);
						}

					}
					if (referenceData.isSetEncoding()
							&& referenceData.isSetSchema()) {
						// schema and encoding set

						// encoding
						String defaultEncoding = inputPD.getComplexData()
								.getDefault().getFormat().getEncoding();

						List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
						if (defaultEncoding.equalsIgnoreCase(referenceData
								.getEncoding())) {
							foundEncodingList.add(inputPD.getComplexData()
									.getDefault().getFormat());

						} else {
							ComplexDataDescriptionType[] formats = inputPD
									.getComplexData().getSupported()
									.getFormatArray();
							for (ComplexDataDescriptionType tempFormat : formats) {
								if (tempFormat.getEncoding().equalsIgnoreCase(
										referenceData.getEncoding())) {
									foundEncodingList.add(tempFormat);
								}
							}

							// schema
							List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
							String defaultSchema = inputPD.getComplexData()
									.getDefault().getFormat().getSchema();
							if (defaultSchema.equalsIgnoreCase(referenceData
									.getSchema())) {
								foundSchemaList.add(inputPD.getComplexData()
										.getDefault().getFormat());
							} else {
								formats = inputPD.getComplexData()
										.getSupported().getFormatArray();
								for (ComplexDataDescriptionType tempFormat : formats) {
									if (tempFormat.getEncoding()
											.equalsIgnoreCase(
													referenceData.getSchema())) {
										foundSchemaList.add(tempFormat);
									}
								}
							}

							// results
							ComplexDataDescriptionType foundCommonFormat = null;
							for (ComplexDataDescriptionType encodingFormat : foundEncodingList) {
								for (ComplexDataDescriptionType schemaFormat : foundSchemaList) {
									if (encodingFormat.equals(schemaFormat)) {
										foundCommonFormat = encodingFormat;
									}
								}

							}

							if (foundCommonFormat != null) {
								mimeType = foundCommonFormat.getMimeType();
								if (foundCommonFormat.isSetEncoding()) {
									encoding = foundCommonFormat.getEncoding();
								}
								if (foundCommonFormat.isSetSchema()) {
									schema = foundCommonFormat.getSchema();
								}
							} else {
								throw new ExceptionReport(
										"Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]",
										ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						}

					}

				}
			}

		}

		LOGGER.debug("Loading parser for: schema = \"" + schema
				+ "\" , mimetype = \"" + mimeType + "\", encoding = \""
				+ encoding + "\"");

		IParser parser = null;
		try {
			Class<?> algorithmInputClass = RepositoryManager.getInstance()
					.getInputDataTypeForAlgorithm(this.algorithmIdentifier,
							inputID);
			if (algorithmInputClass == null) {
				throw new RuntimeException(
						"Could not determine internal input class for input"
								+ inputID);
			}
			LOGGER.info("Looking for matching Parser ..." + " schema: \""
					+ schema + "\", mimeType: \"" + mimeType
					+ "\", encoding: \"" + encoding + "\"");

			parser = ParserFactory.getInstance().getParser(schema, mimeType,
					encoding, algorithmInputClass);

			if (parser == null) {
				throw new ExceptionReport(
						"Error. No applicable parser found for schema=\""
								+ schema + "\", mimeType=\"" + mimeType
								+ "\", encoding=\"" + encoding + "\"",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		IData parsedInputData = parser.parse(stream, mimeType, schema);

		// enable maxxoccurs of parameters with the same name.
		if (inputData.containsKey(inputID)) {
			List<IData> list = inputData.get(inputID);
			list.add(parsedInputData);
			inputData.put(inputID, list);
		} else {
			List<IData> list = new ArrayList<IData>();
			list.add(parsedInputData);
			inputData.put(inputID, list);
		}

	}
	
	private Format getDefaultFormat(Format[] formatArray){
		
		for (Format format : formatArray) {
			if(format.isSetDefault()){
				return format;
			}
		}
		//TODO throw RuntimeException, as there must be a default format?
		return null;
	}
	
	private Format findFormat(net.opengis.wps.x20.InputDescriptionType inputReferenceDesc,
			String dataMimeType, String dataSchema, String dataEncoding,
			String potentialFormatSchema, String potentialFormatEncoding) {
		Format result = null;
		boolean canUseDefault = false;
		
		Format[] formatArray =  inputReferenceDesc.getDataDescription().getFormatArray();
		
		Format defaultFormat = getDefaultFormat(formatArray);
		
		String defaultMimeType = defaultFormat.getMimeType();

		if (defaultMimeType.equalsIgnoreCase(dataMimeType)) {
			Format potentialFormat = defaultFormat;
			if (dataSchema != null && dataEncoding == null) {
				if (dataSchema.equalsIgnoreCase(potentialFormatSchema)) {
					canUseDefault = true;
					result = potentialFormat;
				}
			} else if (dataSchema != null && dataEncoding != null) {
				if (dataSchema.equalsIgnoreCase(potentialFormatSchema)
						&& dataEncoding
								.equalsIgnoreCase(potentialFormatEncoding)) {
					canUseDefault = true;
					result = potentialFormat;
				}
			} else if (dataSchema == null && dataEncoding != null) {
				if (dataEncoding.equalsIgnoreCase(potentialFormatEncoding)) {
					canUseDefault = true;
					result = potentialFormat;
				}
			} else {
				canUseDefault = true;
				result = potentialFormat;
			}
		}

		if (!canUseDefault) {
			result = getNonDefaultFormat(inputReferenceDesc, dataMimeType,
					dataSchema, dataEncoding);
		}
		return result;
	}
	
	private Format getNonDefaultFormat(
			net.opengis.wps.x20.InputDescriptionType inputRefDesc, String dataMimeType,
			String dataSchema, String dataEncoding) {
		//TODO still needed?
//		if (inputRefDesc.getComplexData() == null) {
//			return null; // No complex data within inputs
//		}

		Format[] formats = inputRefDesc.getDataDescription().getFormatArray();
		for (Format potentialFormat : formats) {
			String pFormatSchema = potentialFormat.getSchema();
			String pFormatEncoding = potentialFormat.getEncoding();
			if (potentialFormat.getMimeType().equalsIgnoreCase(dataMimeType)) {
				if (dataSchema != null && dataEncoding == null) {
					if (dataSchema.equalsIgnoreCase(pFormatSchema)) {
						return potentialFormat;
					}
				}
				if (dataSchema == null && dataEncoding != null) {
					if (dataEncoding.equalsIgnoreCase(pFormatEncoding)) {
						return potentialFormat;
					}

				}
				if (dataSchema != null && dataEncoding != null) {
					if (dataSchema.equalsIgnoreCase(pFormatSchema)
							&& dataEncoding.equalsIgnoreCase(pFormatEncoding)) {
						return potentialFormat;
					}

				}
				if (dataSchema == null && dataEncoding == null) {
					return potentialFormat;
				}
			}
		}
		return null;
	}
	
         protected IData parseComplexValue(String formatEncoding, String complexValue, String dataMimeType, String formatSchema, IParser parser) throws ExceptionReport {
             IData idata;
             String complexValueCopy = complexValue.toString();
             // encoding is UTF-8 (or nothing and we default to UTF-8)
		// everything that goes to this condition should be inline xml data
		if (StringUtils.isBlank(formatEncoding) || formatEncoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
			try {
				if(!complexValueCopy.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")){
                                    complexValueCopy = complexValueCopy.replace("xsi:schemaLocation", "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation");
				}
				idata = parser.parse(new ByteArrayInputStream(complexValueCopy.getBytes()), dataMimeType, formatSchema);
			} catch(RuntimeException e) {
				throw new ExceptionReport("Error occured, while XML parsing", ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		} else if (formatEncoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
                    // in case encoding is base64
                    // everything that goes to this condition should be inline base64 data
                    idata = getBase64EncodedData(complexValue, parser, dataMimeType, formatSchema);
		} else {
                    throw new ExceptionReport("Unable to generate encoding " + formatEncoding, ExceptionReport.NO_APPLICABLE_CODE);
		}
                return idata;
         }

    //TODO-- Needs testing
    protected IData getBase64EncodedData(String complexValue, IParser parser, String dataMimeType, String formatSchema) throws ExceptionReport {
        File f = null;
        String complexValueCopy = complexValue.toString();

        try {
            f = File.createTempFile("wps" + UUID.randomUUID(), "tmp");

            if (complexValueCopy.startsWith("<xml-fragment")) {
                int startIndex = complexValueCopy.indexOf(">");
                complexValueCopy = complexValueCopy.substring(startIndex + 1);

                int endIndex = complexValueCopy.indexOf("</xml-fragment");
                complexValueCopy = complexValueCopy.substring(0, endIndex);
            }

            FileUtils.write(f, complexValueCopy);

            return parser.parseBase64(new FileInputStream(f), dataMimeType, formatSchema);

        } catch (IOException e) {
            throw new ExceptionReport("Error occured, while Base64 extracting", ExceptionReport.NO_APPLICABLE_CODE, e);
        } finally {
            FileUtils.deleteQuietly(f);
            System.gc();
        }
    }

	/**
	 * Handles the literalData
	 * @param input The client's input
	 * @throws ExceptionReport If the type of the parameter is invalid.
	 */
	private void handleLiteralData(InputType input) throws ExceptionReport {
		String inputID = input.getIdentifier().getStringValue();
		String parameter = input.getData().getLiteralData().getStringValue();
		String xmlDataType = input.getData().getLiteralData().getDataType();
		String uom = input.getData().getLiteralData().getUom();

		InputDescriptionType inputDesc = XMLBeansHelper.findInputByID(inputID, processDesc.getDataInputs());

		if(xmlDataType == null) {
			DomainMetadataType dataType = inputDesc.getLiteralData().getDataType();
			xmlDataType = dataType != null ? dataType.getReference() : null;
		}
		//still null, assume string as default
		if(xmlDataType == null) {
			xmlDataType = BasicXMLTypeFactory.STRING_URI;
		} else if(xmlDataType.contains("http://www.w3.org/TR/xmlschema-2#")){
			xmlDataType = xmlDataType.replace("http://www.w3.org/TR/xmlschema-2#","xs:");
		}
		xmlDataType = xmlDataType.toLowerCase();

		IData parameterObj = null;
		try {
			parameterObj = BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, parameter);
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("The passed parameterValue: " + parameter + " for input " + inputID + " is not of type: " + xmlDataType, ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		//validate allowed values.
		if(inputDesc.getLiteralData().isSetAllowedValues()){
			if((!inputDesc.getLiteralData().isSetAnyValue())){
				ValueType[] allowedValues = inputDesc.getLiteralData().getAllowedValues().getValueArray();
				boolean foundAllowedValue = false;
				for(ValueType allowedValue : allowedValues){
					if(input.getData().getLiteralData().getStringValue().equals(allowedValue.getStringValue())){
						foundAllowedValue = true;

					}
				}
				RangeType[] allowedRanges = {};
				if(parameterObj instanceof LiteralIntBinding || parameterObj instanceof LiteralDoubleBinding || parameterObj instanceof LiteralShortBinding || parameterObj instanceof LiteralFloatBinding || parameterObj instanceof LiteralLongBinding || parameterObj instanceof LiteralByteBinding){

					allowedRanges = inputDesc.getLiteralData().getAllowedValues().getRangeArray();
					for(RangeType allowedRange : allowedRanges){
						foundAllowedValue = checkRange(parameterObj, allowedRange);
					}
				}

				if(!foundAllowedValue && (allowedValues.length!=0 || allowedRanges.length!=0)){
					throw new ExceptionReport("Input with ID " + inputID + " does not contain an allowed value. See ProcessDescription.", ExceptionReport.INVALID_PARAMETER_VALUE);
				}

			}
		}

		if(parameterObj == null) {
			throw new ExceptionReport("XML datatype as LiteralParameter is not supported by the server: dataType " + xmlDataType,
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		if(uom != null && !uom.equals("")){
			if(parameterObj instanceof AbstractLiteralDataBinding){
				((AbstractLiteralDataBinding)parameterObj).setUnitOfMeasurement(uom);
			}
		}

		//enable maxxoccurs of parameters with the same name.
		if(inputData.containsKey(inputID)) {
			List<IData> list = inputData.get(inputID);
			list.add(parameterObj);
		}
		else {
			List<IData> list = new ArrayList<IData>();
			list.add(parameterObj);
			inputData.put(inputID, list);
		}

	}

	/**
	 * Handles the literalData
	 * @param input The client's input
	 * @throws ExceptionReport If the type of the parameter is invalid.
	 */
	private void handleLiteralData(DataInputType input) throws ExceptionReport {
		String inputID = input.getId();
		String parameter = "";
		try {
			parameter = XMLUtil.nodeToString(input.getData().getDomNode().getFirstChild());
		} catch (TransformerFactoryConfigurationError | TransformerException e1) {
			LOGGER.error("Could not parse supposed LiteralData. " + input.toString(), e1);
		}

		net.opengis.wps.x20.InputDescriptionType inputDesc = XMLBeansHelper.findInputByID(inputID, processOffering.getProcess());
				
		LiteralDataDomain literalDataDomain = ((LiteralDataType)inputDesc.getDataDescription()).getLiteralDataDomainArray(0);
		
		
		net.opengis.ows.x20.DomainMetadataType dataType = literalDataDomain.getDataType();
		String xmlDataType = dataType != null ? dataType.getReference() : null;
		
		//still null, assume string as default
		if(xmlDataType == null) {
			xmlDataType = BasicXMLTypeFactory.STRING_URI;
		} else if(xmlDataType.contains("http://www.w3.org/TR/xmlschema-2#")){
			xmlDataType = xmlDataType.replace("http://www.w3.org/TR/xmlschema-2#","xs:");
		}
		xmlDataType = xmlDataType.toLowerCase();

		IData parameterObj = null;
		try {
			parameterObj = BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, parameter);
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("The passed parameterValue: " + parameter + " for input " + inputID + " is not of type: " + xmlDataType, ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		//validate allowed values.
		if(literalDataDomain.isSetAllowedValues()){
			if((!literalDataDomain.isSetAnyValue())){
				net.opengis.ows.x20.ValueType[] allowedValues = literalDataDomain.getAllowedValues().getValueArray();
				boolean foundAllowedValue = false;
				for(net.opengis.ows.x20.ValueType allowedValue : allowedValues){
					if(parameter.equals(allowedValue.getStringValue())){
						foundAllowedValue = true;

					}
				}
				net.opengis.ows.x20.RangeType[] allowedRanges = {};
				if(parameterObj instanceof LiteralIntBinding || parameterObj instanceof LiteralDoubleBinding || parameterObj instanceof LiteralShortBinding || parameterObj instanceof LiteralFloatBinding || parameterObj instanceof LiteralLongBinding || parameterObj instanceof LiteralByteBinding){

					allowedRanges = literalDataDomain.getAllowedValues().getRangeArray();
					for(net.opengis.ows.x20.RangeType allowedRange : allowedRanges){
						foundAllowedValue = checkRangeV200(parameterObj, allowedRange);
					}
				}

				if(!foundAllowedValue && (allowedValues.length!=0 || allowedRanges.length!=0)){
					throw new ExceptionReport("Input with ID " + inputID + " does not contain an allowed value. See ProcessDescription.", ExceptionReport.INVALID_PARAMETER_VALUE);
				}

			}
		}

		if(parameterObj == null) {
			throw new ExceptionReport("XML datatype as LiteralParameter is not supported by the server: dataType " + xmlDataType,
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		//enable maxxoccurs of parameters with the same name.
		if(inputData.containsKey(inputID)) {
			List<IData> list = inputData.get(inputID);
			list.add(parameterObj);
		}
		else {
			List<IData> list = new ArrayList<IData>();
			list.add(parameterObj);
			inputData.put(inputID, list);
		}

	}
	
	private boolean checkRange(IData parameterObj, RangeType allowedRange){

		List<?> l = allowedRange.getRangeClosure();

		/*
		 * no closure info or RangeClosure is "closed", so include boundaries
		 */
		if(l == null || l.isEmpty() || l.get(0).equals("closed")){

			if((parameterObj instanceof LiteralIntBinding)){
				int min = new Integer(allowedRange.getMinimumValue().getStringValue());
				int max = new Integer(allowedRange.getMaximumValue().getStringValue());
				if((Integer)(parameterObj.getPayload())>=min && (Integer)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralDoubleBinding)){
				Double min = new Double(allowedRange.getMinimumValue().getStringValue());
				Double max = new Double(allowedRange.getMaximumValue().getStringValue());
				if((Double)(parameterObj.getPayload())>=min && (Double)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralShortBinding)){
				Short min = new Short(allowedRange.getMinimumValue().getStringValue());
				Short max = new Short(allowedRange.getMaximumValue().getStringValue());
				if((Short)(parameterObj.getPayload())>=min && (Short)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralFloatBinding)){
				Float min = new Float(allowedRange.getMinimumValue().getStringValue());
				Float max = new Float(allowedRange.getMaximumValue().getStringValue());
				if((Float)(parameterObj.getPayload())>=min && (Float)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralLongBinding)){
				Long min = new Long(allowedRange.getMinimumValue().getStringValue());
				Long max = new Long(allowedRange.getMaximumValue().getStringValue());
				if((Long)(parameterObj.getPayload())>=min && (Long)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralByteBinding)){
				Byte min = new Byte(allowedRange.getMinimumValue().getStringValue());
				Byte max = new Byte(allowedRange.getMaximumValue().getStringValue());
				if((Byte)(parameterObj.getPayload())>=min && (Byte)parameterObj.getPayload()<=max){
					return true;
				}
			}
			return false;
		}
		/*
		 * TODO:implement other closure cases
		 */

		return false;
	}

	private boolean checkRangeV200(IData parameterObj, net.opengis.ows.x20.RangeType allowedRange){

		List<?> l = allowedRange.getRangeClosure();

		/*
		 * no closure info or RangeClosure is "closed", so include boundaries
		 */
		if(l == null || l.isEmpty() || l.get(0).equals("closed")){

			if((parameterObj instanceof LiteralIntBinding)){
				int min = new Integer(allowedRange.getMinimumValue().getStringValue());
				int max = new Integer(allowedRange.getMaximumValue().getStringValue());
				if((Integer)(parameterObj.getPayload())>=min && (Integer)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralDoubleBinding)){
				Double min = new Double(allowedRange.getMinimumValue().getStringValue());
				Double max = new Double(allowedRange.getMaximumValue().getStringValue());
				if((Double)(parameterObj.getPayload())>=min && (Double)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralShortBinding)){
				Short min = new Short(allowedRange.getMinimumValue().getStringValue());
				Short max = new Short(allowedRange.getMaximumValue().getStringValue());
				if((Short)(parameterObj.getPayload())>=min && (Short)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralFloatBinding)){
				Float min = new Float(allowedRange.getMinimumValue().getStringValue());
				Float max = new Float(allowedRange.getMaximumValue().getStringValue());
				if((Float)(parameterObj.getPayload())>=min && (Float)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralLongBinding)){
				Long min = new Long(allowedRange.getMinimumValue().getStringValue());
				Long max = new Long(allowedRange.getMaximumValue().getStringValue());
				if((Long)(parameterObj.getPayload())>=min && (Long)parameterObj.getPayload()<=max){
					return true;
				}
			}
			if((parameterObj instanceof LiteralByteBinding)){
				Byte min = new Byte(allowedRange.getMinimumValue().getStringValue());
				Byte max = new Byte(allowedRange.getMaximumValue().getStringValue());
				if((Byte)(parameterObj.getPayload())>=min && (Byte)parameterObj.getPayload()<=max){
					return true;
				}
			}
			return false;
		}
		/*
		 * TODO:implement other closure cases
		 */

		return false;
	}
	
	/**
	 * Handles the ComplexValueReference
	 * @param input The client input
	 * @throws ExceptionReport If the input (as url) is invalid, or there is an error while parsing the XML.
	 */
	private void handleComplexValueReference(InputType input) throws ExceptionReport{
		String inputID = input.getIdentifier().getStringValue();

		ReferenceStrategyRegister register = ReferenceStrategyRegister.getInstance();
		ReferenceInputStream stream = register.resolveReference(new InputReference(input));

		String dataURLString = input.getReference().getHref();
		//dataURLString = URLDecoder.decode(dataURLString);
		//dataURLString = dataURLString.replace("&amp;", "");
		LOGGER.debug("Loading data from: " + dataURLString);


		/**
		 * initialize data format with default values defaults and overwrite with defaults from request if applicable
		 */
		InputDescriptionType inputPD = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputPD = tempDesc;
				break;
			}
		}
		if(inputPD == null) { // check if there is a corresponding input identifier in the process description
			LOGGER.debug("Input cannot be found in description for " + this.processDesc.getIdentifier().getStringValue() + "," + inputID);
			throw new RuntimeException("Input cannot be found in description for " + this.processDesc.getIdentifier().getStringValue() + "," + inputID);
		}

		//select parser

		//1. mimeType set?
		//yes--> set it
			//1.1 schema/encoding set?
			//yes-->set it
			//not-->set default values for parser with matching mime type

		//no--> look in http stream
		//2. mimeType set in http stream
			//yes -->set it
				//2.1 schema/encoding set?
				//yes-->set it
				//not-->set default values for parser with matching mime type
			//no--> schema or/and encoding are set?
					//yes-->use it, look if only one mime type can be found
					//not-->use default values



		String schema = null;
		String mimeType = null;
		String encoding = null;

		// overwrite with data format from request if appropriate
		InputReferenceType referenceData = input.getReference();

		if (referenceData.isSetMimeType() && referenceData.getMimeType() != null){
			//mime type in request
			mimeType = referenceData.getMimeType();
			ComplexDataDescriptionType format = null;

			String defaultMimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();

			boolean canUseDefault = false;
			if(defaultMimeType.equalsIgnoreCase(mimeType)){
				ComplexDataDescriptionType potentialFormat = inputPD.getComplexData().getDefault().getFormat();
				if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
					if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema())){
						canUseDefault = true;
						format = potentialFormat;
					}
				}
				if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
					if(referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
						canUseDefault = true;
						format = potentialFormat;
					}

				}
				if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
					if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
						canUseDefault = true;
						format = potentialFormat;
					}

				}
				if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
					canUseDefault = true;
					format = potentialFormat;
				}

			}
			if(!canUseDefault){
				 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
				 for(ComplexDataDescriptionType potentialFormat : formats){
					 if(potentialFormat.getMimeType().equalsIgnoreCase(mimeType)){
						 if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
								if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema())){
									format = potentialFormat;
								}
							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
								if(referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
									format = potentialFormat;
								}

							}
							if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
								if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
									format = potentialFormat;
								}

							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
								format = potentialFormat;
							}
					 }
				 }
			}
			if(format == null){
				throw new ExceptionReport("Possibly multiple or none matching generators found for the input data with id = \"" + inputPD.getIdentifier().getStringValue() + "\". Is the MimeType (\"" + referenceData.getMimeType() + "\") correctly set?", ExceptionReport.INVALID_PARAMETER_VALUE);
				//throw new ExceptionReport("Could not determine format of the input data (id= \"" + inputPD.getIdentifier().getStringValue() + "\"), given the mimetype \"" + referenceData.getMimeType() + "\"", ExceptionReport.INVALID_PARAMETER_VALUE);

			}

			mimeType = format.getMimeType();

			if(format.isSetEncoding()){
				//no encoding provided--> select default one for mimeType
				encoding = format.getEncoding();
			}

			if(format.isSetSchema()){
				//no encoding provided--> select default one for mimeType
				schema = format.getSchema();
			}

		}else{
			// mimeType not in request, fetch mimetype from reference response
            mimeType = stream.getMimeType();
            if(mimeType.contains("GML2")){
                mimeType = "text/xml; subtype=gml/2.0.0";
            }
            if(mimeType.contains("GML3")){
                mimeType = "text/xml; subtype=gml/3.0.0";
            }
            ComplexDataDescriptionType format = null;

            if(mimeType != null){
                String defaultMimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();

                boolean canUseDefault = false;
                if(defaultMimeType.equalsIgnoreCase(mimeType)){
                    ComplexDataDescriptionType potentialFormat = inputPD.getComplexData().getDefault().getFormat();
                    if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
                        if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema())){
                            canUseDefault = true;
                            format = potentialFormat;
                        }
                    }
                    if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
                        if(referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
                            canUseDefault = true;
                            format = potentialFormat;
                        }

                    }
                    if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
                        if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
                            canUseDefault = true;
                            format = potentialFormat;
                        }

                    }
                    if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
                        canUseDefault = true;
                        format = potentialFormat;
                    }

                }
                if(!canUseDefault){
                     ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
                     for(ComplexDataDescriptionType potentialFormat : formats){
                         if(!StringUtils.isBlank(potentialFormat.getMimeType()) && potentialFormat.getMimeType().equalsIgnoreCase(mimeType)){
                             if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
                                    if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema())){
                                        format = potentialFormat;
                                    }
                                }
                                if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
                                    if(referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
                                        format = potentialFormat;
                                    }

                                }
                                if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
                                    if(referenceData.getSchema().equalsIgnoreCase(potentialFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potentialFormat.getEncoding())){
                                        format = potentialFormat;
                                    }

                                }
                                if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
                                    format = potentialFormat;
                                }
                         }
                     }
                }
                if(format == null){
                    //throw new ExceptionReport("Could not determine intput format. Possibly multiple or none matching generators found. MimeType Set?", ExceptionReport.INVALID_PARAMETER_VALUE);
                    // TODO Review error message
                    throw new ExceptionReport("Could not determine input format because none of the supported formats match the given schema (\"" + referenceData.getSchema() + "\") and encoding (\"" + referenceData.getEncoding() + "\"). (A mimetype was not specified)", ExceptionReport.INVALID_PARAMETER_VALUE);

                }

                mimeType = format.getMimeType();

                if(format.isSetEncoding()){
                    //no encoding provided--> select default one for mimeType
                    encoding = format.getEncoding();
                }

                if(format.isSetSchema()){
                    //no encoding provided--> select default one for mimeType
                    schema = format.getSchema();
                }
            }

			if(mimeType==null && !referenceData.isSetEncoding() && !referenceData.isSetSchema()){
					//nothing set, use default values
					schema = inputPD.getComplexData().getDefault().getFormat().getSchema();
					mimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();
					encoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();

			}else{
					//do a smart search an look if a mimeType can be found for either schema and/or encoding

				if(mimeType==null){
					if(referenceData.isSetEncoding() && !referenceData.isSetSchema()){
							//encoding set only
							ComplexDataDescriptionType encodingFormat = null;
							String defaultEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
							int found = 0;
							String foundEncoding = null;
							if(defaultEncoding.equalsIgnoreCase(referenceData.getEncoding())){
								foundEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
								encodingFormat = inputPD.getComplexData().getDefault().getFormat();
								found += 1;
							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getEncoding())){
										 foundEncoding = tempFormat.getEncoding();
										 encodingFormat = tempFormat;
										 found += 1;
									 }
								 }
							}

							if(found == 1){
								encoding = foundEncoding;
								mimeType = encodingFormat.getMimeType();
								if(encodingFormat.isSetSchema()){
									schema = encodingFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						}
						if(referenceData.isSetSchema() && !referenceData.isSetEncoding()){
							//schema set only
							ComplexDataDescriptionType schemaFormat = null;
							String defaultSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
							int found = 0;
							String foundSchema = null;
							if(defaultSchema.equalsIgnoreCase(referenceData.getSchema())){
								foundSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
								schemaFormat = inputPD.getComplexData().getDefault().getFormat();
								found += 1;
							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getSchema())){
										 foundSchema = tempFormat.getSchema();
										 schemaFormat =tempFormat;
										 found += 1;
									 }
								 }
							}

							if(found == 1){
								schema = foundSchema;
								mimeType = schemaFormat.getMimeType();
								if(schemaFormat.isSetEncoding()){
									encoding = schemaFormat.getEncoding();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						}
						if(referenceData.isSetEncoding() && referenceData.isSetSchema()){
							//schema and encoding set


							//encoding
							String defaultEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();

							List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
							if(defaultEncoding.equalsIgnoreCase(referenceData.getEncoding())){
								foundEncodingList.add(inputPD.getComplexData().getDefault().getFormat());


							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getEncoding())){
										 foundEncodingList.add(tempFormat);
									 }
							}




							//schema
							List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
							String defaultSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
							if(defaultSchema.equalsIgnoreCase(referenceData.getSchema())){
								foundSchemaList.add(inputPD.getComplexData().getDefault().getFormat());
							}else{
								 formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getSchema())){
										 foundSchemaList.add(tempFormat);
									 }
								 }
							}


							//results
							ComplexDataDescriptionType foundCommonFormat = null;
							for(ComplexDataDescriptionType encodingFormat : foundEncodingList){
								for(ComplexDataDescriptionType schemaFormat : foundSchemaList){
									if(encodingFormat.equals(schemaFormat)){
										foundCommonFormat = encodingFormat;
									}
								}


							}

							if(foundCommonFormat!=null){
								mimeType = foundCommonFormat.getMimeType();
								if(foundCommonFormat.isSetEncoding()){
									encoding = foundCommonFormat.getEncoding();
								}
								if(foundCommonFormat.isSetSchema()){
									schema = foundCommonFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}

						}

					}

				}
			}

		}


		LOGGER.debug("Loading parser for: schema = \""+ schema
				+ "\" , mimetype = \"" + mimeType
				+ "\", encoding = \"" + encoding + "\"");

		IParser parser = null;
		try {
			Class<?> algorithmInputClass = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputID);
			if(algorithmInputClass == null) {
				throw new RuntimeException("Could not determine internal input class for input" + inputID);
			}
			LOGGER.info("Looking for matching Parser ..." +
					" schema: \"" + schema +
					"\", mimeType: \"" + mimeType +
					"\", encoding: \"" + encoding + "\"");

			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInputClass);

			if(parser == null) {
				throw new ExceptionReport("Error. No applicable parser found for schema=\"" + schema + "\", mimeType=\"" + mimeType + "\", encoding=\"" + encoding + "\"", ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data", ExceptionReport.NO_APPLICABLE_CODE, e);
		}


			/****PROXY*****/
			/*String decodedURL = URLDecoder.decode(dataURLString);
			decodedURL = decodedURL.replace("&amp;", "&");
			if(decodedURL.indexOf("&BBOX")==-1){
				decodedURL = decodedURL.replace("BBOX", "&BBOX");
				decodedURL = decodedURL.replace("outputFormat", "&outputFormat");
				decodedURL = decodedURL.replace("SRS", "&SRS");
				decodedURL = decodedURL.replace("REQUEST", "&REQUEST");
				decodedURL = decodedURL.replace("VERSION", "&VERSION");
				decodedURL = decodedURL.replace("SERVICE", "&SERVICE");
				decodedURL = decodedURL.replace("format", "&format");
			}*/


			//TODO lookup WFS -- we can't do that here.
//			if(dataURLString.toUpperCase().contains("REQUEST=GETFEATURE") &&
//				dataURLString.toUpperCase().contains("SERVICE=WFS")){
//					if(parser instanceof SimpleGMLParser){
//						parser = new GML2BasicParser();
//					}
//					if(parser instanceof GML2BasicParser && !dataURLString.toUpperCase().contains("OUTPUTFORMAT=GML2")){
//						//make sure we get GML2
//						dataURLString = dataURLString+"&outputFormat=GML2";
//					}
//					if(parser instanceof GML3BasicParser && !dataURLString.toUpperCase().contains("OUTPUTFORMAT=GML3")){
//						//make sure we get GML3
//						dataURLString = dataURLString+"&outputFormat=GML3";
//					}
//			}



			IData parsedInputData = parser.parse(stream, mimeType, schema);

			//enable maxxoccurs of parameters with the same name.
			if(inputData.containsKey(inputID)) {
				List<IData> list = inputData.get(inputID);
				list.add(parsedInputData);
				inputData.put(inputID, list);
			}
			else {
				List<IData> list = new ArrayList<IData>();
				list.add(parsedInputData);
				inputData.put(inputID, list);
			}


	}

	/**
	 * Handles BBoxValue
	 * @param input The client input
	 */
    private void handleBBoxValue(InputType input)
            throws ExceptionReport {

        IData envelope = parseBoundingBox(input.getData().getBoundingBoxData());

        List<IData> resultList = inputData.get(input.getIdentifier()
                .getStringValue());
        if (resultList == null) {
            inputData.put(input.getIdentifier().getStringValue(), resultList
                    = new ArrayList<IData>(1));
        }
        resultList.add(envelope);

    }

    private IData parseBoundingBox(BoundingBoxType bbt)
            throws ExceptionReport {
        final BigInteger dim = bbt.getDimensions();
        final double[] lower, upper;

        if (dim != null && (dim.compareTo(INT_MAX) > 0 ||
                            dim.compareTo(INT_MIN) < 0)) {
            throw new ExceptionReport(
                      String.format("Unsupported BoundingBox dimension %s. Has to be betweeen %s and %s!",
                                    dim, INT_MIN, INT_MAX),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }

        try {
            lower = parseCoordinate(bbt.getLowerCorner());
        } catch (NumberFormatException e) {
            throw new ExceptionReport("Invalid lower corner",
                      ExceptionReport.INVALID_PARAMETER_VALUE, e);
        }

        try {
            upper = parseCoordinate(bbt.getUpperCorner());
        } catch (NumberFormatException e) {
            throw new ExceptionReport("Invalid upper corner",
                      ExceptionReport.INVALID_PARAMETER_VALUE, e);
        }

        if (upper.length != lower.length) {
            throw new ExceptionReport(
                      String.format("Mismatching BoundingBox dimensions: %s vs %s!",
                                    upper.length, lower.length),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }

        if (dim != null && lower.length != dim.intValue()) {
            throw new ExceptionReport(
                      String.format("Mismatching BoundingBox dimensions: %s vs %s!",
                                    dim.intValue(), lower.length),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }
        return new BoundingBoxData(lower, upper, bbt.getCrs());
    }

	/**
	 * Handles BBoxValue
	 * @param input The client input
	 */
    private void handleBBoxValue(DataInputType input)
            throws ExceptionReport {
    	
    	net.opengis.ows.x20.BoundingBoxType boundingBoxType = null;
		try {
			boundingBoxType = net.opengis.ows.x20.BoundingBoxType.Factory.parse(input.getData().getDomNode());
		} catch (XmlException e) {
			LOGGER.error("XmlException occurred while trying to parse bounding box: " + (boundingBoxType == null ? null : boundingBoxType.toString()), e);
		}
    	
        IData envelope = parseBoundingBox(boundingBoxType);

        List<IData> resultList = inputData.get(input.getId());
        if (resultList == null) {
            inputData.put(input.getId(), resultList
                    = new ArrayList<IData>(1));
        }
        resultList.add(envelope);

    }

    private IData parseBoundingBox(net.opengis.ows.x20.BoundingBoxType bbt)
            throws ExceptionReport {
        final BigInteger dim = bbt.getDimensions();
        final double[] lower, upper;

        if (dim != null && (dim.compareTo(INT_MAX) > 0 ||
                            dim.compareTo(INT_MIN) < 0)) {
            throw new ExceptionReport(
                      String.format("Unsupported BoundingBox dimension %s. Has to be betweeen %s and %s!",
                                    dim, INT_MIN, INT_MAX),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }

        try {
            lower = parseCoordinate(bbt.getLowerCorner());
        } catch (NumberFormatException e) {
            throw new ExceptionReport("Invalid lower corner",
                      ExceptionReport.INVALID_PARAMETER_VALUE, e);
        }

        try {
            upper = parseCoordinate(bbt.getUpperCorner());
        } catch (NumberFormatException e) {
            throw new ExceptionReport("Invalid upper corner",
                      ExceptionReport.INVALID_PARAMETER_VALUE, e);
        }

        if (upper.length != lower.length) {
            throw new ExceptionReport(
                      String.format("Mismatching BoundingBox dimensions: %s vs %s!",
                                    upper.length, lower.length),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }

        if (dim != null && lower.length != dim.intValue()) {
            throw new ExceptionReport(
                      String.format("Mismatching BoundingBox dimensions: %s vs %s!",
                                    dim.intValue(), lower.length),
                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }
        return new BoundingBoxData(lower, upper, bbt.getCrs());
    }
    
    private double[] parseCoordinate(List<?> ordinates)
            throws NumberFormatException {
        List<Number> coordinate = new ArrayList<Number>(ordinates.size());
        for (Object o  : ordinates) {
            if (o instanceof Number) {
                coordinate.add((Number) o);
            } else {
                coordinate.add(Double.parseDouble(String.valueOf(o)));
            }
        }
        return Doubles.toArray(coordinate);
    }

	/**
	 * Gets the resulting InputLayers from the parser
	 * @return A map with the parsed input
	 */
	public Map<String, List<IData>> getParsedInputData(){
		return inputData;
	}


//	private InputStream retrievingZippedContent(URLConnection conn) throws IOException{
//		String contentType = conn.getContentEncoding();
//		if(contentType != null && contentType.equals("gzip")) {
//			return new GZIPInputStream(conn.getInputStream());
//		}
//		else{
//			return conn.getInputStream();
//		}
//	}
}
