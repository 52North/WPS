package org.n52.wps.io.test.datahandler;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.AbstractIOHandler;


public abstract class AbstractTestCase<T  extends AbstractIOHandler> extends TestCase {

	private Logger LOGGER = Logger.getLogger(AbstractTestCase.class);
	
	protected String projectRoot;

	protected T dataHandler;
	
	public AbstractTestCase() {
		
		BasicConfigurator.configure();
		
		File f = new File(this.getClass().getProtectionDomain().getCodeSource()
				.getLocation().getFile());

		projectRoot = f.getParentFile().getParentFile().getParent();																	

		try {
			String configFilePath = WPSConfig.tryToGetPathFromWebAppSource();
			if(configFilePath==null){
				configFilePath = WPSConfig.getConfigPath();
			}
			WPSConfig.forceInitialization(configFilePath);
		} catch (XmlException e1) {
			fail(e1.getMessage());
		} catch (IOException e1) {
			fail(e1.getMessage());
		}

		initializeDataHandler();

	}
	
	protected boolean isDataHandlerActive(){
		
		if(dataHandler == null){
			LOGGER.info("Data handler not initialized in test class " + this.getClass().getName());
			return false;
		}
		
		String className = dataHandler.getClass().getName();
		
		if(!(WPSConfig.getInstance().isGeneratorActive(className)||WPSConfig.getInstance().isParserActive(className))){
			LOGGER.info("Skipping inactive data handler: " + className);
			return false;
		}
		return true;
	}
	
	protected abstract void initializeDataHandler();
	
}
