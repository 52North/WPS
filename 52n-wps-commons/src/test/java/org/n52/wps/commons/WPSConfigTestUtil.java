/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
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