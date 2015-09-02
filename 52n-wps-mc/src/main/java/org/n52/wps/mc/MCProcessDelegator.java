/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.mc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.iodata.IIOParameter;
import org.n52.movingcode.runtime.iodata.IODataType;
import org.n52.movingcode.runtime.iodata.MediaData;
import org.n52.movingcode.runtime.processors.AbstractProcessor;
import org.n52.movingcode.runtime.processors.ProcessorFactory;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.IAlgorithm;

public class MCProcessDelegator implements IAlgorithm {

	private final String identifier;
	private List<String> errors;
	private ProcessDescriptionType description = null;

	public MCProcessDelegator(final String identifier) {
		this.identifier = identifier;
		this.errors = new ArrayList<String>();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		MovingCodePackage[] allMCs = GlobalRepositoryManager.getInstance().getPackageByFunction(identifier);

		AbstractProcessor processor = null;
		//  try to isolate a package that is supported by a registered processor
		for (MovingCodePackage currentMC : allMCs){
			if (ProcessorFactory.getInstance().supportsPackage(currentMC)){
				processor = ProcessorFactory.getInstance().newProcessor(currentMC);
				break;
			}
		}
		
		// TODO: exit here if processor == null;


		// assign inputs
		for (String inputID : inputData.keySet()) {
			List<IData> iDataList = inputData.get(inputID);
			if (iDataList == null || iDataList.isEmpty() || iDataList.size() == 0) {
				// do nothing
			}
			else {
				// here we can be sure that iDataList contains at least one element
				for (IData iData : iDataList) {

					switch (probeMCDataType(iData)) {
						// simple cases
						case BOOLEAN:
							processor.addData(inputID, iData.getPayload());
							break;
						case DOUBLE:
							processor.addData(inputID, iData.getPayload());
							break;
						case INTEGER:
							processor.addData(inputID, iData.getPayload());
							break;
						case STRING:
							processor.addData(inputID, iData.getPayload());
							break;

							// media data
						case MEDIA:
							GenericFileData gfd = (GenericFileData) iData.getPayload();
							processor.addData(inputID, new MediaData(gfd.getDataStream(), gfd.getMimeType()));
							break;
						default:
							errors.add("The supplied data is not compatible with the MC library.\n"
									+ "Allowed types are BOOLEAN, DOUBLE, INTEGER, STRING, MEDIA.\n"
									+ "Offending InputID: " + inputID);
							throw new IllegalArgumentException("Invalid data passed to process. Offending InputID: "
									+ inputID);
					}
				}

			}

		}

		// How do we declare MimeTypes for Outputs?
		// Do we have enough information to do this?
		//
		// We could use the ExecuteRequest to do this but the WPS might
		// do additional transformation to support more inputs types
		// (e.g. gml for shapefiles)
		//
		// so we shall stick to the defaults until there's a better way
		// to do output probing
		// MC packages for WPS should therefore have only *one* supported
		// type per input or output

		OutputDescriptionType[] wpsOutputs = this.getDescription().getProcessOutputs().getOutputArray();

		for (IIOParameter param : processor.values()) {
			// for all output-only parameters:
			if (param.getDirection() == IIOParameter.Direction.OUT) {
				// fetch the "default" mimeType
				for (OutputDescriptionType wpsOut : wpsOutputs) {
					String outputID = param.getMessageOutputIdentifier();
					if (wpsOut.getIdentifier().getStringValue().equalsIgnoreCase(outputID)) {
						switch (param.getType()) {
							// simple cases
							case BOOLEAN:
								processor.addData(outputID, null);
								break;
							case DOUBLE:
								processor.addData(outputID, null);
								break;
							case INTEGER:
								processor.addData(outputID, null);
								break;
							case STRING:
								processor.addData(outputID, null);
								break;

								// media data
							case MEDIA:
								String mimeType = wpsOut.getComplexOutput().getDefault().getFormat().getMimeType();
								processor.addData(outputID, new MediaData(null, mimeType));
								break;
							default:
								errors.add("Soemthing went wrong assigning the output data types.\n"
										+ "Probably a messed-up process description.\n" + "Offending OutputID: " + outputID);
								throw new IllegalArgumentException("Invalid data passed to process. Offending InputID: "
										+ outputID);
						}
					}
				}
			}
		}

		// check feasibility
		if ( !processor.isFeasible()) {
			errors.add("Feasibility == FALSE for process: " + identifier);
			throw new IllegalArgumentException("For some reason the parameterisation was wrong for process: "
					+ identifier);
		}

		try {
			processor.execute(0); // execute without any timeout
		}
		catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
			throw new RuntimeException("Execution terminated with an error.");
		}
		catch (RuntimeException e) {
			errors.add(e.getMessage());
			throw new RuntimeException("Execution terminated with an error.");
		}
		catch (IOException e) {
			errors.add(e.getMessage());
			throw new RuntimeException("Execution terminated with an error.");
		}

		// create the output
		HashMap<String, IData> result = new HashMap<String, IData>();

