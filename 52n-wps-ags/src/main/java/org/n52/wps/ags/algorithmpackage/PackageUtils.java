/**
 * ﻿Copyright (C) 2009 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.ags.algorithmpackage;

import java.io.File;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class PackageUtils {

	public static Class getInputDataType(AlgorithmPackage mco, String id) {
		InputDescriptionType[] inputs = mco.getProcessDescription().getDataInputs().getInputArray();

		for(InputDescriptionType input : inputs){

			if (input.getIdentifier().getStringValue().equalsIgnoreCase(id)){

				//Literal Input
				if(input.isSetLiteralData()){
					String datatype = input.getLiteralData().getDataType().getStringValue();
					if(datatype.equalsIgnoreCase("string")){
						return LiteralStringBinding.class;
					}
					if(datatype.equalsIgnoreCase("boolean")){
						return LiteralBooleanBinding.class;
					}
					if(datatype.equalsIgnoreCase("float")){
						return LiteralFloatBinding.class;
					}
					if(datatype.equalsIgnoreCase("double")){
						return LiteralDoubleBinding.class;
					}
					if(datatype.equalsIgnoreCase("int")){
						return LiteralIntBinding.class;
					}
					if(datatype.equalsIgnoreCase("integer")){
						return LiteralIntBinding.class;
					}
				}

				//Complex Output
				if(input.isSetComplexData()){
					return GenericFileDataWithGTBinding.class;
				}
			}
		}

		return null;
	}

	public static Class getOutputDataType(AlgorithmPackage mco, String id) {
		OutputDescriptionType[] outputs = mco.getProcessDescription().getProcessOutputs().getOutputArray();

		for(OutputDescriptionType output : outputs){

			if (output.getIdentifier().getStringValue().equalsIgnoreCase(id)){

				//Literal Output
				if(output.isSetLiteralOutput()){
					String datatype = output.getLiteralOutput().getDataType().getStringValue();
					if(datatype.equalsIgnoreCase("string")){
						return LiteralStringBinding.class;
					}
					if(datatype.equalsIgnoreCase("boolean")){
						return LiteralBooleanBinding.class;
					}
					if(datatype.equalsIgnoreCase("float")){
						return LiteralFloatBinding.class;
					}
					if(datatype.equalsIgnoreCase("double")){
						return LiteralDoubleBinding.class;
					}
					if(datatype.equalsIgnoreCase("int")){
						return LiteralIntBinding.class;
					}
					if(datatype.equalsIgnoreCase("integer")){
						return LiteralIntBinding.class;
					}
				}

				//Complex Output
				if(output.isSetComplexOutput()){
					return GenericFileDataWithGTBinding.class;
				}
			}
		}
		return null;
	}


	public static String loadSingleDataItem(IData dataItem, File workspaceDir){

		Object payload = dataItem.getPayload();
		String fileName = null;

		//File
		if (payload instanceof GenericFileDataWithGT){
			GenericFileDataWithGT gfd = (GenericFileDataWithGT)payload;
			fileName = gfd.writeData(workspaceDir);
		}

		//String
		if (payload instanceof String)
			fileName = (String) payload;

		//Float
		if (payload instanceof Float)
			fileName = ((Float)payload).toString();

		//Integer
		if (payload instanceof Integer)
			fileName = ((Integer)payload).toString();

		//Double
		if (payload instanceof Double)
			fileName = ((Double)payload).toString();

		return fileName;
	}


}
