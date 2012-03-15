package org.n52.wps.io.test.datahandler;
import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;

import junit.framework.TestCase;


public abstract class AbstractTestCase extends TestCase {

	protected String projectRoot;

	public AbstractTestCase(){
		
		
		System.out.println(WPSConfig.getConfigPath());
		
		
		File f = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
		
		projectRoot = f.getParentFile().getParentFile().getParent();//Project root
		
		/*
		 * now navigate to 52n-wps-webapp/src/main/resources/webapp/config/wps_config.xml
		 */						
		try {	
		String configFilePath = WPSConfig.getConfigPath();
		WPSConfig.forceInitialization(configFilePath);
		} catch (XmlException e1) {
			fail(e1.getMessage());
		} catch (IOException e1) {
			fail(e1.getMessage());
		}
		
	}
	
}
