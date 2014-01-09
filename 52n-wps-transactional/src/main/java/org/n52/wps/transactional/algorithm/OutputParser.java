/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */



package org.n52.wps.transactional.algorithm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.io.BasicXMLTypeFactory;


public class OutputParser {
	
	
	/**
	 * Handles the ComplexValueReference
	 * @param class1 
	 * @param input The client input
	 * @throws ExceptionReport If the input (as url) is invalid, or there is an error while parsing the XML.
	 */
	protected  static String handleComplexValueReference(OutputDataType output) throws ExceptionReport{
		return output.getReference().getHref();
		
	}
	
	/**
	 * Handles the complexValue, which in this case should always include XML 
	 * which can be parsed into a FeatureCollection.
	 * @param class1 
	 * @param input The client input
	 * @throws ExceptionReport If error occured while parsing XML
	 */
	protected static IData handleComplexValue(OutputDataType output, ProcessDescriptionType processDescription) throws ExceptionReport{
		String outputID = output.getIdentifier().getStringValue();
		String complexValue = output.getData().getComplexData().toString();
		OutputDescriptionType outputDesc = null;
		for(OutputDescriptionType tempDesc : processDescription.getProcessOutputs().getOutputArray()) {
			if((tempDesc.getIdentifier().getStringValue().startsWith(outputID))) {
				outputDesc = tempDesc;
				break;
			}
		}

		if(outputDesc == null) {
			throw new RuntimeException("output cannot be found in description for " + processDescription.getIdentifier().getStringValue() + "," + outputID);
		}
		
		// get data specification from request
		String schema = output.getData().getComplexData().getSchema();
		String encoding = output.getData().getComplexData().getEncoding();
		String format = output.getData().getComplexData().getMimeType();
		
		// check for null elements in request and replace by defaults
		if(schema == null) {
			schema = outputDesc.getComplexOutput().getDefault().getFormat().getSchema();
		}
		if(format == null) {
			format = outputDesc.getComplexOutput().getDefault().getFormat().getMimeType();
		}
		if(encoding == null) {
			encoding = outputDesc.getComplexOutput().getDefault().getFormat().getEncoding();
		}
		
		Class outputDataType = determineOutputDataType(outputID, outputDesc);
		
		IParser parser = ParserFactory.getInstance().getParser(schema, format, encoding, outputDataType);
//		if(parser == null) {
//			parser = ParserFactory.getInstance().getSimpleParser();
//		}
		IData collection = null;
		// encoding is UTF-8 (or nothing and we default to UTF-8)
		// everything that goes to this condition should be inline xml data
		if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
			try {
				InputStream stream = new ByteArrayInputStream(complexValue.getBytes());
				collection = parser.parse(stream, format, schema);
			}
			catch(RuntimeException e) {
				throw new ExceptionReport("Error occured, while XML parsing", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		}
		else {
			throw new ExceptionReport("parser does not support operation: " + parser.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		return collection;
	}
	
	

	private static Class determineOutputDataType(String outputID, OutputDescriptionType output) {
			
		if(output.isSetLiteralOutput()){
			String datatype = output.getLiteralOutput().getDataType().getStringValue();
			if(datatype.contains("tring")){
				return LiteralStringBinding.class;
			}
			if(datatype.contains("ollean")){
				return LiteralBooleanBinding.class;
			}
			if(datatype.contains("loat") || datatype.contains("ouble")){
				return LiteralDoubleBinding.class;
			}
			if(datatype.contains("nt")){
				return LiteralIntBinding.class;
			}
		}
		if(output.isSetComplexOutput()){
			String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
			if(mimeType.contains("xml") || (mimeType.contains("XML"))){
				return GTVectorDataBinding.class;
			}else{
				return GTRasterDataBinding.class;
			}
		}
		
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	protected static IData handleLiteralValue(OutputDataType output) throws ExceptionReport {
		
		String parameter = output.getData().getLiteralData().getStringValue();
		String xmlDataType = output.getData().getLiteralData().getDataType();
		IData parameterObj = null;
		try {
			parameterObj = BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, parameter);
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("The passed parameterValue: " + parameter + ", but should be of type: " + xmlDataType, ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		if(parameterObj == null) {
			throw new ExceptionReport("XML datatype as LiteralParameter is not supported by the server: dataType " + xmlDataType, 
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		return parameterObj;
		
	}
	
	/**
	 * Handles BBoxValue
	 * @param input The client input
	 * @param class1 
	 */
	protected static IData handleBBoxValue(OutputDataType input) throws ExceptionReport{
		//String inputID = input.getIdentifier().getStringValue();
		throw new ExceptionReport("BBox is not supported", ExceptionReport.OPERATION_NOT_SUPPORTED);
	}

}
