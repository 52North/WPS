package org.n52.wps.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.repository.DefaultTransactionalProcessRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.GetAuditDocument;
import net.opengis.wps.x100.ProcessDescriptionDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm {

	protected String algorithmID;
	private static Logger LOGGER = Logger
			.getLogger(DefaultTransactionalProcessRepository.class);

	public AbstractTransactionalAlgorithm(String algorithmID) {
		this.algorithmID = algorithmID;

	}

	public String getAlgorithmID() {
		return algorithmID;
	}

	public abstract HashMap run(ExecuteRequest document)
			throws ExceptionReport;

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
	 * @param processDescription
	 */
	public static void setDescription(String processId,
			ProcessDescriptionType processDescription) {
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		/**
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		*/
				File directory = new File(subPath + "WEB-INF/ProcessDescriptions/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String path = subPath + "WEB-INF/ProcessDescriptions/" + processId
				+ ".xml";
		try {
			// TODO handling when exception occurs ...
			LOGGER.info("*************************=========. write "+path);
			XMLUtils.writeXmlFile(processDescription.getDomNode(), path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	// TODO enhance code (repeated code)
	/**
	 * Read the Process Description for the given Process Id.
	 */
	public static ProcessDescriptionType getDescription(String processId) {
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/ProcessDescriptions/" + processId
				+ ".xml";
		LOGGER.info(path);
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			File descFile = new File(path);
			ProcessDescriptionDocument descDom = ProcessDescriptionDocument.Factory
					.parse(descFile, option);
			return descDom.getProcessDescription();
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
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/ProcessDescriptions/" + processId
				+ ".xml";
		File descFile = new File(path);
		descFile.delete();
	}

	public static void storeAuditDocument(String instanceId,
			AuditTraceType auditTraceType) {

		LOGGER.info("Storing audit:" + instanceId);
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		File directory = new File(subPath + "WEB-INF/AuditDocuments/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String path = subPath + "WEB-INF/AuditDocuments/" + instanceId + ".xml";
		try {
			// TODO handling when exception occurs ...
			auditTraceType.save(new File(path));
			// XMLUtils.writeXmlFile((Document)auditTraceType.getDomNode(),
			// path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		LOGGER.info("Audit document is stored in:" + subPath);
	}

	public static AuditTraceType getAuditDocument(String instanceId) {
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/AuditDocuments/" + instanceId + ".xml";
		LOGGER.info("Retrieving in " + path);
		AuditTraceType auditDom = null;
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			File auditFile = new File(path);
			auditDom = AuditTraceType.Factory.parse(auditFile);

		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return auditDom;
	}

	public static void storeAuditLongDocument(String instanceId,
			AuditTraceType auditTraceType) {

		LOGGER.info("Storing audit:" + instanceId);
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		File directory = new File(subPath + "WEB-INF/AuditDocuments/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String path = subPath + "WEB-INF/AuditDocuments/" + instanceId
				+ "_long.xml";
		try {
			// TODO handling when exception occurs ...
			auditTraceType.save(new File(path));
			// XMLUtils.writeXmlFile((Document)auditTraceType.getDomNode(),
			// path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		LOGGER.info("Audit document is stored in:" + subPath);
	}

	public static AuditTraceType getAuditLongDocument(String instanceId) {
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		String path = subPath + "WEB-INF/AuditDocuments/" + instanceId
				+ "_long.xml";
		LOGGER.info("Retrieving in " + path);
		AuditTraceType auditDom = null;
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			File auditFile = new File(path);
			auditDom = AuditTraceType.Factory.parse(auditFile);

		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return auditDom;
	}

	public AuditTraceType getAudit() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public AuditTraceType getAuditLongForm() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void storeAudit() {
		// TODO Auto-generated method stub

	}

	public void callback(ExecuteResponseDocument execRespDom) {
		
	}

}
