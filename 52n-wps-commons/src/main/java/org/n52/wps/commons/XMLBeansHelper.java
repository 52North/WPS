/*
 * Copyright (C) 2006-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons;

import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x20.ProcessDescriptionType;

/*
 *
 * Some conveniant methods, to access some XMLBean objects.
 * @author foerster
 *
 */
public class XMLBeansHelper {

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
    public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The prefix for XSI: {@value}.
     */
    public static final String NS_XSI_PREFIX = "xsi";

    private static final ConcurrentMap<String, String> PREFIXES;

    public static OutputDescriptionType findOutputByID(String outputID,
            OutputDescriptionType[] outputDescs) {
        for (OutputDescriptionType desc : outputDescs) {
            if (desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }

    public static net.opengis.wps.x20.OutputDescriptionType findOutputByID(String outputID,
            net.opengis.wps.x20.OutputDescriptionType[] outputDescs) {
        for (net.opengis.wps.x20.OutputDescriptionType desc : outputDescs) {
            if (desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }

    public static InputDescriptionType findInputByID(String outputID,
            DataInputs inputs) {
        for (InputDescriptionType desc : inputs.getInputArray()) {
            if (desc.getIdentifier().getStringValue().equals(outputID)) {
                return desc;
            }
        }
        return null;
    }

    public static net.opengis.wps.x20.InputDescriptionType findInputByID(String inputID,
            ProcessDescriptionType descType) {
        for (net.opengis.wps.x20.InputDescriptionType desc : descType.getInputArray()) {
            if (desc.getIdentifier().getStringValue().equals(inputID)) {
                return desc;
            }
        }
        return null;
    }

    /**
     * @return the default XmlOptions used in responses
     */
    public static XmlOptions getXmlOptions() {
        return new XmlOptions().setSaveNamespacesFirst().setSaveSuggestedPrefixes(PREFIXES)
                .setSaveAggressiveNamespaces().setSavePrettyPrint();
    }

    /**
     * Registers a prefix for a namespace to be used in responses.
     *
     * @param namespace
     *            the XML namespace
     * @param prefix
     *            the prefix
     */
    public static void registerPrefix(String namespace,
            String prefix) {
        PREFIXES.put(Preconditions.checkNotNull(Strings.emptyToNull(namespace)),
                Preconditions.checkNotNull(Strings.emptyToNull(prefix)));
    }

    /**
     * Adds a schema location attribute to an XMLObject
     *
     * @param object
     *            the XMLObject
     * @param schemaLocation
     *            the schema location
     */
    public static void addSchemaLocationToXMLObject(XmlObject object,
            String schemaLocation) {

        XmlCursor c = object.newCursor();
        c.toFirstChild();
        c.toLastAttribute();
        c.setAttributeText(new QName(NS_XSI, "schemaLocation"), schemaLocation);

    }

    static {
        PREFIXES = Maps.newConcurrentMap();
        PREFIXES.put(NS_XSI, NS_XSI_PREFIX);
        PREFIXES.put(NS_WPS_1_0_0, NS_WPS_PREFIX);
        PREFIXES.put(NS_WPS_2_0, NS_WPS_PREFIX);
        PREFIXES.put(NS_OWS_1_1, NS_OWS_PREFIX);
        PREFIXES.put(NS_OWS_2_0, NS_OWS_PREFIX);
    }

}
