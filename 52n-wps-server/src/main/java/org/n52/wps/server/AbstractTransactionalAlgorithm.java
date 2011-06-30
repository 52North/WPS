package org.n52.wps.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.n52.wps.util.XMLUtils;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm {

	protected String algorithmID;

	public AbstractTransactionalAlgorithm(String algorithmID) {
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
	 * @param processDescription
	 */
	public static void setDescription(String processId,
			ProcessDescriptionType processDescription) {
		String fullPath = AbstractTransactionalAlgorithm.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1);
		}
		File directory = new File(subPath + "WEB-INF/ProcessDescriptions/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String path = subPath + "WEB-INF/ProcessDescriptions/" + processId
				+ ".xml";
		try {
			// TODO handling when exception occurs ...
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

}
