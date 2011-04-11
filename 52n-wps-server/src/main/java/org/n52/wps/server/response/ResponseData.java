/*****************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.binary.AbstractBinaryGenerator;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.w3c.dom.Node;

/*
 * @author foerster
 * This and the inheriting classes in charge of populating the ExecuteResponseDocument.
 */
public abstract class ResponseData {
	
	private static Logger LOGGER = Logger.getLogger(ResponseData.class); 
	
	protected IData obj = null;
	protected String id;
	protected String schema;
	protected String encoding;
	protected String mimeType;
	protected IGenerator generator = null;
	protected String algorithmIdentifier = null;
	
		
	public ResponseData(IData obj, String id, String schema, String encoding, 
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
	 * Object has to be available to the class. This has to be ensured in the inheriting classes.
	 * @param stream Stream to append the data to.
	 * @param generator generator which appends the data to the stream.
	 * @throws ExceptionReport
	 */
	
	protected void storeRaw(OutputStream stream, IGenerator generator) 
			throws ExceptionReport {
		if(generator instanceof IStreamableGenerator) {
			try {
				((IStreamableGenerator)generator).writeToStream(obj, stream);
			} catch (RuntimeException e) {
				throw new ExceptionReport("Error generating data", ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		}
		else {
			if(generator instanceof AbstractXMLGenerator) {
				try {
					Node xmlNode = ((AbstractXMLGenerator)generator).generateXML(obj, null);
					XmlObject xmlObj = XmlObject.Factory.parse(xmlNode);
					xmlObj.save(stream);
					
				}
				catch(XmlException e) {
					throw new ExceptionReport("Something happend while converting XML node to the rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
				catch(IOException e) {
					throw new ExceptionReport("Something happend while converting XML node to rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
				catch(RuntimeException e) {
					throw new ExceptionReport("Something happend while converting XML node to rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
			else if(generator instanceof AbstractBinaryGenerator) {
				File file = ((AbstractBinaryGenerator)generator).generateFile(obj, mimeType);
				try {
					FileInputStream fileInputStream = new FileInputStream(file);
					IOUtils.copy(fileInputStream, stream);
				} catch (IOException e) {
					throw new ExceptionReport("Something happend while converting binary coverage to rawDataStream", ExceptionReport.NO_APPLICABLE_CODE);
				}
				
			}
			else {
				throw new ExceptionReport("This generator does not support serialization: " + generator.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
	}

	protected void prepareGenerator() throws ExceptionReport {
		Class algorithmOutput = RepositoryManager.getInstance().getOutputDataTypeForAlgorithm(this.algorithmIdentifier, id);
		this.generator =  GeneratorFactory.getInstance().getGenerator(this.schema, 
				this.mimeType, this.encoding, algorithmOutput);
		
		if(this.generator != null){ 
			LOGGER.info("Using generator " + generator.getClass().getName() + " for Schema: " + schema);
		}
		if(this.generator == null) {
			generator = getDefaultGeneratorForProcess(this.algorithmIdentifier, algorithmOutput);
			if(generator !=null){
				LOGGER.info("Using default generator for Schema: " + schema);
			}
		}
		if(this.generator == null) {
			LOGGER.info("Using simpleGenerator for Schema: " + schema);
			generator = GeneratorFactory.getInstance().getSimpleXMLGenerator();
		}
	}
	
	public IGenerator getDefaultGeneratorForProcess(String algorithmIdentifier, Class algorithmOutput) {
		ProcessDescriptionType description = RepositoryManager.getInstance().getProcessDescription(algorithmIdentifier);
		OutputDescriptionType[] outputs = description.getProcessOutputs().getOutputArray();
		if(outputs[0].isSetComplexOutput()){
			ComplexDataDescriptionType format = outputs[0].getComplexOutput().getDefault().getFormat();
			String encoding = format.getEncoding();
			String mimeType = format.getMimeType();
			String schema = format.getSchema();
			return GeneratorFactory.getInstance().getGenerator(schema, mimeType, encoding, algorithmOutput);
		}else{
			//TODO
			return null;
		}
	}
	
}


