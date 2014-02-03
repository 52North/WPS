/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.AbstractIOHandler;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public abstract class AbstractParser extends AbstractIOHandler implements IParser{
	
	/**
	 * A list of files that shall be deleted by destructor.
	 * Convenience mechanism to delete temporary files that had
	 * to be written during the generation procedure.
	 */
	protected List<File> finalizeFiles;
	
	public AbstractParser(){
		super();
		
		// load Parser Properties
		this.properties = WPSConfig.getInstance().getPropertiesForParserClass(this.getClass().getName());
		
		this.formats = WPSConfig.getInstance().getFormatsForParserClass(this.getClass().getName());
		
		for (Format format : formats) {			

			if(format.getMimetype()!= null && !format.getMimetype().equals("")){
				String mimetype = format.getMimetype();
				supportedFormats.add(mimetype);
			}
			if(format.getSchema()!= null && !format.getSchema().equals("")){
				String schema = format.getSchema();
				supportedSchemas.add(schema);				
			}
			
			if(format.getEncoding()!= null && !format.getEncoding().equals("")){
				String encoding = format.getEncoding();
				supportedEncodings.add(encoding);
			}else{
				supportedEncodings.add(IOHandler.DEFAULT_ENCODING);
			}			
		}	
		
		
//		for(Property property : properties){
//			if(property.getName().equalsIgnoreCase("supportedFormat")){
//				String format = property.getStringValue();
//				supportedFormats.add(format);
//			}
//			if(property.getName().equalsIgnoreCase("supportedSchema")){
//				String schema = property.getStringValue();
//				supportedSchemas.add(schema);
//			}
//			if(property.getName().equalsIgnoreCase("supportedEncoding")){
//				String encoding = property.getStringValue();
//				supportedEncodings.add(encoding);
//			} 
//		}
		finalizeFiles = new ArrayList<File>();
	}

	@Override
	public IData parseBase64(InputStream input, String mimeType, String schema) {
		return parse(new Base64InputStream(input), mimeType, schema);
	}
	
	/**
	 * Destructor deletes generated temporary files.
	 */
	@Override
	protected void finalize() throws Throwable {
		
		for (File currentFile : finalizeFiles){
			currentFile.delete();
		}
		
		super.finalize();
	}

}
