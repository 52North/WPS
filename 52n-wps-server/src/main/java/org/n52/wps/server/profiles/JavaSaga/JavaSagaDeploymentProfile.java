/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.profiles.JavaSaga;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import net.opengis.wps.x100.ApacheOdeDeploymentProfileType;
import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.SagaDeploymentProfileType;
import net.opengis.wps.x100.SagaDeploymentProfileType.JsdlTemplate;
import net.opengis.wps.x100.impl.ApacheOdeDeploymentProfileTypeImpl;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.DefaultTransactionalProcessRepository;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.ogf.saga.job.Job;
import org.w3c.dom.Node;

import sun.net.ftp.FtpClient;

/** 
 * TODO rename ApacheOdeDeployementProfile to match to the XSD element type
 *  **/
public class JavaSagaDeploymentProfile extends DeploymentProfile {

	private static Logger LOGGER = Logger.getLogger(JavaSagaDeploymentProfile.class);
	private Node suitCase;
	private Node bpel;
	private Node clientWSDL;
	private Map<Integer, Node> wsdlList;
	private byte[] archive;
	private String processId;
	private boolean reference;
	private String archiveRef;
	private JsdlTemplate jsdlTemplate;

	public JavaSagaDeploymentProfile(DeployProcessDocument deployDom,
			String processID) {
		super(deployDom, processID);
		LOGGER.info("Java Saga Deployement Profile creating instance");
		try {

			extractInformation(deployDom);
		} catch (Exception e) {
			LOGGER.info(e.getStackTrace().toString());
			e.printStackTrace();
		}

	}

	public Node getSuitCase() {
		return suitCase;
	}

	public Node getBPEL() {
		return bpel;
	}

	public Node getClientWSDL() {
		return clientWSDL;
	}

	public Map<Integer, Node> getWSDLList() {
		return wsdlList;
	}

	private void extractInformation(DeployProcessDocument deployDom)
			throws Exception {
		LOGGER.info("extract information from saga profile");
		SagaDeploymentProfileType deployProfile = (SagaDeploymentProfileType) deployDom.getDeployProcess().getDeploymentProfile().changeType(SagaDeploymentProfileType.type);
		if(!deployProfile.validate()) {
			throw new ExceptionReport("Saga Deploy Profile is not valid (according WPS schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		LOGGER.info("deployProfile doc:"+deployProfile.toString());
		setProcessId(deployDom.getDeployProcess().getProcessDescription()
				.getIdentifier().getStringValue());
		if (deployProfile.isSetArchive()) {
			// Note that XMLBeans automatically decodes base64
			setArchive(deployProfile.getArchive());
			setReference(false);
		} else if (deployProfile.isSetArchiveReference()) {
			LOGGER.info("archive reference is set");
			setArchiveRef(deployProfile.getArchiveReference().getHref());
			LOGGER.info("downloading archive");
			setArchive(downloadArchive(getArchiveRef()));
			LOGGER.info("downloaded");
			setReference(true);
		}
		//LOGGER.info(deployProfile.getJsdlTemplate());
		setJsdlTemplate(deployProfile.getJsdlTemplate());
	}

	private byte[] downloadHTTP(String url) throws Exception {
		URL u = new URL(url);
		URLConnection uc = u.openConnection();
	    String contentType = uc.getContentType();
	    int contentLength = uc.getContentLength();
	    InputStream raw = uc.getInputStream();
	    InputStream in = new BufferedInputStream(raw);
	    byte[] data = new byte[contentLength];
	    int bytesRead = 0;
	    int offset = 0;
	    LOGGER.info("loading started...");
	    while (offset < contentLength) {
	    	System.out.print(".");
	      bytesRead = in.read(data, offset, data.length - offset);
	      if (bytesRead == -1)
	        break;
	      offset += bytesRead;
	    }
	    in.close();
	    if (offset != contentLength) {
	      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
	    }
	   return data;
	}
	
	/**
	 * This method download a binary file located at the given URL and returns the byte array
	 * @param archiveRef2
	 * @return
	 * @throws IOException
	 */
	private byte[] downloadArchive(String url) throws IOException {
		LOGGER.info("Downloading url "+url); 
		URL u = new URL(url);
		if (StringUtils.startsWithIgnoreCase(url, "http://")
				|| StringUtils
						.startsWithIgnoreCase(url, "https://")) {
			LOGGER.info("HTTP protocol");
			byte[] data=null;
			try {
				data = downloadHTTP(url);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
				LOGGER.info(e.getStackTrace().toString());
				e.printStackTrace();
			}
			return data;
		} else if (StringUtils.startsWithIgnoreCase(url, "ftp://")) {
			LOGGER.info("FTP protocol");
			byte[] data=null;
			try {
				data = downloadFTP(url);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
				LOGGER.info(e.getStackTrace().toString());
				e.printStackTrace();
			}
			return data;
					}
		return null;
	}

	private byte[] downloadFTP(String url) throws Exception {
		URL u = new URL(url);
		FTPClient client = new FTPClient();
	    ByteArrayOutputStream fos = null;
	    client.connect(u.getHost(), u.getPort());
	    // hardcoded
	    client.login("ftpuser", "ssegrid");
	    int reply = client.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			client.disconnect();
		}
	    String filename = u.getFile();
	    fos = new ByteArrayOutputStream();
	    client.retrieveFile(filename, fos);
	    byte[] data = fos.toByteArray();
	    fos.close();
	    client.disconnect();
		return data;
	}

	public void setArchive(byte[] archive) {
		LOGGER.info("setArchive");
		this.archive = archive;
	}

	public byte[] getArchive() {
		return archive;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setReference(boolean reference) {
		this.reference = reference;
	}

	public boolean isReference() {
		return reference;
	}

	public void setArchiveRef(String string) {
		this.archiveRef = string;
	}

	public String getArchiveRef() {
		return archiveRef;
	}

	public void setJsdlTemplate(JsdlTemplate jsdlTemplate) {
		this.jsdlTemplate = jsdlTemplate;
	}

	public JsdlTemplate getJsdlTemplate() {
		return jsdlTemplate;
	}

}
