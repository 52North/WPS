/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.transactional.request;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.n52.wps.server.ExceptionReport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

public class UndeployProcessRequest implements ITransactionalRequest {
	private String processID;

	public UndeployProcessRequest(Document request) throws ExceptionReport {
		try {
			processID = XPathAPI.selectSingleNode(request,
					"/UnDeployProcessRequest/Process/Identifier/text()").getNodeValue().trim();
		} catch (DOMException e) {
			throw new ExceptionReport("Error. Malformed undeploy request",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (TransformerException e) {
			throw new ExceptionReport("Error. Malformed undeploy request",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}
	}

	public String getProcessID() {
		return processID;
	}
}