		// iterate through all values
		for (IIOParameter param : processor.values()) {
			// select the outputs only
			if (param.isMessageOut()) {
				// we will only add the first element due to wps output multiplicity restrictions
				// actually, there should be exactly one value in the list, but who knows ...
				// check: is there more than one value?
				if (param.size() <= 0) {
					errors.add("Could not retrieve process output for identifier: "
							+ param.getMessageOutputIdentifier() + ". Value list was found empty.");
				}
				switch (param.getType()) {
					case BOOLEAN:
						result.put(param.getMessageOutputIdentifier(), new LiteralBooleanBinding((Boolean) param.get(0)));
						break;
					case DOUBLE:
						result.put(param.getMessageOutputIdentifier(), new LiteralDoubleBinding((Double) param.get(0)));
						break;
					case INTEGER:
						result.put(param.getMessageOutputIdentifier(), new LiteralIntBinding((Integer) param.get(0)));
						break;
					case STRING:
						result.put(param.getMessageOutputIdentifier(), new LiteralStringBinding((String) param.get(0)));
						break;
					case MEDIA:

						MediaData md = (MediaData) param.get(0);
						GenericFileData gfd = new GenericFileData(md.getMediaStream(), md.getMimeType());
						result.put(param.getMessageOutputIdentifier(), new GenericFileDataBinding(gfd));
						break;
				}
			}
		}
		if (errors.size() > 0) {
			throw new RuntimeException("Abnormal termination of execution");
		}

		// TODO: implement return values
		return result;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessDescriptionType getDescription() {
		if (description == null) {
			ProcessDescriptionType originalDescription = GlobalRepositoryManager.getInstance().getProcessDescription(identifier);
			MCProcessRepository.filterProcessDescription(originalDescription);
			description = originalDescription;
		}
		
		return description;
	}

	@Override
	public String getWellKnownName() {
		return getDescription().getIdentifier().getStringValue();
	}

	@Override
	public boolean processDescriptionIsValid() {
		return getDescription().validate();
	}

	@Override
	public Class< ? > getInputDataType(String id) {
		InputDescriptionType[] inputs = this.getDescription().getDataInputs().getInputArray();

		for (InputDescriptionType input : inputs) {

			// Literal Input
			if (input.isSetLiteralData()) {
				String datatype = input.getLiteralData().getDataType().getStringValue();
				if (datatype.equalsIgnoreCase("string")) {
					return LiteralStringBinding.class;
				}
				if (datatype.equalsIgnoreCase("boolean")) {
					return LiteralBooleanBinding.class;
				}
				if (datatype.equalsIgnoreCase("float")) {
					return LiteralFloatBinding.class;
				}
				if (datatype.equalsIgnoreCase("double")) {
					return LiteralDoubleBinding.class;
				}
				if (datatype.equalsIgnoreCase("int")) {
					return LiteralIntBinding.class;
				}
				if (datatype.equalsIgnoreCase("integer")) {
					return LiteralIntBinding.class;
				}
			}

			// Complex Output
			if (input.isSetComplexData()) {
				return GenericFileDataBinding.class;
			}
		}

		return null;
	}

	@Override
	public Class< ? > getOutputDataType(String id) {
		OutputDescriptionType[] outputs = this.getDescription().getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputs) {

			// Literal Output
			if (output.isSetLiteralOutput()) {
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if (datatype.equalsIgnoreCase("string")) {
					return LiteralStringBinding.class;
				}
				if (datatype.equalsIgnoreCase("boolean")) {
					return LiteralBooleanBinding.class;
				}
				if (datatype.equalsIgnoreCase("float")) {
					return LiteralFloatBinding.class;
				}
				if (datatype.equalsIgnoreCase("double")) {
					return LiteralDoubleBinding.class;
				}
				if (datatype.equalsIgnoreCase("int")) {
					return LiteralIntBinding.class;
				}
				if (datatype.equalsIgnoreCase("integer")) {
					return LiteralIntBinding.class;
				}
			}

			// Complex Output
			if (output.isSetComplexOutput()) {
				return GenericFileDataBinding.class;
			}
		}
		return null;
	}

	/*
	 * This class performs a lookup to mediate between the data types used in the 52n WPS framework and the
	 * types defined in the Moving Code lib.
	 */
	private static final IODataType probeMCDataType(IData iData) {
		Class< ? > clazz = iData.getSupportedClass();

		if (clazz == IODataType.BOOLEAN.getSupportedClass()) {
			return IODataType.BOOLEAN;
		}

		if (clazz == IODataType.DOUBLE.getSupportedClass()) {
			return IODataType.DOUBLE;
		}

		if (clazz == IODataType.INTEGER.getSupportedClass()) {
			return IODataType.INTEGER;
		}

		if (clazz == IODataType.STRING.getSupportedClass()) {
			return IODataType.STRING;
		}

		if (clazz == GenericFileData.class) {
			return IODataType.MEDIA;
		}

		// in case we do not find a matching type return null
		return null;
	}

}
