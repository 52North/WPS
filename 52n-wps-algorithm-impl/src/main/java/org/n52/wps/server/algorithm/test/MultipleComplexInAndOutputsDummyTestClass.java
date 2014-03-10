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
package org.n52.wps.server.algorithm.test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

/**
 * This class can be used to test sending multiple complex inputs and receiving multiple complex outputs.
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class MultipleComplexInAndOutputsDummyTestClass extends AbstractSelfDescribingAlgorithm {
	private final String inputID1 = "ComplexInputData1";
	private final String inputID2 = "ComplexInputData2";
	private final String inputID3 = "LiteralInputData";
	private final String inputID4 = "BBOXInputData";
	private final String outputID1 = "ComplexOutputData1";
	private final String outputID2 = "ComplexOutputData2";
	private final String outputID3 = "LiteralOutputData";
	private final String outputID4 = "BBOXOutputData";
	
	private List<String> errors = new ArrayList<String>();	

	public List<String> getErrors() {
		return errors;
	}

	public Class<?> getInputDataType(String id) {
		if (id.equalsIgnoreCase(inputID1)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(inputID2)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(inputID3)) {
			return LiteralStringBinding.class;
		}
		if (id.equalsIgnoreCase(inputID4)) {
			return GTReferenceEnvelope.class;
		}
		return null;
		
	}
	
	@Override
	public BigInteger getMinOccurs(String identifier){
		return new BigInteger("0");
	}	

	public Class<?> getOutputDataType(String id) {
		if (id.equalsIgnoreCase(outputID1)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(outputID2)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(outputID3)) {
			return LiteralStringBinding.class;
		}
		if (id.equalsIgnoreCase(outputID4)) {
			return GTReferenceEnvelope.class;
		}
		return null;
	}
	
	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(inputID1);
		identifierList.add(inputID2);
		identifierList.add(inputID3);
		identifierList.add(inputID4);
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(outputID1);
		identifierList.add(outputID2);
		identifierList.add(outputID3);
		identifierList.add(outputID4);
		return identifierList;
	}	

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		HashMap<String,IData> result = new HashMap<String,IData>();
		if(inputData.containsKey(inputID1)){
			
			IData data = inputData.get(inputID1).get(0);
			
			if(data instanceof GenericFileDataBinding){
				GenericFileDataBinding genericFileDataBinding = (GenericFileDataBinding)data;
				GenericFileData genericFileData = genericFileDataBinding.getPayload();
				try {
					result.put(outputID1, new GenericFileDataBinding(new GenericFileData(genericFileData.getBaseFile(false), genericFileData.getMimeType())));
				} catch (IOException e) {
					errors.add(e.getMessage());
				}
			}else{
				result.put(outputID1, data);
			}
		}
		if(inputData.containsKey(inputID2)){
			result.put(outputID2, inputData.get(inputID2).get(0));
			
			IData data = inputData.get(inputID2).get(0);
			
			if(data instanceof GenericFileDataBinding){
				GenericFileDataBinding genericFileDataBinding = (GenericFileDataBinding)data;
				GenericFileData genericFileData = genericFileDataBinding.getPayload();
				try {
					result.put(outputID2, new GenericFileDataBinding(new GenericFileData(genericFileData.getBaseFile(false), genericFileData.getMimeType())));
				} catch (IOException e) {
					errors.add(e.getMessage());
				}
			}else{
				result.put(outputID2, data);
			}
			
		}
		if(inputData.containsKey(inputID3)){
			result.put(outputID3, inputData.get(inputID3).get(0));
		}
		if(inputData.containsKey(inputID4)){
			result.put(outputID4, inputData.get(inputID4).get(0));
		}
			
		return result;
	}
	
	@Override
	public String[] getSupportedCRSForBBOXInput(String identifier){
		String[] supportedCRS = new String[2];
		supportedCRS[0] = "EPSG:4328";
		supportedCRS[1] = "EPSG:5628";
		
		return supportedCRS;
	}
	
	@Override
	public String[] getSupportedCRSForBBOXOutput(String identifier){
		String[] supportedCRS = new String[2];
		supportedCRS[0] = "EPSG:4328";
		supportedCRS[1] = "EPSG:5628";
		
		return supportedCRS;
	}
}