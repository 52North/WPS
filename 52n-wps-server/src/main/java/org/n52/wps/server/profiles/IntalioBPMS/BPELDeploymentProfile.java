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

package org.n52.wps.server.profiles.IntalioBPMS;

import java.util.Map;

import net.opengis.wps.x100.ApacheOdeDeploymentProfileType;
import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.impl.ApacheOdeDeploymentProfileTypeImpl;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.DefaultTransactionalProcessRepository;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.ogf.saga.job.Job;
import org.w3c.dom.Node;

/** 
 * TODO rename ApacheOdeDeployementProfile to match to the XSD element type
 *  **/
public class BPELDeploymentProfile extends DeploymentProfile {

	private static Logger LOGGER = Logger.getLogger(BPELDeploymentProfile.class);
	private Node suitCase;
	private Node bpel;
	private Node clientWSDL;
	private Map<Integer, Node> wsdlList;
	private byte[] archive;
	private String processId;
	private boolean reference;
	private String archiveRef;

	public BPELDeploymentProfile(DeployProcessDocument deployDom,
			String processID) {
		super(deployDom, processID);
		try {

			extractInformation(deployDom);
		} catch (Exception e) {

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
		
		ApacheOdeDeploymentProfileType deployProfile = (ApacheOdeDeploymentProfileType) deployDom.getDeployProcess().getDeploymentProfile().changeType(ApacheOdeDeploymentProfileType.type);
		setProcessId(deployDom.getDeployProcess().getProcessDescription()
				.getIdentifier().getStringValue());
		if (deployProfile.isSetArchive()) {
			// Note that XMLBeans automatically decodes base64
			setArchive(deployProfile.getArchive());
			setReference(false);
		} else if (deployProfile.isSetArchiveReference()) {
			setArchiveRef(deployProfile.getArchiveReference().getHref());
			setReference(true);
			throw new ExceptionReport("Archive Reference not supported yet",
					org.n52.wps.server.ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
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

}
