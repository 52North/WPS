/**
 * ﻿Copyright (C) 2014 - 2014 52°North Initiative for Geospatial Open Source
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
package de.tudresden.gis.fusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.ExceptionReport;

import de.tudresden.gis.fusion.operation.IOperation;
import de.tudresden.gis.fusion.operation.metadata.IIODescription;

public class FusionAlgorithm extends AbstractSelfDescribingAlgorithm {
	
	IOperation fusionOperation;
	
	public FusionAlgorithm(IOperation fusionOperation){
		this.fusionOperation = fusionOperation;
	}

	@Override
	public Map<String,IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
		
		Map<String,de.tudresden.gis.fusion.data.IData> fusionInputs = new HashMap<String,de.tudresden.gis.fusion.data.IData>();
		for(Map.Entry<String,List<IData>> input : inputData.entrySet()){
			fusionInputs.put(input.getKey(), DataTransformer.transformIData(input.getValue()));
		}
		Map<String, IData> resultData = new HashMap<String,IData>();
		Map<String,de.tudresden.gis.fusion.data.IData> fusionResults = this.fusionOperation.execute(fusionInputs);
		for(Map.Entry<String,de.tudresden.gis.fusion.data.IData> result : fusionResults.entrySet()){
			resultData.put(result.getKey(), DataTransformer.transformIData(result.getValue()));
		}
		
		return resultData;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		Collection<IIODescription> descriptions = fusionOperation.getProfile().getInputDescriptions();
		for(IIODescription description : descriptions){
			if(description.getIdentifier().asString().equals(id))
				return DataTransformer.getSupportedClass(description);
		}
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		Collection<IIODescription> descriptions = fusionOperation.getProfile().getOutputDescriptions();
		for(IIODescription description : descriptions){
			if(description.getIdentifier().asString().equals(id))
				return DataTransformer.getSupportedClass(description);
		}
		return null;
	}

	@Override
	public List<String> getInputIdentifiers() {
		Collection<IIODescription> descriptions = fusionOperation.getProfile().getInputDescriptions();
		List<String> identifiers = new ArrayList<String>();
		for(IIODescription description : descriptions){
			identifiers.add(description.getIdentifier().asString());
		}
		return identifiers;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		Collection<IIODescription> descriptions = fusionOperation.getProfile().getOutputDescriptions();
		List<String> identifiers = new ArrayList<String>();
		for(IIODescription description : descriptions){
			identifiers.add(description.getIdentifier().asString());
		}
		return identifiers;
	}
	
	@Override
	public String getWellKnownName() {
		return "de.tudresden.gis.fusion.algorithm." + this.fusionOperation.getProfile().getIdentifier().asString().split("#")[1];
	}

}
