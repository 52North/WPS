/*****************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: foerster
Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany


 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputReferenceType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.binary.AbstractBinaryGenerator;
import org.n52.wps.io.xml.AbstractXMLGenerator;
import org.n52.wps.io.xml.AbstractXMLStringGenerator;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.n52.wps.server.database.IDatabase;
import org.w3c.dom.Node;

/*
 * @author foerster
 *
 */
public class OutputDataItem extends ResponseData {

	private static Logger LOGGER = Logger.getLogger(OutputDataItem.class);
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
	 */
	public OutputDataItem(Object obj, String id, String schema, 
			String encoding, String mimeType, LanguageStringType title) {
		super(obj, id, schema, encoding, mimeType);
		this.title = title;
	}

	/**
	 * 
	 * @param obj
	 * @param id
	 * @param schema
	 * @param encoding
	 * @param mimeType
	 * @param title
	 * @param algorithmIdentifier
	 */
	public OutputDataItem(Object obj, String id, String schema, String encoding, 
			String mimeType, LanguageStringType title, String algorithmIdentifier) {
		super(obj, id, schema, encoding, mimeType, algorithmIdentifier);
		this.title = title;
	}
	
	/**
	 * 
	 * @param res
	 * @throws ExceptionReport
	 */
	public void updateResponseForComplexData(ExecuteResponseDocument res) throws ExceptionReport {
		OutputDataType output = prepareOutput(res);
		prepareGenerator();
		
		try {
			// CHECKING IF STORE IS TRUE AND THEN PROCESSING.... SOMEHOW!
			// CREATING A COMPLEXVALUE	
			if(generator instanceof AbstractXMLGenerator) {
				Node xmlNode;
				xmlNode = ((AbstractXMLGenerator)generator).generateXML(super.obj, null);
				if (xmlNode == null){
					LOGGER.error("Something bad happend while generating output");
				} else {
					try {
						output.addNewData().addNewComplexData().set(XmlObject.Factory.parse(xmlNode));
					} catch(XmlException e) {
						throw new ExceptionReport("Error occured while generating XML",ExceptionReport.NO_APPLICABLE_CODE, e);
					}
				}
			} else if(generator instanceof AbstractXMLStringGenerator) {
				try {
					output.addNewData().addNewComplexData().set(XmlObject.Factory.parse(
							((AbstractXMLStringGenerator)generator).generateXML(super.obj)));
				} catch(XmlException xml_ex) {
					throw new ExceptionReport("Error occured while generating XML",ExceptionReport.NO_APPLICABLE_CODE, xml_ex);
				}
			} else {
				throw new ExceptionReport("This generator does not support serialization: " + generator.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		} catch(RuntimeException e) {
			throw new ExceptionReport("Error while generating XML out of the process result", 
												ExceptionReport.NO_APPLICABLE_CODE, e);
		}
	}
	
	public void updateResponseForLiteralData(ExecuteResponseDocument res, String dataTypeReference){
		OutputDataType output = prepareOutput(res);
		String processValue = BasicXMLTypeFactory.getStringRepresentation(dataTypeReference, obj);
		CodeType idType = output.addNewIdentifier();
		idType.setStringValue(id);
		LiteralDataType literalData = output.addNewData().addNewLiteralData();
		literalData.setDataType(dataTypeReference);
		literalData.setStringValue(processValue);
	}
	
	public void updateResponseAsReference(ExecuteResponseDocument res, String reqID) throws ExceptionReport {
		prepareGenerator();
		OutputDataType output = prepareOutput(res);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputReferenceType outReference = output.addNewReference();
		outReference.setSchema(schema);
		IDatabase db = DatabaseFactory.getDatabase();
		String storeID = reqID + "#" + id;
		if(generator instanceof IStreamableGenerator) {
			OutputStreamWriter writer = new OutputStreamWriter(baos);
			((IStreamableGenerator)generator).write(obj, writer);
			try {
				writer.close();
			} catch(IOException io_ex) {
				throw new ExceptionReport("Closing the writer throws an IO exception", ExceptionReport.NO_APPLICABLE_CODE); 
			}
		} else {
			if(generator instanceof AbstractXMLGenerator) {
				Node xmlNode = ((AbstractXMLGenerator)generator).generateXML(obj, schema);
				try{
					XmlObject xmlObj = XmlObject.Factory.parse(xmlNode);
					xmlObj.save(baos);
				}
				catch(XmlException e) {
					throw new ExceptionReport("Something happend while converting XML node to dataBaseStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
				catch(IOException e) {
					throw new ExceptionReport("Something happend while converting XML node to dataBaseStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
			} if(generator instanceof AbstractBinaryGenerator) {
				// OutputStream stream = ((AbstractBinaryGenerator)generator).generate(obj);
			} else {
				throw new ExceptionReport("This generator does not support serialization: " + generator.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
		String storeReference = db.storeComplexValue(storeID, baos, COMPLEX_DATA_TYPE);
		outReference.setHref(URLEncoder.encode(storeReference));
	}
	
	private OutputDataType prepareOutput(ExecuteResponseDocument res){
		OutputDataType output = res.getExecuteResponse().getProcessOutputs().addNewOutput();
		CodeType identifierCode = output.addNewIdentifier();
		identifierCode.setStringValue(id);
		output.setTitle(title);
		return output;	
	}
}
