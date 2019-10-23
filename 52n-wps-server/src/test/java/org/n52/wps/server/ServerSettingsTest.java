package org.n52.wps.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.common.AbstractITClass;

public class ServerSettingsTest extends AbstractITClass {

    @Test
    public void testGetMaxRequestSize(){
        
        WPSConfig.getInstance().getServerConfigurationModule().setMaxRequestSize(134);
        
        int maxRequestSizeMB = WPSConfig.getInstance().getServerConfigurationModule().getMaxRequestSize();
        
        assertNotNull(maxRequestSizeMB);
        
        assertTrue(maxRequestSizeMB == 134);
        
    }
    
}
