package org.n52.wps.server.request;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;

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
                    try { configInputStream.close(); } catch (IOException ignore) {}
                }
            }
    }

}