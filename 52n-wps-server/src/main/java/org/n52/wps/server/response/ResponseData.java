/*****************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.binary.AbstractBinaryGenerator;
import org.n52.wps.io.xml.AbstractXMLGenerator;
import org.n52.wps.server.ExceptionReport;
import org.w3c.dom.Node;

/*
 * @author foerster
 * This and the inheriting classes in charge of populating the ExecuteResponseDocument.
 */
public abstract class ResponseData {
	
	private static Logger LOGGER = Logger.getLogger(ResponseData.class); 
	
	protected Object obj = null;
	protected String id;
	protected String schema;
	protected String encoding;
	protected String mimeType;
	protected IGenerator generator = null;
	protected String algorithmIdentifier = null;
	
	public ResponseData(Object obj, String id, String schema, String encoding, String mimeType) {
		this.obj = obj;
		this.id = id;
		this.schema = schema;
		this.encoding = encoding;
		this.mimeType = mimeType;		
	}
	
	public ResponseData(Object obj, String id, String schema, String encoding, 
			String mimeType, String algorithmIdentifier) {
		this.obj = obj;
		this.id = id;
		this.schema = schema;
		this.encoding = encoding;
		this.mimeType = mimeType;
		this. algorithmIdentifier = algorithmIdentifier;
	}
	
	/**
	 *  convenience method, used for Rawdata and if in Output the as Reference is true
	 * Object has to be available to the class. THis has to be ensured in the inheriting classes.
	 * @param stream Stream to append the data to.
	 * @param generator generator which appends the data to the stream.
	 * @throws ExceptionReport
	 */
	
	protected void storeRaw(OutputStream stream, IGenerator generator) 
			throws ExceptionReport {
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		if(generator instanceof IStreamableGenerator) {
			((IStreamableGenerator)generator).write(obj, writer);
		}
		else {
			if(generator instanceof AbstractXMLGenerator) {
				Node xmlNode = ((AbstractXMLGenerator)generator).generateXML(obj, null);
				try {
					XmlObject xmlObj = XmlObject.Factory.parse(xmlNode);
					xmlObj.save(stream);
				}
				catch(XmlException e) {
					throw new ExceptionReport("Something happend while converting XML node to the rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
				catch(IOException e) {
					throw new ExceptionReport("Something happend while converting XML node to rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
			else if(generator instanceof AbstractBinaryGenerator) {
//				OutputStream stream = ((AbstractBinaryGenerator)generator).generate(obj);
			}
			else {
				throw new ExceptionReport("This generator does not support serialization: " + generator.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
	}

	protected void prepareGenerator() throws ExceptionReport {
		if(algorithmIdentifier == null)
			this.generator =  GeneratorFactory.getInstance().getGenerator(this.schema, 
					this.mimeType, this.encoding);
		else 
			this.generator =  GeneratorFactory.getInstance().getGenerator(this.schema, 
					this.mimeType, this.encoding, this.algorithmIdentifier);
		if(this.generator == null) {
			LOGGER.info("Using simpleGenerator for Schema: " + schema);
			generator = GeneratorFactory.getInstance().getSimpleXMLGenerator();
		}
	}
	
}


