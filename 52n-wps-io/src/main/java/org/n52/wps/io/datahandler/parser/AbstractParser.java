/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.AbstractIOHandler;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.webapp.api.FormatEntry;

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
		this.properties = WPSConfig.getInstance().getConfigurationEntriesForParserClass(this.getClass().getName());
		
		this.formats = WPSConfig.getInstance().getFormatEntriesForParserClass(this.getClass().getName());
				
		for (FormatEntry format : formats) {			

			if(format.getMimeType()!= null && !format.getMimeType().equals("")){
				String mimetype = format.getMimeType();
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
