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

package org.n52.wps.server.request.deploy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.DeployDataDocument;
import net.opengis.wps.x100.DeployProcessDocument;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.profiles.oozie.OozieDeploymentProfile;
import org.n52.wps.server.repository.TransactionalRepositoryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class DeploymentProfile {

	private static Logger LOGGER = Logger
			.getLogger(OozieDeploymentProfile.class);

	private Object payload;
	private String processID;

	public DeploymentProfile(DeployProcessDocument deployDom, String processID) {
		this.processID = processID;
		this.payload = payload;

	}

	public DeploymentProfile(DeployDataDocument deployDom, String processID) {
		this.processID = processID;
		this.payload = payload;

	}

	public String getProcessID() {
		return processID;
	}

	public Object getPayload() {
		return payload;
	}

	protected byte[] downloadHTTP(String url) throws Exception {
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
			throw new IOException("Only read " + offset + " bytes; Expected "
					+ contentLength + " bytes");
		}
		return data;
	}

	/**
	 * This method download a binary file located at the given URL and returns
	 * the byte array TODO move to an util class
	 * 
	 * @param archiveRef2
	 * @return
	 * @throws IOException
	 */
	protected byte[] downloadArchive(String url) throws IOException {
		LOGGER.info("Downloading url " + url);
		URL u = new URL(url);
		if (StringUtils.startsWithIgnoreCase(url, "http://")
				|| StringUtils.startsWithIgnoreCase(url, "https://")) {
			LOGGER.info("HTTP protocol");
			byte[] data = null;
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
			byte[] data = null;
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
		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForServer();
		Property ftpUserProp = WPSConfig.getInstance().getPropertyForKey(
				properties, "portalFTPUser");
		Property ftpPassProp = WPSConfig.getInstance().getPropertyForKey(
				properties, "portalFTPPassword");
		client.login(ftpUserProp.getStringValue(), ftpPassProp.getStringValue());
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

}
