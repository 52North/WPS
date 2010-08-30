/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

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

package org.n52.wps.io.data;


import java.util.HashMap;

import org.n52.wps.io.IOHandler;



public final class GenericFileDataConstants {
	
	public static final String MIME_TYPE_ZIPPED_SHP = IOHandler.MIME_TYPE_ZIPPED_SHP;
	public static final String MIME_TYPE_HDF = "application/img";
	public static final String MIME_TYPE_GEOTIFF = "application/geotiff";
	public static final String MIME_TYPE_TIFF = "image/tiff";
	public static final String MIME_TYPE_DBASE = "application/dbase";
	public static final String MIME_TYPE_REMAPFILE = "application/remap";
	public static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
	public static final String MIME_TYPE_TEXT_XML = "text/xml";
	
	
	public static final HashMap<String, String> mimeTypeFileTypeLUT(){
		
		HashMap<String, String> lut = new HashMap<String, String>();
		
		lut.put(MIME_TYPE_ZIPPED_SHP, "shp");
		lut.put(MIME_TYPE_HDF, "img");
		lut.put(MIME_TYPE_GEOTIFF, "tif");
		lut.put(MIME_TYPE_TIFF, "tif");
		lut.put(MIME_TYPE_DBASE, "dbf");
		lut.put(MIME_TYPE_REMAPFILE, "RMP");
		lut.put(MIME_TYPE_PLAIN_TEXT, "txt");
		lut.put(MIME_TYPE_TEXT_XML, "xml");
		
		return lut;
	}
	
	public static final String[] getMimeTypes (){
		return mimeTypeFileTypeLUT().keySet().toArray(new String[0]);
	}
	
	
	//public static final String RASTER_SCHEMA = "http://tu-dresden.de/fgh/geo/gis/schemas/esri/raster.xsd";
	//public static final String VECTOR_SCHEMA = "http://tu-dresden.de/fgh/geo/gis/schemas/esri/shape.xsd";
	
	private static final String[] additionalSHPFileItems = {"shx", "dbf", "prj"};
	
	public static final String[] getIncludeFilesByMimeType(String mimeType){
		
		String[] returnValue = null;
		
		if (mimeType.equalsIgnoreCase("application/x-zipped-shp")){
			returnValue = additionalSHPFileItems;
		}
		
		return returnValue;
		
	}
	
}
