package org.n52.wps.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.repository.DefaultTransactionalProcessRepository;
import org.n52.wps.util.XMLUtils;

import net.opengis.wps.x100.DataDescriptionDocument;
import net.opengis.wps.x100.DataDescriptionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

public abstract class AbstractTransactionalData  {

	protected String algorithmID;
	private static Logger LOGGER = Logger
	.getLogger(AbstractTransactionalData.class);

	public AbstractTransactionalData(String algorithmID) {
		this.algorithmID = algorithmID;

	}

	public String getAlgorithmID() {
		return algorithmID;
	}

	public abstract HashMap run(ExecuteDocument document);

	/** call the backend to cancel the task */
	public void cancel() {
	}

	/**
	 * Writes the ProcessDescription in the appropriate directory. Additional
	 * method for transactional operation deploy. Note : It is implemented in
	 * the parent abstract class and (TODO?) maybe the (initializeDescription)
	 * should be here too ? TODO : everything about the ProcessDescription
	 * should move to a ProcessDescriptionManager, or be added to
	 * FlatFileDatabase (if considered to belong to the DataBase?)
	 * 
	 * @param processId
	 * @param dataDescription
	 */
	public static void setDescription(String processId,
			DataDescriptionType dataDescription) {
		String fullPath = AbstractTransactionalData.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		File directory = new File(subPath + "WEB-INF/DataDescriptions/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String path = subPath + "WEB-INF/DataDescriptions/" + processId
				+ ".xml";
		LOGGER.info("File path..."+path);
		LOGGER.info("Description Writing file..."+dataDescription);
		try {
			// TODO handling when exception occurs ...
			XMLUtils.writeXmlFile(dataDescription.getDomNode(), path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		LOGGER.info("Written");
	}

	// TODO enhance code (repeated code)
	/**
	 * Read the Process Description for the given Process Id.
	 */
	public static DataDescriptionType getDescription(String processId) {
		String fullPath = AbstractTransactionalData.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/DataDescriptions/" + processId
				+ ".xml";
		LOGGER.info(path);
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			File descFile = new File(path);
			DataDescriptionDocument descDom = DataDescriptionDocument.Factory.parse(descFile,option);
			return descDom.getDataDescription();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void removeDescription(String processId) {
		String fullPath = AbstractTransactionalData.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/DataDescriptions/" + processId
				+ ".xml";
		File descFile = new File(path);
		descFile.delete();
	}
	
}
