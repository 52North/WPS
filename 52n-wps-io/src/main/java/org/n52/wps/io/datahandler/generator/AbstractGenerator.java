/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

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

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.AbstractIOHandler;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;

public abstract class AbstractGenerator extends AbstractIOHandler implements IGenerator {
	
	/**
	 * A list of files that shall be deleted by destructor.
	 * Convenience mechanism to delete temporary files that had
	 * to be written during the generation procedure.
	 */
	protected List<File> finalizeFiles;
	
	public AbstractGenerator(){
		super();
		
		this.properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("supportedFormat")){
				String format = property.getStringValue();
				supportedFormats.add(format);
			}
			if(property.getName().equalsIgnoreCase("supportedSchema")){
				String schema = property.getStringValue();
				supportedSchemas.add(schema);
			}
			if(property.getName().equalsIgnoreCase("supportedEncoding")){
				String encoding = property.getStringValue();
				supportedEncodings.add(encoding);
			} 
		}
		
		finalizeFiles = new ArrayList<File>();
	}
	
	public InputStream generateBase64Stream(IData data, String mimeType, String schema) throws IOException {
		return new Base64InputStream(generateStream(data, mimeType, schema), true);
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
