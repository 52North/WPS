/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

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

package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;

public abstract class AbstractBinaryGenerator implements IGenerator {
	private List<String> supportedFormats;
	private List<String> supportedEncodings;
	protected Property[] properties;
	
	public AbstractBinaryGenerator(){
		supportedFormats = new ArrayList<String>();
		supportedEncodings = new ArrayList<String>();
		properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		for(Property property : properties){
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
	
	
	public boolean isSupportedSchema(String schema) {
		return true;
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
	
	public String[] getSupportedSchemas() {
		return null;
	}

	public abstract File generateFile(IData coll, String mimeType);
}
