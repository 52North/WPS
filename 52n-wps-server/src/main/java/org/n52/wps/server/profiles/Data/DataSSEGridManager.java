package org.n52.wps.server.profiles.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractDataManager;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.repository.DefaultTransactionalDataRepository;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployDataRequest;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.UndeployDataRequest;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * TODO this class was based on transactional branch implementation. However the
 * invoke method was reimplemented Therefore there is a doublon implementation
 * for sending request. *
 * 
 * @author cnl
 * 
 */
public class DataSSEGridManager extends AbstractDataManager {

	private static Logger LOGGER = Logger.getLogger(DataSSEGridManager.class);
	private String deployDir;

	public String getDeployDir() {
		return deployDir;
	}

	public void setDeployDir(String deployDir) {
		this.deployDir = deployDir;
	}

	private ExecuteResponseDocument executeResponse;

	// Asychronous execute client must be shared between threads
	/**
	 * @param repository
	 */
	public DataSSEGridManager(DefaultTransactionalDataRepository repository) {
		super(repository);
		/**
		 * Get the properties of the repository (in regard of the repository
		 * name instead of repository class) (This may also be done with
		 * this.className)
		 */
		Property deployDirProperty = repository
				.getPropertyForKey("DataDeployDirectory");
		if (deployDirProperty == null) {
			throw new RuntimeException("Error. Could not find data deployDir");
		}
		setDeployDir(deployDirProperty.getStringValue());

	}

	/**
	 * Signature should move to void (exception if failure)
	 */
	public boolean deployData(DeployDataRequest request) throws Exception {

		DeploymentProfile profile = request.getDeploymentProfile();
		if (!(profile instanceof DataDeploymentProfile)) {
			throw new Exception("Requested Deployment Profile not supported");
		}
		DataDeploymentProfile deploymentProfile = (DataDeploymentProfile) profile;
		String processID = deploymentProfile.getDataId();
		byte[] archive = deploymentProfile.getArchive();
		// TODO write File
		LOGGER.info("DATA SSEGRID Write test");
		storeArchive(deploymentProfile.getArchive(), processID);
		LOGGER.info("DATA SSEGRID Write test done");
		return true;
	}

	/**
	 * Unzip a collection files contained in a archive zip file and write onto
	 * disk
	 * 
	 * @param archive
	 * @throws IOException
	 * @throws ExceptionReport
	 */
	private void storeArchive(byte[] archive, String processId)
			throws IOException, ExceptionReport {
		String archiveDir = getDeployDir() + processId + File.separator;
		File archiveFile = new File(archiveDir);
		archiveFile.mkdirs();
		// create directory

		LOGGER.info("Storing archive in " + archiveDir);
		ByteArrayInputStream bais = new ByteArrayInputStream(archive);
		byte[] buf = new byte[1024];
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipentry = zis.getNextEntry();
		if (zipentry == null) {
			// Try tgz
			// TODO remove not necessary to write ??
			FileOutputStream fos = new FileOutputStream(getDeployDir()
					+ processId + "temp.tgz");
			fos.write(archive);
			File tarFile = new File(getDeployDir() + processId + "temp.tgz");
			FileInputStream fis = new FileInputStream(tarFile);
			LOGGER.info("Archive is not a zip file");
			GZIPInputStream gzis = new GZIPInputStream(fis);
			TarInputStream is = new TarInputStream(gzis);
			TarEntry tarentry = is.getNextEntry();
			if (tarentry == null) {
				LOGGER.warn("Archive is not zip neither a tar file!");
				throw new ExceptionReport("Archive is not a zip or tar file.",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
			while (tarentry != null) {
				try {
					
					String entryName = tarentry.getName();
					LOGGER.info("Writing tar file " + entryName);
					int n;
					FileOutputStream fileoutputstream;
					/**
					File newFile = new File(entryName);
					String directory = newFile.getParent();
					if (directory == null) {
						if (newFile.isDirectory())
							LOGGER.info("null parent is a directory (??)");
						break;
					}*/
					LOGGER.info("Writing in "+archiveDir+entryName);
					File directoryF = new File(archiveDir + entryName);
					if (!tarentry.isDirectory()) {
						LOGGER.info(entryName + "is not a directory");
						fileoutputstream = new FileOutputStream(directoryF);
						is.copyEntryContents(fileoutputstream);
						fileoutputstream.close();
					} else {
						LOGGER.info("is directory");
						directoryF.mkdirs();
					}
					LOGGER.info("load next entry");
					tarentry = is.getNextEntry();
				} catch (Exception e) {
					LOGGER.info("exception occured");
					e.printStackTrace();
					throw new ExceptionReport("Archive writing exception",ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
			LOGGER.info("close stream");
			is.close();
			tarFile.delete();
		} else {
			while (zipentry != null) {
				String entryName = zipentry.getName();
				LOGGER.info("Writing file " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null) {
					if (newFile.isDirectory())
						break;
				}
				File directoryF = new File(archiveDir + entryName);
				if (!directoryF.getParentFile().exists()) {
					directoryF.getParentFile().mkdirs();
				}
				fileoutputstream = new FileOutputStream(archiveDir + entryName);
				while ((n = zis.read(buf, 0, 1024)) > -1)
					fileoutputstream.write(buf, 0, n);
				fileoutputstream.close();
				zis.closeEntry();
				zipentry = zis.getNextEntry();
			}
			zis.close();
		}
	}

	/**
	 * TODO
	 * 
	 * @param processID
	 * @return
	 * @throws Exception
	 */
	public boolean unDeployData(String dataID) throws Exception {
		// Prepare undeploy message
		// TODO which version should be deleted ?

		String archiveDir = getDeployDir() + dataID;
		return deleteFile(archiveDir);
	}

	public static boolean deleteFile(String sFilePath) {
		File oFile = new File(sFilePath);
		if (oFile.isDirectory()) {
			File[] aFiles = oFile.listFiles();
			for (File oFileCur : aFiles) {
				deleteFile(oFileCur.getAbsolutePath());
			}
		}
		return oFile.delete();
	}

	public Document invoke(ExecuteDocument doc, String algorithmID)
			throws Exception {

		return null;
	}

	public Collection<String> getAllDataes() throws Exception {
		LOGGER.info("should not be reached todo");
		return null;

	}

	public boolean containsData(String processID) throws Exception {
		return false;
	}

	public boolean unDeployData(UndeployDataRequest request) throws Exception {
		LOGGER.info("undeploy starting");
		// unDeployData(String processID) is implemented though...
		return unDeployData((String) request.getDataID());
		// return false;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@SuppressWarnings("unused")
	private ByteArrayOutputStream writeXMLToStream(Source source)
			throws TransformerException {
		// Prepare the DOM document for writing

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Prepare the output file

		Result result = new StreamResult(out);
		// System.etProperty("javax.xml.transform.TransformerFactory","com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		// Write the DOM document to the file
		TransformerFactory x = TransformerFactory.newInstance();
		Transformer xformer = x.newTransformer();
		xformer.transform(source, result);

		return out;
	}

	@SuppressWarnings("unused")
	private String nodeToString(Node node)
			throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(
				stringWriter));

		return stringWriter.toString();
	}

	@Override
	public Collection<String> getAllDatas() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
