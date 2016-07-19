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
package org.n52.wps.server.response;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows.x11.BoundingBoxType;
import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.ows.x20.BoundingBoxDocument;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputReferenceType;
import net.opengis.wps.x20.DataDocument.Data;
import net.opengis.wps.x20.DataOutputType;
import net.opengis.wps.x20.LiteralDataDocument;
import net.opengis.wps.x20.LiteralValueDocument;
import net.opengis.wps.x20.LiteralValueDocument.LiteralValue;
import net.opengis.wps.x20.ResultDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.AbstractLiteralDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.primitives.Doubles;

/*
 * @author foerster
 *
 */
public class OutputDataItem extends ResponseData {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutputDataItem.class);
	private static final String COMPLEX_DATA_TYPE = "ComplexDataResponse";
	private XmlString title;

	/**
	 *
	 * @param obj
	 * @param id
	 * @param schema
	 * @param encoding
	 * @param mimeType
	 * @param title
	 * @param algorithmIdentifier
	 * @throws ExceptionReport
	 */
	public OutputDataItem(IData obj, String id, String schema, String encoding,
			String mimeType, XmlString title, String algorithmIdentifier, ProcessDescription description) throws ExceptionReport {
		super(obj, id, schema, encoding, mimeType, algorithmIdentifier, description);

		this.title = title;
	}

	/**
	 *
	 * @param res
	 * @throws ExceptionReport
	 */
	public void updateResponseForInlineComplexData(ExecuteResponseDocument res) throws ExceptionReport {
		OutputDataType output = prepareOutput(res);
		prepareGenerator();
		ComplexDataType complexData = null;



		try {
			// CHECKING IF STORE IS TRUE AND THEN PROCESSING.... SOMEHOW!
			// CREATING A COMPLEXVALUE

			// in case encoding is NULL -or- empty -or- UTF-8
			// send plain text (XML or not) in response node
			//
			// in case encoding is base64
			// send base64encoded (binary) data in node
			//
			// in case encoding is
			//
			InputStream stream = null;
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
				stream = generator.generateStream(super.obj, mimeType, schema);
			}

			// in case encoding is base64 create a new text node
			// and parse the generator's result into it
			else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
				stream = generator.generateBase64Stream(super.obj, mimeType, schema);
			}
			else {
				throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
			complexData = output.addNewData().addNewComplexData();
			if(mimeType.contains("xml") || mimeType.contains("XML")){
				complexData.set(XmlObject.Factory.parse(stream));
				stream.close();
			}else{
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = builder.newDocument();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(stream, baos);
				stream.close();
				String text = baos.toString();
				baos.close();
				Node dataNode = document.createTextNode(text);
				complexData.set(XmlObject.Factory.parse(dataNode));
			}

		} catch(RuntimeException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (XmlException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result. Check encoding (base64 for inline binary data or UTF-8 for XML based data)", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Base64 Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		if (complexData != null) {
			if (schema != null) {
				// setting the schema attribute for the output.
				complexData.setSchema(schema);
			}
			if (encoding != null) {
				complexData.setEncoding(encoding);
			}
			if (mimeType != null) {
				complexData.setMimeType(mimeType);
			}
		}
	}

	public void updateResponseForLiteralData(ExecuteResponseDocument res, String dataTypeReference){
		OutputDataType output = prepareOutput(res);
		String processValue = BasicXMLTypeFactory.getStringRepresentation(dataTypeReference, obj);
		LiteralDataType literalData = output.addNewData().addNewLiteralData();
		if (dataTypeReference != null) {
			literalData.setDataType(dataTypeReference);
		}
	    literalData.setStringValue(processValue);
		if(obj instanceof AbstractLiteralDataBinding){
			String uom = ((AbstractLiteralDataBinding)obj).getUnitOfMeasurement();
			if(uom != null && !uom.equals("")){
				literalData.setUom(uom);
			}
		}
	}

	public void updateResponseAsReference(ExecuteResponseDocument res, String reqID, String mimeType) throws ExceptionReport {
		prepareGenerator();
		OutputDataType output = prepareOutput(res);
		InputStream stream;

		OutputReferenceType outReference = output.addNewReference();
		if (schema != null) {
			outReference.setSchema(schema);
		}
		if (encoding != null) {
			outReference.setEncoding(encoding);
		}
		if (mimeType != null) {
			outReference.setMimeType(mimeType);
		}
		IDatabase db = DatabaseFactory.getDatabase();
		String storeID = reqID + "" + id;

		try {
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
				stream = generator.generateStream(super.obj, mimeType, schema);
			}

			// in case encoding is base64
			else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
				stream = generator.generateBase64Stream(super.obj, mimeType, schema);
			}

			else {
				throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
		}
		catch (IOException e){
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Error while generating Complex Data out of the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		String storeReference = db.storeComplexValue(storeID, stream, COMPLEX_DATA_TYPE, mimeType);
		storeReference = storeReference.replace("#", "%23");
		outReference.setHref(storeReference);
		// MSS:  05-02-2009 changed default output type to text/xml to be certain that the calling application doesn't
		// serve the wrong type as it is a reference in this case.
		this.mimeType = "text/xml";
	}

	private OutputDataType prepareOutput(ExecuteResponseDocument res){
		OutputDataType output = res.getExecuteResponse().getProcessOutputs().addNewOutput();
		CodeType identifierCode = output.addNewIdentifier();
		identifierCode.setStringValue(id);
		output.setTitle((LanguageStringType) title);
		return output;
	}

	public void updateResponseForBBOXData(ExecuteResponseDocument res, IBBOXData bbox) {
		OutputDataType output = prepareOutput(res);
		BoundingBoxType bboxData = output.addNewData().addNewBoundingBoxData();
        if (bbox.getCRS() != null) {
            bboxData.setCrs(bbox.getCRS());
        }
		bboxData.setLowerCorner(Doubles.asList(bbox.getLowerCorner()));
		bboxData.setUpperCorner(Doubles.asList(bbox.getUpperCorner()));
		bboxData.setDimensions(BigInteger.valueOf(bbox.getDimension()));
	}

	private DataOutputType prepareOutput(ResultDocument res){
		DataOutputType output = res.getResult().addNewOutput();
		output.setId(id);
		return output;
	}

	public void updateResponseForLiteralData(ResultDocument res,
			String dataTypeReference) {
            DataOutputType output = prepareOutput(res);
            String processValue = BasicXMLTypeFactory.getStringRepresentation(dataTypeReference, obj);
            Data literalData = output.addNewData();
//            if (dataTypeReference != null) {
//                    literalData.setDataType(dataTypeReference);
//            }
            LiteralValueDocument literalValueDocument = LiteralValueDocument.Factory.newInstance();
            
            LiteralValue literalValue = literalValueDocument.addNewLiteralValue();
            
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.newDocument();
                Node dataNode = document.createTextNode(processValue);
                literalValue.set(XmlObject.Factory.parse(dataNode));
                literalData.set(literalValueDocument);
                
            } catch (Exception e) {
                LOGGER.error("Excepion while trying to write inline literal output.");
                LOGGER.error(e.getMessage());
            }
