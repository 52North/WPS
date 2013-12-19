/**
 * ﻿Copyright (C) 2006
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
package org.n52.wps.commons;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.xmlbeans.XmlException;

/**
 *
 * @author tkunicki
 */
public class WPSConfigTestUtil {

    public static void generateMockConfig(String path) throws XmlException, IOException {
        generateMockConfig(WPSConfigTestUtil.class, path);
    }
    
    public static void generateMockConfig(Class clazz, String path) throws XmlException, IOException {

            InputStream configInputStream = null;
            try {
                configInputStream = new BufferedInputStream(clazz.getResourceAsStream(path));
                WPSConfig.forceInitialization(configInputStream);
                
            } finally {
                if (configInputStream != null) {
                    try { configInputStream.close(); } catch (IOException ignore) {
                        // do nothing
                    }
                }
            }
    }

}