/**
 * ﻿Copyright (C) 2007 - 2019 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io.data;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.IOHandler;


public final class GenericFileDataConstants {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GenericFileDataConstants.class);
	
	public static final String MIME_TYPE_ZIPPED_SHP = IOHandler.MIME_TYPE_ZIPPED_SHP;
	public static final String MIME_TYPE_SHP = "application/shp";
	public static final String MIME_TYPE_HDF = "application/img";
	public static final String MIME_TYPE_GEOTIFF = "application/geotiff";
	public static final String MIME_TYPE_TIFF = "image/tiff";
	public static final String MIME_TYPE_DBASE = "application/dbase";
	public static final String MIME_TYPE_REMAPFILE = "application/remap";
	public static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
	public static final String MIME_TYPE_TEXT_XML = "text/xml";
	public static final String MIME_TYPE_IMAGE_GEOTIFF = "image/geotiff";//TODO: this could not work due to geotiffparser...
	public static final String MIME_TYPE_X_GEOTIFF = "application/x-geotiff";
	public static final String MIME_TYPE_IMAGE_PNG= "image/png";
	public static final String MIME_TYPE_IMAGE_GIF = "image/gif";
	public static final String MIME_TYPE_IMAGE_JPEG = "image/jpeg";
	public static final String MIME_TYPE_X_ERDAS_HFA = "application/x-erdas-hfa";
	public static final String MIME_TYPE_NETCDF = "application/netcdf";
	public static final String MIME_TYPE_X_NETCDF = "application/x-netcdf";
	public static final String MIME_TYPE_DGN = "application/dgn";	
	public static final String MIME_TYPE_KML = "application/vnd.google-earth.kml+xml";	
	public static final String MIME_TYPE_HDF4EOS = "application/hdf4-eos";
	public static final String MIME_TYPE_GML200 = "text/xml; subtype=gml/2.0.0";
	public static final String MIME_TYPE_GML211 = "text/xml; subtype=gml/2.1.1";
	public static final String MIME_TYPE_GML212 = "text/xml; subtype=gml/2.1.2";
	public static final String MIME_TYPE_GML2121 = "text/xml; subtype=gml/2.1.2.1";
	public static final String MIME_TYPE_GML300 = "text/xml; subtype=gml/3.0.0";
	public static final String MIME_TYPE_GML301 = "text/xml; subtype=gml/3.0.1";
	public static final String MIME_TYPE_GML310 = "text/xml; subtype=gml/3.1.0";
	public static final String MIME_TYPE_GML311 = "text/xml; subtype=gml/3.1.1";
	public static final String MIME_TYPE_GML321 = "text/xml; subtype=gml/3.2.1";
	
	private static final String[] additionalSHPFileItems = {"shx", "dbf", "prj", "sbn", "sbx", "shp.xml"};
	private static final String[] additionalDBFFileItems = {"dbf.xml"}; // e.g. ArcGIS backend returns shape and a metadata xml file (e.g. process pointdistance)
	
	private static HashMap<String, String> lut;	
	
	public static final HashMap<String, String> mimeTypeFileTypeLUT(){
		
		if (lut == null) {

			lut = new HashMap<String, String>();

			Properties ioProperties = new Properties();

			try {

				ioProperties.load(GenericFileDataConstants.class.getResourceAsStream("/org/n52/wps/io/io.properties"));

				Enumeration<Object> en = ioProperties.keys();

				while (en.hasMoreElements()) {
					String type = (String) en.nextElement();
					lut.put(type, ioProperties.getProperty(type));
				}

			} catch (Exception e) {
				LOGGER.error("Exception while setting up Look up table.", e);
			} 
		}
		
		return lut;
	}
	
	public static final String[] getMimeTypes (){
		return mimeTypeFileTypeLUT().keySet().toArray(new String[0]);
	}
	
	public static final String[] getIncludeFilesByMimeType(String mimeType){
		
		String[] returnValue = null;
		
		if (mimeType != null && mimeType.equalsIgnoreCase("application/x-zipped-shp")){
			returnValue = additionalSHPFileItems;
		} if (mimeType != null && mimeType.equalsIgnoreCase("application/dbase")){
			returnValue = additionalDBFFileItems;
		}
		
		return returnValue;
		
	}
	
}
