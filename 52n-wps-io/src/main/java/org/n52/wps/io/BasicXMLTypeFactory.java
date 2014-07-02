/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.impl.util.Base64;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
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

	private final static Logger LOGGER = LoggerFactory.getLogger(BasicXMLTypeFactory.class);
    
	// List of supported basic XML datatypes.
	public static final String DOUBLE_URI = "xs:double";
	public static final String FLOAT_URI = "xs:float";
	public static final String INTEGER_URI = "xs:integer";
	public static final String LONG_URI = "xs:long";
	public static final String INT_URI = "xs:int";
	public static final String SHORT_URI = "xs:short";
	public static final String BYTE_URI = "xs:byte";
	public static final String BOOLEAN_URI = "xs:boolean";
	public static final String STRING_URI = "xs:string";
	public static final String DATETIME_URI = "xs:dateTime";
	public static final String DATE_URI = "xs:date";
	public static final String BASE64BINARY_URI = "xs:base64Binary";
    public static final String ANYURI_URI = "xs:anyURI";
    
    private final static DatatypeFactory DATATYPE_FACTORY;

    static {
        DatatypeFactory datatypeFactory = null;
        try {
            // Is this thread safe?
            // bah, a checked exception on factory instantiation?
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            LOGGER.error("Error creating DatatypeFactory for xs:datTime and xs:dateParsing");
        }
        DATATYPE_FACTORY = datatypeFactory;
    }
	
	private BasicXMLTypeFactory(){
		
	}
	
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
		} else if (xmlDataTypeURI.equalsIgnoreCase(FLOAT_URI)) {
			return new LiteralFloatBinding(Float.parseFloat(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(DOUBLE_URI)) {
			return new LiteralDoubleBinding(Double.parseDouble(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(LONG_URI)) {
			return new LiteralLongBinding(Long.parseLong(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(INT_URI) || xmlDataTypeURI.equalsIgnoreCase(INTEGER_URI)) {
			return new LiteralIntBinding(Integer.parseInt(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(SHORT_URI)) {
			return new LiteralShortBinding(Short.parseShort(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(BYTE_URI)) {
			return new LiteralByteBinding(Byte.parseByte(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(BOOLEAN_URI)) {
			return new LiteralBooleanBinding(Boolean.parseBoolean(obj));
		} else if (xmlDataTypeURI.equalsIgnoreCase(STRING_URI)) {
			return new LiteralStringBinding(obj);
		} else if (xmlDataTypeURI.equalsIgnoreCase(DATETIME_URI) || xmlDataTypeURI.equalsIgnoreCase(DATE_URI)) {
            try {
                return new LiteralDateTimeBinding(DATATYPE_FACTORY.newXMLGregorianCalendar(obj).toGregorianCalendar().getTime());
            } catch (Exception e) {
				LOGGER.error("Could not parse xs:dateTime or xs:date data", e);
				return null;
            }
		} else if (xmlDataTypeURI.equalsIgnoreCase(BASE64BINARY_URI)) {
			return new LiteralBase64BinaryBinding(Base64.decode(obj.getBytes()));
		} else if (xmlDataTypeURI.equalsIgnoreCase(ANYURI_URI)) {
            try {
                return new LiteralAnyURIBinding(new URI(obj));
            } catch (URISyntaxException e) {
				LOGGER.error("Could not parse anyURI data", e);
				return null;
            }
		} else {
            return null;
        }
	}

   public static String getStringRepresentation(String xmlDataTypeURI, IData obj) {
	   return obj.getPayload().toString();
   }
   
   public static Class<? extends ILiteralData> getBindingForPayloadType(Class<?> payloadType) {
        if (payloadType.equals(float.class) || payloadType.equals(Float.class)) {
            return LiteralFloatBinding.class;
        }
        if (payloadType.equals(double.class) || payloadType.equals(Double.class)) {
            return LiteralDoubleBinding.class;
        }
        if (payloadType.equals(long.class) || payloadType.equals(Long.class)) {
            return LiteralLongBinding.class;
        }
        if (payloadType.equals(int.class) || payloadType.equals(Integer.class)) {
            return LiteralIntBinding.class;
        }
        if (payloadType.equals(short.class) || payloadType.equals(Short.class)) {
            return LiteralShortBinding.class;
        }
        if (payloadType.equals(byte.class) || payloadType.equals(Byte.class)) {
            return LiteralByteBinding.class;
        }
        if (payloadType.equals(boolean.class) || payloadType.equals(Boolean.class)) {
            return LiteralBooleanBinding.class;
        }
        if (payloadType.equals(String.class)) {
            return LiteralStringBinding.class;
        }
        if (payloadType.equals(Date.class)) {
            return LiteralDateTimeBinding.class;
        }
        if (payloadType.equals(byte[].class)) {
            return LiteralBase64BinaryBinding.class;
        }
        if (payloadType.equals(URI.class)) {
            return LiteralAnyURIBinding.class;
        }
        return null;
    }

    public static String getXMLDataTypeforBinding(Class<? extends ILiteralData> clazz) {
        if (LiteralFloatBinding.class.isAssignableFrom(clazz)) {
            return FLOAT_URI;
        } else if (LiteralDoubleBinding.class.isAssignableFrom(clazz)) {
            return DOUBLE_URI;
        } else if (LiteralLongBinding.class.isAssignableFrom(clazz)) {
            return LONG_URI;
        } else if (LiteralIntBinding.class.isAssignableFrom(clazz)) {
            return INT_URI;
//            return INTEGER_URI;
        } else if (LiteralShortBinding.class.isAssignableFrom(clazz)) {
            return SHORT_URI;
        } else if (LiteralByteBinding.class.isAssignableFrom(clazz)) {
            return BYTE_URI;
        } else if (LiteralBooleanBinding.class.isAssignableFrom(clazz)) {
            return BOOLEAN_URI;
        } else if (LiteralStringBinding.class.isAssignableFrom(clazz)) {
            return STRING_URI;
        } else if (LiteralDateTimeBinding.class.isAssignableFrom(clazz)) {
            return DATETIME_URI;
//            return DATE_URI;
        } else if (LiteralBase64BinaryBinding.class.isAssignableFrom(clazz)) {
            return  BASE64BINARY_URI;
        } else if (LiteralAnyURIBinding.class.isAssignableFrom(clazz)) {
            return ANYURI_URI;
        }
        return null;
    }

}
