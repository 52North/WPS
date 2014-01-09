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
package org.n52.wps.client;


import net.opengis.ows.x11.ExceptionReportDocument;

public class WPSClientException extends Exception {
	// Universal version identifier for a Serializable class.
	// Should be used here, because Exception implements the java.io.Serializable
	private static final long serialVersionUID = -6012433945141734834L;
	private ExceptionReportDocument doc;
	
	public WPSClientException(String message, ExceptionReportDocument doc) {
		super(message);
		this.doc = doc;
	}
	
	public WPSClientException(String message) {
		super(message);
	}
	public WPSClientException(String message, Exception e) {
		super(message);
	}
	
	public boolean isServerException() {
		return doc != null;
	}
	
	public ExceptionReportDocument getServerException() {
		return doc;
	}
}
