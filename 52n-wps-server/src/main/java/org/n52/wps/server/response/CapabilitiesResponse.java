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

package org.n52.wps.server.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.CapabilitiesRequest;
import org.n52.wps.util.XMLBeansHelper;

/**
 * A Response to the Request "GetCapabilities"
 *
 */
public class CapabilitiesResponse extends Response {

	/**
	 * Serves the Request with a Response
	 * @param request The GetCapabilities request
	 */
	public CapabilitiesResponse(CapabilitiesRequest request){
		super(request);
	}
	
	/**
	 * Save this Response to an OutputStream
	 * @param os The OutputStream to save this Response to
	 * @throws ExceptionReport
	 */
	public InputStream getAsStream() throws ExceptionReport{
		try {
			return CapabilitiesConfiguration.getInstance().newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (IOException e) {
			throw new ExceptionReport("Exception occured while generating response", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (XmlException e) {
			throw new ExceptionReport("Exception occured while generating response", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
	}
}
