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
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntegerBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

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
	public static IData getBasicJavaObject(String xmlDataTypeURI, String obj) {
		obj = obj.replace('\n', ' ').replace('\t', ' ').trim();
		if(xmlDataTypeURI == null) {
			return new LiteralStringBinding(obj);
		}
		if(xmlDataTypeURI.equals(DOUBLE_URI)) {
			return new LiteralDoubleBinding(Double.parseDouble(obj));
		}
		if(xmlDataTypeURI.equals(INT_URI)) {
			return new LiteralIntegerBinding(Integer.parseInt(obj));
		}
		if(xmlDataTypeURI.equals(BOOLEAN_URI)) {
			return new LiteralBooleanBinding(Boolean.parseBoolean(obj));
		}
		if(xmlDataTypeURI.equals(STRING_URI)) {
			return new LiteralStringBinding(obj);
		}
		return null;
		
	}
	
   public static String getStringRepresentation(String xmlDataTypeURI, IData obj) {
	   return obj.getPayload().toString();
    }

}
