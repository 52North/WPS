/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.util;

import java.util.concurrent.ConcurrentMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x20.ProcessDescriptionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.iceland.w3c.W3CConstants;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/*
 * 
 * Some conveniant methods, to access some XMLBean objects.
 * @author foerster
 *
 */
public class XMLBeansHelper {
	public static OutputDescriptionType findOutputByID(String outputID, OutputDescriptionType[] outputDescs) {
		for(OutputDescriptionType desc : outputDescs) {
			if(desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }

	public static InputDescriptionType findInputByID(String outputID, DataInputs inputs) {
		for(InputDescriptionType desc : inputs.getInputArray()) {
			if(desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }
	
	public static net.opengis.wps.x20.OutputDescriptionType findOutputByID(String outputID, net.opengis.wps.x20.OutputDescriptionType[] outputDescs) {
		for(net.opengis.wps.x20.OutputDescriptionType desc : outputDescs) {
			if(desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }

	public static net.opengis.wps.x20.InputDescriptionType findInputByID(String inputID, ProcessDescriptionType descType) {
		for(net.opengis.wps.x20.InputDescriptionType desc : descType.getInputArray()) {
			if(desc.getIdentifier().getStringValue().equals(inputID)) {
                return desc;
            }
        }
        return null;
    }

    /**
     * @return the default XmlOptions used in responses
     */
    public static XmlOptions getXmlOptions() {
        return new XmlOptions()
                .setSaveNamespacesFirst()
                .setSaveSuggestedPrefixes(PREFIXES)
                .setSaveAggressiveNamespaces()
                .setSavePrettyPrint();
    }

    /**
     * Registers a prefix for a namespace to be used in responses.
     *
     * @param namespace the XML namespace
     * @param prefix    the prefix
     */
    public static void registerPrefix(String namespace, String prefix) {
        PREFIXES.put(Preconditions.checkNotNull(Strings.emptyToNull(namespace)),
                     Preconditions.checkNotNull(Strings.emptyToNull(prefix)));
    }
    
    /**
     * TODO javadoc
     * 
     * @param object
     * @param schemaLocation
     */
    public static void addSchemaLocationToXMLObject(XmlObject object, String schemaLocation){
    	
		XmlCursor c = object.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(W3CConstants.QN_SCHEMA_LOCATION_PREFIXED, schemaLocation);
    	
    }

    /**
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_WPS_1_0_0 = "http://www.opengis.net/wps/1.0.0";
    
    /**
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_WPS_2_0 = "http://www.opengis.net/wps/2.0";

    /**
     * The prefix for WPS 1.0.0: {@value}.
     */
    public static final String NS_WPS_PREFIX = "wps";

    /**
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_OWS_1_1 = "http://www.opengis.net/ows/1.1";
    
    /**
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_OWS_2_0 = "http://www.opengis.net/ows/2.0";

    /**
     * The prefix for WPS 1.0.0: {@value}.
     */
    public static final String NS_OWS_PREFIX = "ows";

    /**
     * The namespace for XSI: {@value}.
     */
    public static final String NS_XSI
            = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The prefix for XSI: {@value}.
     */
    public static final String NS_XSI_PREFIX = "xsi";

    private static final ConcurrentMap<String, String> PREFIXES;

    static {
        PREFIXES = Maps.newConcurrentMap();
        PREFIXES.put(NS_XSI, NS_XSI_PREFIX);
        PREFIXES.put(NS_WPS_1_0_0, NS_WPS_PREFIX);
        PREFIXES.put(NS_WPS_2_0, NS_WPS_PREFIX);
        PREFIXES.put(NS_OWS_1_1, NS_OWS_PREFIX);
        PREFIXES.put(NS_OWS_2_0, NS_OWS_PREFIX);
    }

}