//            literalData.setStringValue(processValue);
//            if(obj instanceof AbstractLiteralDataBinding){
//                    String uom = ((AbstractLiteralDataBinding)obj).getUnitOfMeasurement();
//                    if(uom != null && !uom.equals("")){
//                            literalData.setUom(uom);
//                    }
//            }
		
	}

	public void updateResponseAsReference(ResultDocument res, String reqID,
			String mimeType) throws ExceptionReport {
		prepareGenerator();
		DataOutputType output = prepareOutput(res);
		InputStream stream;

		net.opengis.wps.x20.ReferenceType outReference = output.addNewReference();
		if (schema != null) {
			outReference.setSchema(schema);
		}
		if (encoding != null) {
			outReference.setEncoding(encoding);
		}
		if (mimeType != null) {
			outReference.setMimeType(mimeType);
		}
		IDatabase db = DatabaseFactory.getDatabase();
		String storeID = reqID + "" + id;

		try {
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
				stream = generator.generateStream(super.obj, mimeType, schema);
			}

			// in case encoding is base64
			else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
				stream = generator.generateBase64Stream(super.obj, mimeType, schema);
			}

			else {
				throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
		}
		catch (IOException e){
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Error while generating Complex Data out of the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		// TODO enhance this to support additional storage possibilities, e.g. WFS
		String storeReference = db.storeComplexValue(storeID, stream, COMPLEX_DATA_TYPE, mimeType);
		storeReference = storeReference.replace("#", "%23");
		outReference.setHref(storeReference);
		// MSS:  05-02-2009 changed default output type to text/xml to be certain that the calling application doesn't
		// serve the wrong type as it is a reference in this case.
		this.mimeType = "text/xml";
		
	}

	public void updateResponseForInlineComplexData(ResultDocument res) throws ExceptionReport {
		DataOutputType output = prepareOutput(res);
		prepareGenerator();
		Data complexData = null;

		try {
			// in case encoding is NULL -or- empty -or- UTF-8
			// send plain text (XML or not) in response node
			//
			// in case encoding is base64
			// send base64encoded (binary) data in node
			//
			// in case encoding is
			//
			InputStream stream = null;
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
				stream = generator.generateStream(super.obj, mimeType, schema);
			}

			// in case encoding is base64 create a new text node
			// and parse the generator's result into it
			else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
				stream = generator.generateBase64Stream(super.obj, mimeType, schema);
			}
			else {
				throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
			complexData = output.addNewData();
			if(mimeType.contains("xml") || mimeType.contains("XML")){
				complexData.set(XmlObject.Factory.parse(stream));
				stream.close();
			}else{
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = builder.newDocument();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(stream, baos);
				stream.close();
				String text = baos.toString();
				baos.close();
				Node dataNode = document.createTextNode(text);
				complexData.set(XmlObject.Factory.parse(dataNode));
			}

		} catch(RuntimeException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (XmlException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Complex Data from the process result. Check encoding (base64 for inline binary data or UTF-8 for XML based data)", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ExceptionReport("Could not create Inline Base64 Complex Data from the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		}

		if (complexData != null) {
			if (schema != null) {
				// setting the schema attribute for the output.
				complexData.setSchema(schema);
			}
			if (encoding != null) {
				complexData.setEncoding(encoding);
			}
			if (mimeType != null) {
				complexData.setMimeType(mimeType);
			}
		}
		
	}

    public void updateResponseForBBOXData(ResultDocument res,
            IBBOXData bbox) {
        DataOutputType output = prepareOutput(res);
        
        BoundingBoxDocument bbBoxDocument = BoundingBoxDocument.Factory.newInstance();
        
        net.opengis.ows.x20.BoundingBoxType bbBoxType = bbBoxDocument.addNewBoundingBox();
        
        if (bbox.getCRS() != null) {
            bbBoxType.setCrs(bbox.getCRS());
        }
        bbBoxType.setLowerCorner(Doubles.asList(bbox.getLowerCorner()));
        bbBoxType.setUpperCorner(Doubles.asList(bbox.getUpperCorner()));
        bbBoxType.setDimensions(BigInteger.valueOf(bbox.getDimension()));
        
        Data data = output.addNewData();
        
        data.set(bbBoxDocument);
    }
}
