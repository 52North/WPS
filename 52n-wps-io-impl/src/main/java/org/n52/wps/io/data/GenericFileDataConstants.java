/***************************************************************
Copyright 2009 52 North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden
 
 Contact: Andreas Wytzisk, 
 52 North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundations web page, http://www.fsf.org.

 ***************************************************************/

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
