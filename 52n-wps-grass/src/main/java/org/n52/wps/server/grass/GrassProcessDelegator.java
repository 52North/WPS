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
package org.n52.wps.server.grass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.ows.x11.CodeType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.grass.io.GrassIOHandler;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class GrassProcessDelegator extends GenericGrassAlgorithm{

	private static Logger LOGGER = LoggerFactory.getLogger(GrassProcessDelegator.class);

	private String processID;
	private boolean isAddon;
	private ProcessDescription processDescription;
	private List<String> errors;	
	private HashMap<String, Class<?>> complexInputTypes;	
	private HashMap<String, Class<?>> literalInputTypes;
	private HashMap<String, String> outputTypeMimeTypeMap;
	
	private final String dataTypeFloat = "float";
	private final String dataTypeBoolean = "boolean";
	private final String dataTypeString = "string";
	private final String dataTypeInteger ="integer";
	private final String dataTypeDouble = "double";
	
	
	public GrassProcessDelegator(String processID, ProcessDescription processDescriptionType, boolean isAddon){
		this.processID = processID;
		this.isAddon = isAddon;
		this.processDescription = processDescriptionType;
		this.errors = new ArrayList<String>();
		mapInputAndOutputTypes((ProcessDescriptionType) processDescriptionType.getProcessDescriptionType("1.0.0"));		
	}
	
	private void mapInputAndOutputTypes(ProcessDescriptionType processDescriptionType){
		
		complexInputTypes = new HashMap<String, Class<?>>();
		literalInputTypes = new HashMap<String, Class<?>>();
		outputTypeMimeTypeMap = new HashMap<String, String>();
		
		DataInputs inputs = processDescriptionType.getDataInputs();			
		
		for (int j = 0; j < inputs.getInputArray().length; j++) {
			InputDescriptionType input = inputs.getInputArray(j);
			
			CodeType identifierType = input.getIdentifier();
			
			String identifierString = identifierType.getStringValue();
			
			SupportedComplexDataInputType complexData = input.getComplexData();

			if (complexData != null) {

				complexInputTypes.put(identifierString,
						GenericFileDataWithGTBinding.class);

			} else if (input.getLiteralData() != null) {
				
				LiteralInputType literalType = input.getLiteralData();
				
				String datatype = literalType.getDataType().getStringValue();
				
				if(datatype.equals(dataTypeFloat)){
					literalInputTypes.put(identifierString, LiteralFloatBinding.class);
				}else if(datatype.equals(dataTypeBoolean)){
					literalInputTypes.put(identifierString, LiteralBooleanBinding.class);
				}else if(datatype.equals(dataTypeString)){
					literalInputTypes.put(identifierString, LiteralStringBinding.class);
				}else if(datatype.equals(dataTypeInteger)){
					literalInputTypes.put(identifierString, LiteralIntBinding.class);
				}else if(datatype.equals(dataTypeDouble)){
					literalInputTypes.put(identifierString, LiteralDoubleBinding.class);
				}
				
			}
		}
		
		ProcessOutputs pOutputs = processDescriptionType.getProcessOutputs();
		
		for (int i = 0; i < pOutputs.getOutputArray().length; i++) {
			
			OutputDescriptionType oDescType = pOutputs.getOutputArray(i);
			
			SupportedComplexDataType type = oDescType.getComplexOutput();

			String outputIdentifier = oDescType.getIdentifier().getStringValue();
			
			String defaultMimeType = type.getDefault().getFormat().getMimeType();
			
			outputTypeMimeTypeMap.put(outputIdentifier, defaultMimeType);
		}
	}
	
	@Override
	public ProcessDescription getDescription() {
		return processDescription;
	}

	@Override
	public List<String> getErrors() {
		return errors;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(complexInputTypes.containsKey(id)){
			return complexInputTypes.get(id);
		}else if(literalInputTypes.containsKey(id)){
			return literalInputTypes.get(id);
		}else {
			return null;
		}		
	}

	@Override
	public Class<?> getOutputDataType(String id) {		
		return GenericFileDataWithGTBinding.class;
	}

	@Override
	public String getWellKnownName() {
		return processID;
	}

	@Override
	public boolean processDescriptionIsValid(String version) {
		return processDescription.getProcessDescriptionType(version).validate();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		LOGGER.info("Executing GRASS process " + processID + ".");
		
		Map<String, IData> result = new HashMap<String, IData>();

		OutputDefinitionType output = ExecutionContextFactory.getContext().getOutputs().get(0);
		
		String outputSchema = output.getSchema();
		
		String outputMimeType = output.getMimeType();
		
		CodeType outputIdentifierCT = output.getIdentifier();
		
		String outputIdentifier = outputIdentifierCT.getStringValue();	
		
		HashMap<String, List<IData>> firstInputMap = new HashMap<String, List<IData>>();

		for (String key : complexInputTypes.keySet()) {

			if (inputData.containsKey(key)) {
				firstInputMap.put(key, inputData.get(key));
			}
		}
		
		HashMap<String, List<IData>> secondInputMap = new HashMap<String, List<IData>>();
		
		for (String key : literalInputTypes.keySet()) {

			if (inputData.containsKey(key)) {
				secondInputMap.put(key, inputData.get(key));
			}
		}
		
		if(outputMimeType == null || outputMimeType.equals("")){
			outputMimeType = outputTypeMimeTypeMap.get(outputIdentifier);
		}
		
		IData outputFileDB = new GrassIOHandler().executeGrassProcess(
				processID, firstInputMap, secondInputMap, outputIdentifier, outputMimeType, outputSchema, isAddon);
		
		if(outputIdentifier == null || outputIdentifier.equals("")){
			outputIdentifier = "output";
		}
		
		result.put(outputIdentifier, outputFileDB);

		return result;

	}
}
