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

package org.n52.wps.util;

import java.util.concurrent.ConcurrentMap;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;

import org.apache.xmlbeans.XmlOptions;

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
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_WPS_1_0_0 = "http://www.opengis.net/wps/1.0.0";

    /**
     * The prefix for WPS 1.0.0: {@value}.
     */
    public static final String NS_WPS_PREFIX = "wps";

    /**
     * The namespace for WPS 1.0.0: {@value}.
     */
    public static final String NS_OWS_1_1 = "http://www.opengis.net/ows/1.1";

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
        PREFIXES.put(NS_OWS_1_1, NS_OWS_PREFIX);
    }

}
