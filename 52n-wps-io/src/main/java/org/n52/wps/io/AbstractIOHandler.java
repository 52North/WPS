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

package org.n52.wps.io;

import java.util.ArrayList;
import java.util.List;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.PropertyDocument.Property;


/**
 * Extending subclasses of AbstractGenerator shall provide functionality to
 * generate serviceable output data for the processes offered by the 52N WPS framework.
 * 
 * @author Matthias Mueller
 *
 */

public abstract class AbstractIOHandler implements IOHandler {
	protected List<String> supportedFormats;
	protected List<String> supportedSchemas;
	protected List<String> supportedEncodings;
	protected List<Class<?>> supportedIDataTypes;
	protected Property[] properties;
	protected Format[] formats;
	
	public AbstractIOHandler(){
		this.supportedFormats = new ArrayList<String>();
		this.supportedSchemas = new ArrayList<String>();
		this.supportedEncodings = new ArrayList<String>();
		this.supportedIDataTypes = new ArrayList<Class<?>>();
	}
	
	/**
	 * Returns true if the given format is supported, else false.
	 */ 
	public boolean isSupportedFormat(String format) {
		for(String f : getSupportedFormats()) {
			if (f.equalsIgnoreCase(format)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an array having the supported formats (mimeTypes).
	 */
	public String[] getSupportedFormats() {
		String[] resultArray = supportedFormats.toArray(new String[supportedFormats.size()]);
		return resultArray;
	}
	
	
	/**
	 * Returns an array having the supported schemas.
	 */
	public String[] getSupportedSchemas() {
		String[] resultArray = supportedSchemas.toArray(new String[supportedSchemas.size()]);
		return resultArray;
	}


	/**
	 * Returns true if the given schema is supported, else false.
	 * Binary data has no schema in WPS 1.0.0: If the request does not contain a schema and
	 * the Generator has no schemas configured it is assumed to be a "binary case".
	 * The method will return TRUE in this case.
	 * Might lead to unexpected behaviour in malformed requests.
	 */
	public boolean isSupportedSchema(String schema) {
		//no schema given. assuming no schema required. therefore accept all schemas
		if(supportedSchemas.size()==0 && (schema == null || schema.isEmpty())){ // test whether schema is empty, because in ArcToolbox process descriptions, there is empty elements for schemas
			return true;
		}
		for(String supportedSchema : supportedSchemas) {
			if(supportedSchema.equalsIgnoreCase(schema))
				return true;
		}
		return false;
	}
	
	public Class<?>[] getSupportedDataBindings() {
		return supportedIDataTypes.toArray(new Class<?>[supportedIDataTypes.size()]);
	}


	public boolean isSupportedDataBinding(Class<?> binding) {
		for (Class<?> currentBinding : supportedIDataTypes){
			if (binding.equals(currentBinding)){
				return true;
			}
		}
		return false;
	}
	
	public String[] getSupportedEncodings(){
		String[] resultArray = supportedEncodings.toArray(new String[supportedEncodings.size()]);
		return resultArray;
		//return IOHandler.SUPPORTED_ENCODINGS;
	}
	
	public Format[] getSupportedFullFormats(){
		return formats;
	}
	
	public boolean isSupportedEncoding(String encoding){
		for (String currentEncoding : this.getSupportedEncodings()){
			if (currentEncoding.equalsIgnoreCase(encoding)){
				return true;
			}
		}
		return false;
	}
	
	protected boolean isSupportedGenerate (Class<?> binding, String mimeType, String schema){
		
		if (!(this.isSupportedFormat(mimeType))){
			return false;
		}
		
		if (!(this.isSupportedSchema(schema))){
			return false;
		}
		
		if(!(this.isSupportedDataBinding(binding))){
			return false;
		}
		
		return true;
	}
	
}

