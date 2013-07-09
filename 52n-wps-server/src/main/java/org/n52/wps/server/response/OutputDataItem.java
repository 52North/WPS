/*****************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: foerster
		 Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany
		 Matthias Mueler, TU Dresden


 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows.x11.BoundingBoxType;
import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputReferenceType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.IDatabase;
import org.opengis.geometry.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/*
 * @author foerster
 *
 */
public class OutputDataItem extends ResponseData {

	private static Logger LOGGER = LoggerFactory.getLogger(OutputDataItem.class);
	private static String COMPLEX_DATA_TYPE = "ComplexDataResponse";
	private LanguageStringType title;	

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
			String mimeType, LanguageStringType title, String algorithmIdentifier, ProcessDescriptionType description) throws ExceptionReport {
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
		output.setTitle(title);
		return output;	
	}

	public void updateResponseForBBOXData(ExecuteResponseDocument res, IData obj) {
		Envelope bbox = (Envelope) obj.getPayload();
		OutputDataType output = prepareOutput(res);
		BoundingBoxType bboxData = output.addNewData().addNewBoundingBoxData();
		if(bbox.getCoordinateReferenceSystem()!=null && bbox.getCoordinateReferenceSystem().getIdentifiers().size()>0){
			bboxData.setCrs(bbox.getCoordinateReferenceSystem().getIdentifiers().iterator().next().toString());
		}
		double[] lowerCorner = bbox.getLowerCorner().getCoordinate();
		List<Double> lowerCornerList = new ArrayList<Double>();
		for(double d : lowerCorner){
			lowerCornerList.add(d);
		}
		double[] upperCorner = bbox.getUpperCorner().getCoordinate();
		List<Double> upperCornerList = new ArrayList<Double>();
		for(double d : upperCorner){
			upperCornerList.add(d);
		}
		
		bboxData.setLowerCorner(lowerCornerList);
		bboxData.setUpperCorner(upperCornerList);
		
	}
}
