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

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.apache.xmlbeans.impl.util.Base64;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

public class BasicXMLTypeFactory {
	
	private static Logger LOGGER = Logger.getLogger(BasicXMLTypeFactory.class);
	// List of supported basic XML datatypes.
	public static String DOUBLE_URI = "xs:double";
	public static String FLOAT_URI = "xs:float";
	public static String INTEGER_URI = "xs:integer";
	public static String LONG_URI = "xs:long";
	public static String INT_URI = "xs:int";
	public static String SHORT_URI = "xs:short";
	public static String BYTE_URI = "xs:byte";
	public static String BOOLEAN_URI = "xs:boolean";
	public static String STRING_URI = "xs:string";
	public static String DATETIME_URI = "xs:dateTime";
	public static String BASE64BINARY_URI = "xs:base64Binary";

	/**
	 * This is a helper method to create always the correct Java Type out of a string. 
	 * It is based on the basic schema datatypes.
	 * If xmlDataTypeURI is null, string dataType will be assumed.
	 * @param xmlDataTypeURI the expected XML basicDataType
	 * @param obj the XML object String
	 */
	public static IData getBasicJavaObject(String xmlDataTypeURI, String obj) {
		obj = obj.replace('\n', ' ').replace('\t', ' ').trim();
		if (xmlDataTypeURI == null) {
			return new LiteralStringBinding(obj);
		} else if (xmlDataTypeURI.equals(FLOAT_URI)) {
			return new LiteralFloatBinding(Float.parseFloat(obj));
		} else if (xmlDataTypeURI.equals(DOUBLE_URI)) {
			return new LiteralDoubleBinding(Double.parseDouble(obj));
		} else if (xmlDataTypeURI.equals(LONG_URI)
				|| xmlDataTypeURI.equals(INTEGER_URI)) {
			return new LiteralLongBinding(Long.parseLong(obj));
		} else if (xmlDataTypeURI.equals(INT_URI)) {
			return new LiteralIntBinding(Integer.parseInt(obj));
		} else if (xmlDataTypeURI.equals(SHORT_URI)) {
			return new LiteralShortBinding(Short.parseShort(obj));
		} else if (xmlDataTypeURI.equals(BYTE_URI)) {
			return new LiteralByteBinding(Byte.parseByte(obj));
		} else if (xmlDataTypeURI.equals(BOOLEAN_URI)) {
			return new LiteralBooleanBinding(Boolean.parseBoolean(obj));
		} else if (xmlDataTypeURI.equals(STRING_URI)) {
			return new LiteralStringBinding(obj);
		} else if (xmlDataTypeURI.equals(DATETIME_URI)) {
			try {
				return new LiteralDateTimeBinding(new XmlSchemaDateFormat()
						.parse(obj));
			} catch (ParseException e) {
				LOGGER.error("Could not parse dateTime data", e);
				return null;
			}
		} else if (xmlDataTypeURI.equals(BASE64BINARY_URI)) {
			return new LiteralBase64BinaryBinding(Base64.decode(obj.getBytes()));
		} else {
			return null;
		}
	}
	
   public static String getStringRepresentation(String xmlDataTypeURI, IData obj) {
	   return obj.getPayload().toString();
    }

}
