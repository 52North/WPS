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

import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;

/*
 * @author foerster
 * This and the inheriting classes in charge of populating the ExecuteResponseDocument.
 */
public abstract class ResponseData {
	
	private static Logger LOGGER = LoggerFactory.getLogger(ResponseData.class); 
	
	protected IData obj = null;
	protected String id;
	protected String schema;
	protected String encoding;
	protected String mimeType;
	protected IGenerator generator = null;
	protected String algorithmIdentifier = null;
	protected ProcessDescriptionType description = null;
	
		
	public ResponseData(IData obj, String id, String schema, String encoding, 
			String mimeType, String algorithmIdentifier, ProcessDescriptionType description) throws ExceptionReport {
		
		this.obj = obj;
		this.id = id;
		this.algorithmIdentifier = algorithmIdentifier;
		this.description = description;
		this.encoding = encoding;
	
		OutputDescriptionType outputType =null;
		
		OutputDescriptionType[] describeProcessOutput = description.getProcessOutputs().getOutputArray();
		for(OutputDescriptionType tempOutputType : describeProcessOutput){
			if(tempOutputType.getIdentifier().getStringValue().equalsIgnoreCase(id)){
				outputType = tempOutputType;
			}
		}
		
		
		
		//select generator
		
		//0. complex output set? --> no: skip
		//1. mimeType set?
		//yes--> set it
			//1.1 schema/encoding set?
			//yes-->set it
			//not-->set default values for parser with matching mime type
		
		//no--> schema or/and encoding are set?
					//yes-->use it, look if only one mime type can be found
					//not-->use default values
		
		
		String finalSchema = null;
		String finalMimeType = null;
		String finalEncoding = null;
		
		if (outputType.isSetComplexOutput()){
			if (mimeType != null){
				//mime type in request
				ComplexDataDescriptionType format = null;
				
				String defaultMimeType = outputType.getComplexOutput().getDefault().getFormat().getMimeType();
				
				boolean canUseDefault = false;
				if(defaultMimeType.equalsIgnoreCase(mimeType)){
					ComplexDataDescriptionType potenitalFormat = outputType.getComplexOutput().getDefault().getFormat();
					if(schema != null && encoding == null){
						if(schema.equalsIgnoreCase(potenitalFormat.getSchema())){
							canUseDefault = true;
							format = potenitalFormat;
						}
					}
					if(schema == null && encoding != null){
						if(encoding.equalsIgnoreCase(potenitalFormat.getEncoding())){
							canUseDefault = true;
							format = potenitalFormat;
						}
						
					}
					if(schema != null && encoding != null){
						if(schema.equalsIgnoreCase(potenitalFormat.getSchema()) && encoding.equalsIgnoreCase(potenitalFormat.getEncoding())){
							canUseDefault = true;
							format = potenitalFormat;
						}
						
					}
					if(schema == null && encoding == null){
						canUseDefault = true;
						format = potenitalFormat;
					}
					
				}
				if(!canUseDefault){
					 ComplexDataDescriptionType[] formats =outputType.getComplexOutput().getSupported().getFormatArray();
					 for(ComplexDataDescriptionType potenitalFormat : formats){
						 if(potenitalFormat.getMimeType().equalsIgnoreCase(mimeType)){
							 if(schema != null && encoding == null){
									if(schema.equalsIgnoreCase(potenitalFormat.getSchema())){
										format = potenitalFormat;
									}
								}
								if(schema == null && encoding != null){
									if(encoding.equalsIgnoreCase(potenitalFormat.getEncoding()) || potenitalFormat.getEncoding() == null){
										format = potenitalFormat;
									}
									
								}
								if(schema != null && encoding != null){
									if(schema.equalsIgnoreCase(potenitalFormat.getSchema()) && ((encoding.equalsIgnoreCase(potenitalFormat.getEncoding()) || potenitalFormat.getEncoding() == null) )){
										format = potenitalFormat;
									}
									
								}
								if(schema == null && encoding == null){
									format = potenitalFormat;
								}
						 }
					 }
				}
				if(format == null){
					throw new ExceptionReport("Could not determine output format", ExceptionReport.INVALID_PARAMETER_VALUE);
				}
				
				finalMimeType = format.getMimeType();
				
				if(format.isSetEncoding()){
					//no encoding provided--> select default one for mimeType
					finalEncoding = format.getEncoding();
				}
				
				if(format.isSetSchema()){
					//no encoding provided--> select default one for mimeType
					finalSchema = format.getSchema();
				}
				
			}else{
				
				//mimeType not in request
				if(mimeType==null && encoding==null && schema == null){
						//nothing set, use default values
						finalSchema = outputType.getComplexOutput().getDefault().getFormat().getSchema();
						finalMimeType = outputType.getComplexOutput().getDefault().getFormat().getMimeType();
						finalEncoding = outputType.getComplexOutput().getDefault().getFormat().getEncoding();
					
				}else{
						//do a smart search an look if a mimeType can be found for either schema and/or encoding
						
					if(mimeType==null){	
						if(encoding!=null && schema==null){
								//encoding set only
								ComplexDataDescriptionType encodingFormat = null;
								String defaultEncoding = outputType.getComplexOutput().getDefault().getFormat().getEncoding();
								int found = 0;
								String foundEncoding = null;
								if(defaultEncoding.equalsIgnoreCase(encoding)){
									foundEncoding = outputType.getComplexOutput().getDefault().getFormat().getEncoding();
									encodingFormat = outputType.getComplexOutput().getDefault().getFormat();
									found = found +1;
								}else{
									 ComplexDataDescriptionType[] formats = outputType.getComplexOutput().getSupported().getFormatArray();
									 for(ComplexDataDescriptionType tempFormat : formats){
										 if(tempFormat.getEncoding().equalsIgnoreCase(encoding)){
											 foundEncoding = tempFormat.getEncoding();
											 encodingFormat = tempFormat;
											 found = found +1;
										 }
									 }
								}
								
								if(found == 1){
									finalEncoding = foundEncoding;
									finalMimeType = encodingFormat.getMimeType();
									if(encodingFormat.isSetSchema()){
										finalSchema = encodingFormat.getSchema();
									}
								}else{
									throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
								}
								
							}
							if(schema != null && encoding==null){
								//schema set only
								ComplexDataDescriptionType schemaFormat = null;
								String defaultSchema = outputType.getComplexOutput().getDefault().getFormat().getSchema();
								int found = 0;
								String foundSchema = null;
								if(defaultSchema.equalsIgnoreCase(schema)){
									foundSchema = outputType.getComplexOutput().getDefault().getFormat().getSchema();
									schemaFormat = outputType.getComplexOutput().getDefault().getFormat();
									found = found +1;
								}else{
									 ComplexDataDescriptionType[] formats = outputType.getComplexOutput().getSupported().getFormatArray();
									 for(ComplexDataDescriptionType tempFormat : formats){
										 if(tempFormat.getEncoding().equalsIgnoreCase(schema)){
											 foundSchema = tempFormat.getSchema();
											 schemaFormat =tempFormat;
											 found = found +1;
										 }
									 }
								}
								
								if(found == 1){
									finalSchema = foundSchema;
									finalMimeType = schemaFormat.getMimeType();
									if(schemaFormat.isSetEncoding()){
										finalEncoding = schemaFormat.getEncoding();
									}
								}else{
									throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
								}
								
							}
							if(encoding!=null && schema!=null){
								//schema and encoding set
								
								
								//encoding
								String defaultEncoding = outputType.getComplexOutput().getDefault().getFormat().getEncoding();
								
								List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
								if(defaultEncoding.equalsIgnoreCase(encoding)){
									foundEncodingList.add(outputType.getComplexOutput().getDefault().getFormat());
									
									
								}else{
									 ComplexDataDescriptionType[] formats = outputType.getComplexOutput().getSupported().getFormatArray();
									 for(ComplexDataDescriptionType tempFormat : formats){
										 if(tempFormat.getEncoding().equalsIgnoreCase(encoding)){
											 foundEncodingList.add(tempFormat);
										 }
								}
								
								
								
								
								//schema
								List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
								String defaultSchema = outputType.getComplexOutput().getDefault().getFormat().getSchema();
								if(defaultSchema.equalsIgnoreCase(schema)){
									foundSchemaList.add(outputType.getComplexOutput().getDefault().getFormat());
								}else{
									 formats = outputType.getComplexOutput().getSupported().getFormatArray();
									 for(ComplexDataDescriptionType tempFormat : formats){
										 if(tempFormat.getEncoding().equalsIgnoreCase(schema)){
											 foundSchemaList.add(tempFormat);
										 }
									 }
								}
								
								
								//results
								ComplexDataDescriptionType foundCommonFormat = null;
								for(ComplexDataDescriptionType encodingFormat : foundEncodingList){
									for(ComplexDataDescriptionType schemaFormat : foundSchemaList){
										if(encodingFormat.equals(schemaFormat)){
											foundCommonFormat = encodingFormat;
										}
									}
										
									
								}
								
								if(foundCommonFormat!=null){
									mimeType = foundCommonFormat.getMimeType();
									if(foundCommonFormat.isSetEncoding()){
										finalEncoding = foundCommonFormat.getEncoding();
									}
									if(foundCommonFormat.isSetSchema()){
										finalSchema = foundCommonFormat.getSchema();
									}
								}else{
									throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
								}
								
							}
								
						}
							
					}
				}
	
			}
		}
		
		this.schema = finalSchema;
		if(this.encoding==null){
			this.encoding = finalEncoding;
		}
		this.mimeType = finalMimeType;
		
		
		
		
	}

	protected void prepareGenerator() throws ExceptionReport {
		Class<?> algorithmOutput = RepositoryManager.getInstance().getOutputDataTypeForAlgorithm(this.algorithmIdentifier, id);
		
		LOGGER.debug("Looking for matching Generator ..." + 
				" schema: " + schema +
				" mimeType: " + mimeType +
				" encoding: " + encoding);
		
		this.generator =  GeneratorFactory.getInstance().getGenerator(this.schema, this.mimeType, this.encoding, algorithmOutput);
		
		if(this.generator != null){ 
			LOGGER.info("Using generator " + generator.getClass().getName() + " for Schema: " + schema);
		}
		if(this.generator == null) {
			throw new ExceptionReport("Could not find an appropriate generator based on given mimetype/schema/encoding for output", ExceptionReport.NO_APPLICABLE_CODE);
		}
	}
	
}


