/**
 * Copyright (C) 2007 - 2015 52°North Initiative for Geospatial Open Source
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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.generator;

import static org.n52.iceland.service.MiscSettings.CHARACTER_ENCODING;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.xmlbeans.XmlOptions;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.ogc.OGCConstants;
import org.n52.iceland.util.Producer;
import org.n52.iceland.util.Validation;
import org.n52.iceland.w3c.W3CConstants;

/**
 * XML utility class
 *
 * @since 4.0.0
 *
 */
@Configurable
public final class SimpleXmlOptionsHelper implements Producer<XmlOptions> {

    private final ReentrantLock lock = new ReentrantLock();
    private XmlOptions xmlOptions;
    private String characterEncoding = "UTF-8";
    private boolean prettyPrint = true;

    // TODO: To be used by other encoders to have common prefixes
    private Map<String, String> getPrefixMap() {
        final Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put(OGCConstants.NS_OGC, OGCConstants.NS_OGC_PREFIX);
        prefixMap.put(W3CConstants.NS_XLINK, W3CConstants.NS_XLINK_PREFIX);
        prefixMap.put(W3CConstants.NS_XSI, W3CConstants.NS_XSI_PREFIX);
        prefixMap.put(W3CConstants.NS_XS, W3CConstants.NS_XS_PREFIX);
        return prefixMap;
    }

    /**
     * Get the XML options for SOS 1.0.0
     *
     * @return SOS 1.0.0 XML options
     */
    public XmlOptions getXmlOptions() {
        if (xmlOptions == null) {
            lock.lock();
            try {
                if (xmlOptions == null) {
                    xmlOptions = new XmlOptions();
                    Map<String, String> prefixes = getPrefixMap();
                    xmlOptions.setSaveSuggestedPrefixes(prefixes);
                    xmlOptions.setSaveImplicitNamespaces(prefixes);
                    xmlOptions.setSaveAggressiveNamespaces();
                    if (prettyPrint) {
                        xmlOptions.setSavePrettyPrint();
                    }
                    xmlOptions.setSaveNamespacesFirst();
                    xmlOptions.setCharacterEncoding(characterEncoding);
                }
            } finally {
                lock.unlock();
            }
        }
        return xmlOptions;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        lock.lock();
        try {
            if (this.prettyPrint != prettyPrint) {
                setReload();
            }
            this.prettyPrint = prettyPrint;
        } finally {
            lock.unlock();
        }
    }

    @Setting(CHARACTER_ENCODING)
    public void setCharacterEncoding(String characterEncoding) {
        lock.lock();
        try {
            Validation.notNullOrEmpty("Character Encoding", characterEncoding);
            if (!this.characterEncoding.equals(characterEncoding)) {
                setReload();
            }
            this.characterEncoding = characterEncoding;
        } finally {
            lock.unlock();
        }
    }

    private void setReload() {
        lock.lock();
        try {
            xmlOptions = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public XmlOptions get() {
        return getXmlOptions();
    }
}
