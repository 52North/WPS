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


package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class KMLParser extends AbstractParser {
	
	public KMLParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {
		
		FileOutputStream fos = null;
		try{
			File tempFile = File.createTempFile("kml", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GTVectorDataBinding data = parseXML(tempFile);
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	private GTVectorDataBinding parseXML(File file) {
		Configuration configuration = new KMLConfiguration();
		
		SimpleFeatureCollection fc = new GML3BasicParser().parseFeatureCollection(file, configuration, true);
		
		GTVectorDataBinding data = new GTVectorDataBinding(fc);
		
		return data;
	}

}
