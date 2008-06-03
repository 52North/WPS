/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

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
package org.n52.wps.util;

import org.apache.log4j.Logger;

public class BasicXMLTypeFactory {
	
	private static Logger LOGGER = Logger.getLogger(BasicXMLTypeFactory.class);
	// List of supported basic XML datatypes.
	public static String DOUBLE_URI = "xs:double";
	public static String INT_URI = "xs:int";
	public static String BOOLEAN_URI = "xs:boolean";
	public static String STRING_URI = "xs:string";
	
	/**
	 * This is a helper method to create always the correct Java Type out of a string. 
	 * It is based on the basic schema datatypes.
	 * If xmlDataTypeURI is null, string dataType will be assumed.
	 * @param xmlDataTypeURI the expected XML basicDataType
	 * @param obj the XML object String
	 */
	public static Object getBasicJavaObject(String xmlDataTypeURI, String obj) {
		obj = obj.replace('\n', ' ').replace('\t', ' ').trim();
		if(xmlDataTypeURI == null) {
			return obj;
		}
		if(xmlDataTypeURI.equals(DOUBLE_URI)) {
			return Double.parseDouble(obj);
		}
		if(xmlDataTypeURI.equals(INT_URI)) {
			return Integer.parseInt(obj);
		}
		if(xmlDataTypeURI.equals(BOOLEAN_URI)) {
			return Boolean.parseBoolean(obj);
		}
		if(xmlDataTypeURI.equals(STRING_URI)) {
			return obj;
		}
		return null;
		
	}
	
   public static String getStringRepresentation(String xmlDataTypeURI, Object obj) {
        if (xmlDataTypeURI.equals(DOUBLE_URI) && obj instanceof Double) {
            return ((Double)obj).toString();
        }
        if (xmlDataTypeURI.equals(INT_URI) && obj instanceof Integer) {
            return ((Integer)obj).toString();
        }
        if (xmlDataTypeURI.equals(BOOLEAN_URI) && obj instanceof Boolean) {
            return ((Boolean)obj).toString();
        }
        if (xmlDataTypeURI.equals(STRING_URI) && obj instanceof String) {
            return (String)obj;
        }
        LOGGER.debug(xmlDataTypeURI + " is unknown or object is not matching the dataType");
        return null;
    }

}
