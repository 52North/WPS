/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 1.0.0 (OGC 05-007r4). 

 Copyright (C) 2008 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io.datahandler.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;

import com.vividsolutions.jts.geom.GeometryFactory;

public abstract class AbstractXMLParser implements IParser {
	protected GeometryFactory geomFactory;
	protected Property[] properties;
	private List<String> supportedSchemas;
	private List<String> supportedFormats;
	private List<String> supportedEncodings;
	
	
	public AbstractXMLParser() {
		geomFactory = new GeometryFactory();
		supportedSchemas = new ArrayList<String>();
		supportedFormats = new ArrayList<String>();
		supportedEncodings = new ArrayList<String>();
		properties = WPSConfig.getInstance().getPropertiesForParserClass(this.getClass().getName());
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("supportedSchema")){
				String supportedSchema = property.getStringValue();
				supportedSchemas.add(supportedSchema);
			}
			if(property.getName().equalsIgnoreCase("supportedFormat")){
				String supportedFormat = property.getStringValue();
				supportedFormats.add(supportedFormat);
			}
			if(property.getName().equalsIgnoreCase("supportedEncoding")){
				String supportedEncoding = property.getStringValue();
				supportedEncodings.add(supportedEncoding);
			}
		}
	}
	
	public AbstractXMLParser(boolean pReadWPSConfig) {
		geomFactory = new GeometryFactory();
		supportedSchemas = new ArrayList<String>();
		supportedFormats = new ArrayList<String>();
		supportedEncodings = new ArrayList<String>();
		
		
		if (pReadWPSConfig)
		{
			properties = WPSConfig.getInstance().getPropertiesForParserClass(this.getClass().getName());
			for(Property property : properties){
				if(property.getName().equalsIgnoreCase("supportedSchema")){
					String supportedSchema = property.getStringValue();
					supportedSchemas.add(supportedSchema);
				}
				if(property.getName().equalsIgnoreCase("supportedFormat")){
					String supportedFormat = property.getStringValue();
					supportedFormats.add(supportedFormat);
				}
				if(property.getName().equalsIgnoreCase("supportedEncoding")){
					String supportedEncoding = property.getStringValue();
					supportedEncodings.add(supportedEncoding);
				}
			} 
		}
		else
		{
			properties = new Property[0];
		}
	}

	public boolean isSupportedFormat(String format) {
		for(String f : getSupportedFormats()) {
			if (f.equalsIgnoreCase(format)) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public String[] getSupportedFormats() {
		String[] resultList = new String[supportedFormats.size()];
		for(int i = 0; i<supportedFormats.size();i++){
			resultList[i] = supportedFormats.get(i);
		}
		return resultList;
		
	}
	
	/**
	 * Returns an array having the supported schemas.
	 */
	public String[] getSupportedSchemas() {
		String[] resultList = new String[supportedSchemas.size()];
		for(int i = 0; i<supportedSchemas.size();i++){
			resultList[i] = supportedSchemas.get(i);
		}
		return resultList;
	
	}


	/**
	 * Returns true if the given schema is supported, else false.
	 */
	public boolean isSupportedSchema(String schema) {
		for(String supportedSchema : supportedSchemas) {
			if(supportedSchema.equalsIgnoreCase(schema))
				return true;
		}
		return false;
	}
	
	public boolean isSupportedEncoding(String encoding) {
		for(String supportedEncoding : supportedEncodings) {
			if(supportedEncoding.equalsIgnoreCase(encoding))
				return true;
		}
		return false;
	}
	
	public String[] getSupportedEncodings() {
		String[] resultList = new String[supportedEncodings.size()];
		for(int i = 0; i<supportedEncodings.size();i++){
			resultList[i] = supportedEncodings.get(i);
		}
		return resultList;
	}
	
	
	public abstract IData parseXML(String gml);
	public abstract IData parseXML(InputStream stream);
		

}
