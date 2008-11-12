package org.n52.wps.server.sextante;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputChart;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputText;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class WPSGeoAlgorithm {

	


	public ProcessDescriptionType getDescribeProcessType(GeoAlgorithm algorithm){

		try{
			ProcessDescriptionType pdt = ProcessDescriptionType.Factory.newInstance();

			pdt.addNewAbstract().setStringValue(algorithm.getName());
			pdt.addNewTitle().setStringValue(algorithm.getName());
			pdt.addNewIdentifier().setStringValue(algorithm.getCommandLineName());

			//inputs
			DataInputs inputs = pdt.addNewDataInputs();
			ParametersSet params = algorithm.getParameters();
			for (int i = 0; i < params.getNumberOfParameters(); i++) {
				Parameter param = params.getParameter(i);
				addParameter(inputs, param);
			}

			//outputs
			ProcessOutputs outputs = pdt.addNewProcessOutputs();
			OutputObjectsSet ooset = algorithm.getOutputObjects();
			for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
				Output out = ooset.getOutput(i);
				addOutput(outputs, out);
			}

			return pdt;
		}
		catch(Exception e){
			//TODO:add better exception handling
			e.printStackTrace();
			return null;
		}

	}

	private void addOutput(ProcessOutputs outputs, Output out) {

		OutputDescriptionType output = outputs.addNewOutput();
		output.addNewAbstract().setStringValue(out.getDescription());
		output.addNewIdentifier().setStringValue(out.getName());
		output.addNewTitle().setStringValue(out.getDescription());
		if (out instanceof OutputRasterLayer){
			output.addNewComplexOutput().addNewDefault().addNewFormat().setMimeType("image/tiff");
		}
		else if (out instanceof OutputVectorLayer){
			ComplexDataDescriptionType format = output.addNewComplexOutput().addNewDefault().addNewFormat();
			format.setMimeType("text/XML");
			format.setSchema("http://schemas.opengis.net/gml/3.0.0/base/gml.xsd");
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
			ComplexDataDescriptionType format = complex.addNewSupported().addNewFormat();
			format.setMimeType("image/tiff");
			ComplexDataDescriptionType defaultFormat = complex.addNewDefault().addNewFormat();
			defaultFormat.setMimeType("image/tiff");
			if (ai.getIsMandatory()){
				input.setMinOccurs(BigInteger.valueOf(1));
			}
			else{
				input.setMinOccurs(BigInteger.valueOf(0));
			}
			input.setMaxOccurs(BigInteger.valueOf(1));
		}
		if (param instanceof ParameterVectorLayer){
			//TODO:add shape type
			AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
			SupportedComplexDataInputType complex = input.addNewComplexData();
			ComplexDataDescriptionType format = complex.addNewSupported().addNewFormat();
			format.setMimeType("text/XML");
			format.setSchema("http://schemas.opengis.net/gml/3.0.0/base/gml.xsd");
			ComplexDataDescriptionType defaultFormat = complex.addNewDefault().addNewFormat();
			defaultFormat.setMimeType("text/XML");
			defaultFormat.setSchema("http://schemas.opengis.net/gml/3.0.0/base/gml.xsd");
			if (ai.getIsMandatory()){
				input.setMinOccurs(BigInteger.valueOf(1));
			}
			else{
				input.setMinOccurs(BigInteger.valueOf(0));
			}
			input.setMaxOccurs(BigInteger.valueOf(1));
		}
		else if (param instanceof ParameterNumericalValue){
			AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) param.getParameterAdditionalInfo();
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMaximumValue().setStringValue(Double.toString(ai.getMaxValue()));
			range.addNewMinimumValue().setStringValue(Double.toString(ai.getMinValue()));
			literal.setDefaultValue(Double.toString(ai.getDefaultValue()));
		}
		else if (param instanceof ParameterString){
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			literal.addNewAnyValue();
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
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
				//TODO:add shape type
				complex.addNewDefault().addNewFormat().setMimeType("image/tiff");
				if (ai.getIsMandatory()){
					input.setMinOccurs(BigInteger.valueOf(1));
				}
				else{
					input.setMinOccurs(BigInteger.valueOf(0));
				}
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
		}
		else if (param instanceof ParameterTableField ){
			//This has to be improved, to add the information about the parent parameter
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMinimumValue().setStringValue("0");
			literal.setDefaultValue("0");
		}
		else if (param instanceof ParameterBand){
			//This has to be improved, to add the information about the parent parameter
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMinimumValue().setStringValue("0");
			literal.setDefaultValue("0");
		}
		else if (param instanceof ParameterPoint){
			//points are entered as x and y coordinates separated by a comma (any idea
			//about how to better do this?)
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			literal.setDefaultValue("0, 0");
		}
		else if (param instanceof ParameterFixedTable){
			//TODO:
			throw new UnsupportedGeoAlgorithmException();
		}
	}

	//This class is thrown when there is any problem creating the XML
	//WPS file from a geoalgorithm, due to some yet unsupported feature
	//or parameter
	public class UnsupportedGeoAlgorithmException extends Exception{

	}

	public static void main(String[] args){
		/*Sextante.initialize();
		GeoAlgorithm algorithm = Sextante.getAlgorithmFromCommandLineName("buffer");
		WPSGeoAlgorithm geoAlgorithm = new WPSGeoAlgorithm();
		ProcessDescriptionType processDescription = geoAlgorithm.getDescribeProcessType(algorithm);
		try {
			processDescription.save(new File("C:\\testDescription.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
	}
	
}


