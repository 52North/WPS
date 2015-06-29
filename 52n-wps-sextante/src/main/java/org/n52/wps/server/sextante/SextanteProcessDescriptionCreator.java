/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.sextante;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.RangeType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.webapp.api.FormatEntry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputChart;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputText;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class SextanteProcessDescriptionCreator implements SextanteConstants{
	
	@Inject
	private GeneratorFactory generatorFactory;    
	@Inject
	private ParserFactory parserFactory;

	public ProcessDescription createDescribeProcessType(GeoAlgorithm algorithm) throws NullParameterAdditionalInfoException, UnsupportedGeoAlgorithmException{

			ProcessDescriptionType pdt = ProcessDescriptionType.Factory.newInstance();
			pdt.setStatusSupported(true);
			pdt.setStoreSupported(true);
			
			pdt.addNewAbstract().setStringValue(algorithm.getName());
			pdt.addNewTitle().setStringValue(algorithm.getName());
			pdt.addNewIdentifier().setStringValue(algorithm.getCommandLineName());
			pdt.setProcessVersion("1.0.0");
			//inputs
			DataInputs inputs = pdt.addNewDataInputs();
			ParametersSet params = algorithm.getParameters();
			for (int i = 0; i < params.getNumberOfParameters(); i++) {
				Parameter param = params.getParameter(i);
				addParameter(inputs, param);
			}

			//grid extent for raster layers (if needed)
			if (algorithm.getUserCanDefineAnalysisExtent()){
				addGridExtent(inputs, algorithm.requiresRasterLayers());
			}


			//outputs
			ProcessOutputs outputs = pdt.addNewProcessOutputs();
			OutputObjectsSet ooset = algorithm.getOutputObjects();
			for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
				Output out = ooset.getOutput(i);
				addOutput(outputs, out);
			}

			ProcessDescription processDescription = new ProcessDescription();
			
			processDescription.addProcessDescriptionForVersion(pdt, "1.0.0");
			
			return processDescription;
		

	}

	private void addGridExtent(DataInputs inputs, boolean bOptional){

		addDoubleValue(inputs, GRID_EXTENT_X_MIN, "xMin", bOptional);
		addDoubleValue(inputs, GRID_EXTENT_X_MAX, "xMax", bOptional);
		addDoubleValue(inputs, GRID_EXTENT_Y_MIN, "yMin", bOptional);
		addDoubleValue(inputs, GRID_EXTENT_Y_MAX, "yMax", bOptional);
		addDoubleValue(inputs, GRID_EXTENT_CELLSIZE, "cellsize", bOptional);

	}

	private void addDoubleValue(DataInputs inputs, String name, String description, boolean bOptional){

		int iMinOccurs = 1;

		if (bOptional){
			iMinOccurs = 0;
		}

		InputDescriptionType input = inputs.addNewInput();
		input.addNewAbstract().setStringValue(description);
		input.addNewTitle().setStringValue(description);
		input.addNewIdentifier().setStringValue(name);

		LiteralInputType literal = input.addNewLiteralData();
		DomainMetadataType dataType = literal.addNewDataType();
		dataType.setReference("xs:double");
		literal.setDataType(dataType);
		input.setMinOccurs(BigInteger.valueOf(iMinOccurs));
		input.setMaxOccurs(BigInteger.valueOf(1));
		literal.setDefaultValue("0");

	}

	private void addOutput(ProcessOutputs outputs, Output out) {

		OutputDescriptionType output = outputs.addNewOutput();
		output.addNewAbstract().setStringValue(out.getDescription());
		output.addNewIdentifier().setStringValue(out.getName());
		output.addNewTitle().setStringValue(out.getDescription());
		if (out instanceof OutputRasterLayer){
			SupportedComplexDataType complexOutput = output.addNewComplexOutput();
			complexOutput.addNewDefault().addNewFormat().setMimeType("image/tiff");
			ComplexDataDescriptionType supportedFormat = complexOutput.addNewSupported().addNewFormat();
			supportedFormat.setMimeType("image/tiff");
			supportedFormat.setEncoding("base64");
			
			
		}
		else if (out instanceof OutputVectorLayer){
			SupportedComplexDataType complexOutput = output.addNewComplexOutput();
			addVectorOutputFormats(complexOutput);
			/*ComplexDataDescriptionType deafult = complexOutput.addNewDefault().addNewFormat();
			deafult.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			deafult.setSchema("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd");
			ComplexDataCombinationsType supported = complexOutput.addNewSupported();
			ComplexDataDescriptionType supportedFormat = supported.addNewFormat();
			supportedFormat.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			supportedFormat.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
			supportedFormat = supported.addNewFormat();
			supportedFormat.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
			supportedFormat.setEncoding(IOHandler.ENCODING_BASE64);*/
		}
		else if (out instanceof OutputTable){
			//TODO:
		}
		else if (out instanceof OutputText){
			output.addNewComplexOutput().addNewDefault().addNewFormat().setMimeType("text/html");
		}
		else if (out instanceof OutputChart){
			//TODO:
		}


	}

	private void addParameter(DataInputs inputs, Parameter param) throws NullParameterAdditionalInfoException,
																		UnsupportedGeoAlgorithmException {

		InputDescriptionType input = inputs.addNewInput();
		input.addNewAbstract().setStringValue(param.getParameterDescription());
		input.addNewTitle().setStringValue(param.getParameterDescription());
		input.addNewIdentifier().setStringValue(param.getParameterName());

		if (param instanceof ParameterRasterLayer){
			AdditionalInfoRasterLayer ai = (AdditionalInfoRasterLayer) param.getParameterAdditionalInfo();
			SupportedComplexDataInputType complex = input.addNewComplexData();
			ComplexDataCombinationsType supported = complex.addNewSupported();
			ComplexDataDescriptionType format = supported.addNewFormat();
			format.setMimeType("image/tiff");
			format = supported.addNewFormat();
			format.setMimeType("image/tiff");
			format.setEncoding(IOHandler.ENCODING_BASE64);
			ComplexDataDescriptionType defaultFormat = complex.addNewDefault().addNewFormat();
			defaultFormat.setMimeType("image/tiff");
			if (ai.getIsMandatory()){
				input.setMinOccurs(BigInteger.valueOf(1));
			}
			else{
				input.setMinOccurs(BigInteger.valueOf(0));
			}
			input.setMaxOccurs(BigInteger.valueOf(1));
		}else if (param instanceof ParameterVectorLayer){
			//TODO:add shape type
			AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
			SupportedComplexDataInputType complex = input.addNewComplexData();
			/*CComplexDataCombinationsType supported = complex.addNewSupported();
			omplexDataDescriptionType format = supported.addNewFormat();
			format.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			format.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
			format = supported.addNewFormat();
			format.setEncoding(IOHandler.ENCODING_BASE64);
			format.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
			ComplexDataDescriptionType defaultFormat = complex.addNewDefault().addNewFormat();
			defaultFormat.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			defaultFormat.setSchema("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd");
			*/
			if (ai.getIsMandatory()){
				input.setMinOccurs(BigInteger.valueOf(1));
			}
			else{
				input.setMinOccurs(BigInteger.valueOf(0));
			}
			input.setMaxOccurs(BigInteger.valueOf(1));
			
			addVectorInputsFormats(complex);

		}
		else if (param instanceof ParameterNumericalValue){
			AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) param.getParameterAdditionalInfo();
			LiteralInputType literal = input.addNewLiteralData();
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:double");
			literal.setDataType(dataType);
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMaximumValue().setStringValue("" + Double.POSITIVE_INFINITY);
			range.addNewMinimumValue().setStringValue("" + Double.NEGATIVE_INFINITY);
			literal.setDefaultValue(Double.toString(ai.getDefaultValue()));
		}
		else if (param instanceof ParameterString){
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			literal.addNewAnyValue();
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:string");
			literal.setDataType(dataType);
		}
		else if (param instanceof ParameterMultipleInput){
			AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
			SupportedComplexDataInputType complex = input.addNewComplexData();
			switch (ai.getDataType()){
			case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
				complex.addNewDefault().addNewFormat().setMimeType("image/tiff");
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
				input.setMaxOccurs(BigInteger.valueOf(1));
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
				addVectorInputsFormats(complex);
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
				input.setMaxOccurs(BigInteger.valueOf(1));
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
				addVectorInputsFormats(complex);
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
				input.setMaxOccurs(BigInteger.valueOf(1));
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
				addVectorInputsFormats(complex);
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
				input.setMaxOccurs(BigInteger.valueOf(1));
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
				addVectorInputsFormats(complex);
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
				input.setMaxOccurs(BigInteger.valueOf(1));
				break;
			default:
				throw new UnsupportedGeoAlgorithmException();
			}
		}
		else if (param instanceof ParameterSelection){
			AdditionalInfoSelection ai = (AdditionalInfoSelection) param.getParameterAdditionalInfo();
			String[] values = ai.getValues();
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			AllowedValues allowedValues = literal.addNewAllowedValues();
			for (int i = 0; i < values.length; i++) {
				allowedValues.addNewValue().setStringValue(values[i]);
			}
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:string");
		}
		else if (param instanceof ParameterTableField ){
			//This has to be improved, to add the information about the parent parameter
			//the value is the zero-based index of the field
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMinimumValue().setStringValue("0");
			range.addNewMaximumValue().setStringValue("" + Integer.MAX_VALUE);
			literal.setDefaultValue("0");
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:int");
		}
		else if (param instanceof ParameterBand){
			//This has to be improved, to add the information about the parent parameter
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMinimumValue().setStringValue("0");
			range.addNewMaximumValue().setStringValue("" + Integer.MAX_VALUE);
			literal.setDefaultValue("0");
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:int");
		}
		else if (param instanceof ParameterPoint){
			//points are entered as x and y coordinates separated by a comma (any idea
			//about how to better do this?)
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			literal.setDefaultValue("0, 0");
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:string");
		}
		else if (param instanceof ParameterBoolean){
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:boolean");
			literal.setDataType(dataType);
			literal.addNewAnyValue();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			literal.setDefaultValue("false");
		}
		else if (param instanceof ParameterFixedTable){
			//TODO:
			throw new UnsupportedGeoAlgorithmException();
		}
	}

	private void addVectorInputsFormats(SupportedComplexDataInputType complex) {

		List<IParser> parsers = parserFactory.getAllParsers();
		List<IParser> foundParsers = new ArrayList<IParser>();
		for(IParser parser : parsers) {
			Class<?>[] supportedClasses = parser.getSupportedDataBindings();
			for(Class<?> clazz : supportedClasses){
				if(clazz.equals(GTVectorDataBinding.class)){
					foundParsers.add(parser);
				}
				
			}
		}
		
		ComplexDataCombinationsType supportedInputFormat = complex.addNewSupported();
		
		for (int i = 0; i < foundParsers.size(); i++) {
			IParser parser = foundParsers.get(i);

			List<FormatEntry> supportedFullFormats = parser.getSupportedFullFormats();

			if (complex.getDefault() == null) {
				ComplexDataCombinationType defaultInputFormat = complex
						.addNewDefault();
				/*
				 * default format will be the first config format
				 */
				FormatEntry format = supportedFullFormats.get(0);
				ComplexDataDescriptionType defaultFormat = defaultInputFormat
						.addNewFormat();
				defaultFormat.setMimeType(format.getMimeType());

				String encoding = format.getEncoding();

				if (encoding != null && !encoding.equals("")) {
					defaultFormat.setEncoding(encoding);
				}

				String schema = format.getSchema();

				if (schema != null && !schema.equals("")) {
					defaultFormat.setSchema(schema);
				}

			}

			for (int j = 0; j < supportedFullFormats.size(); j++) {
				/*
				 * create supportedFormat for each mimetype, encoding, schema
				 * composition mimetypes can have several encodings and schemas
				 */
				FormatEntry format1 = supportedFullFormats.get(j);

				/*
				 * add one format for this mimetype
				 */
				ComplexDataDescriptionType supportedFormat = supportedInputFormat
						.addNewFormat();
				supportedFormat.setMimeType(format1.getMimeType());
				if (format1.getEncoding() != null) {
					supportedFormat.setEncoding(format1.getEncoding());
				}
				if (format1.getSchema() != null) {
					supportedFormat.setSchema(format1.getSchema());
				}
			}
		}
		
	}

	private void addVectorOutputFormats(SupportedComplexDataType complex){
		
					
		List<IGenerator> generators = generatorFactory.getAllGenerators();
		List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
		for(IGenerator generator : generators) {
			Class<?>[] supportedClasses = generator.getSupportedDataBindings();
			for(Class<?> clazz : supportedClasses){
				if(clazz.equals(GTVectorDataBinding.class)){
					foundGenerators.add(generator);
				}
			}
		}
		ComplexDataCombinationsType supporteOutputFormat = complex.addNewSupported();
		
		for (int i = 0; i < foundGenerators.size(); i++) {
			IGenerator generator = foundGenerators.get(i);

			List<FormatEntry> supportedFullFormats = generator.getSupportedFullFormats();

			if (complex.getDefault() == null) {
				ComplexDataCombinationType defaultInputFormat = complex
						.addNewDefault();
				/*
				 * default format will be the first config format
				 */
				FormatEntry format = supportedFullFormats.get(0);
				ComplexDataDescriptionType defaultFormat = defaultInputFormat
						.addNewFormat();
				defaultFormat.setMimeType(format.getMimeType());

				String encoding = format.getEncoding();

				if (encoding != null && !encoding.equals("")) {
					defaultFormat.setEncoding(encoding);
				}

				String schema = format.getSchema();

				if (schema != null && !schema.equals("")) {
					defaultFormat.setSchema(schema);
				}

			}

			for (int j = 0; j < supportedFullFormats.size(); j++) {
				/*
				 * create supportedFormat for each mimetype, encoding, schema
				 * composition mimetypes can have several encodings and schemas
				 */
				FormatEntry format1 = supportedFullFormats.get(j);

				/*
				 * add one format for this mimetype
				 */
				ComplexDataDescriptionType supportedFormat = supporteOutputFormat
						.addNewFormat();
				supportedFormat.setMimeType(format1.getMimeType());
				if (format1.getEncoding() != null) {
					supportedFormat.setEncoding(format1.getEncoding());
				}
				if (format1.getSchema() != null) {
					supportedFormat.setSchema(format1.getSchema());
				}
			}
		}
					
		
	}
	
	//This class is thrown when there is any problem creating the XML
	//WPS file from a geoalgorithm, due to some yet unsupported feature
	//or parameter
	public class UnsupportedGeoAlgorithmException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1017100163300095362L;

	}

}


