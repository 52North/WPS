package org.n52.wps.server.sextante;

import java.math.BigInteger;

import org.n52.wps.io.IOHandler;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
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

	public ProcessDescriptionType createDescribeProcessType(GeoAlgorithm algorithm) throws NullParameterAdditionalInfoException, UnsupportedGeoAlgorithmException{

			ProcessDescriptionType pdt = ProcessDescriptionType.Factory.newInstance();
			pdt.setStatusSupported(true);
			pdt.setStoreSupported(true);
			/*XmlCursor c = pdt.newCursor();
			c.toFirstChild();
			c.toLastAttribute();
			c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_response.xsd");
			*/

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
			if (algorithm.generatesUserDefinedRasterOutput()){
				addGridExtent(inputs, algorithm.requiresRasterLayers());
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
			complexOutput.addNewSupported().addNewFormat().setMimeType("image/tiff");
		}
		else if (out instanceof OutputVectorLayer){
			SupportedComplexDataType complexOutput = output.addNewComplexOutput();
			ComplexDataDescriptionType deafult = complexOutput.addNewDefault().addNewFormat();
			deafult.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			deafult.setSchema("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd");
			ComplexDataCombinationsType supported = complexOutput.addNewSupported();
			ComplexDataDescriptionType supportedFormat = supported.addNewFormat();
			supportedFormat.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			supportedFormat.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
			supportedFormat = supported.addNewFormat();
			supportedFormat.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
			supportedFormat.setEncoding(IOHandler.ENCODING_BASE64);
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
		}
		if (param instanceof ParameterVectorLayer){
			//TODO:add shape type
			AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
			SupportedComplexDataInputType complex = input.addNewComplexData();
			ComplexDataCombinationsType supported = complex.addNewSupported();
			ComplexDataDescriptionType format = supported.addNewFormat();
			format.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			format.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
			format = supported.addNewFormat();
			format.setEncoding(IOHandler.ENCODING_BASE64);
			format.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
			ComplexDataDescriptionType defaultFormat = complex.addNewDefault().addNewFormat();
			defaultFormat.setMimeType(IOHandler.DEFAULT_MIMETYPE);
			defaultFormat.setSchema("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd");
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
			DomainMetadataType dataType = literal.addNewDataType();
			dataType.setReference("xs:double");
			literal.setDataType(dataType);
			input.setMinOccurs(BigInteger.valueOf(1));
			input.setMaxOccurs(BigInteger.valueOf(1));
			RangeType range = literal.addNewAllowedValues().addNewRange();
			range.addNewMaximumValue().setStringValue("+Infinity");
			range.addNewMinimumValue().setStringValue("-Infinity");
			literal.setDefaultValue(Double.toString(ai.getDefaultValue()));
		}
		else if (param instanceof ParameterString){
			LiteralInputType literal = input.addNewLiteralData();
			input.setMinOccurs(BigInteger.valueOf(1));
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
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
				//TODO:add shape type
				ComplexDataDescriptionType format = complex.addNewDefault().addNewFormat();
				format.setMimeType(IOHandler.DEFAULT_MIMETYPE);
				format.setSchema("http://schemas.opengis.net/gml/2.1.2/feature.xsd");
				ComplexDataDescriptionType supportedFormat1 = complex.addNewSupported().addNewFormat();
				supportedFormat1.setEncoding(IOHandler.ENCODING_BASE64);
				supportedFormat1.setMimeType(IOHandler.MIME_TYPE_ZIPPED_SHP);
				ComplexDataDescriptionType supportedFormat2 = complex.addNewSupported().addNewFormat();
				supportedFormat2.setMimeType(IOHandler.DEFAULT_MIMETYPE);
				supportedFormat2.setSchema("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd");
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
			range.addNewMaximumValue().setStringValue("+Infinity");
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
			range.addNewMaximumValue().setStringValue("+Infinity");
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

	//This class is thrown when there is any problem creating the XML
	//WPS file from a geoalgorithm, due to some yet unsupported feature
	//or parameter
	public class UnsupportedGeoAlgorithmException extends Exception{

	}

	public static void main(String[] args){
		Sextante.initialize();
		GeoAlgorithm algorithm = Sextante.getAlgorithmFromCommandLineName("buffer");
		SextanteProcessDescriptionCreator geoAlgorithm = new SextanteProcessDescriptionCreator();
		ProcessDescriptionType processDescription = null;
		try {
			processDescription = geoAlgorithm.createDescribeProcessType(algorithm);
			System.out.println(processDescription);
		} catch (NullParameterAdditionalInfoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedGeoAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}

}


