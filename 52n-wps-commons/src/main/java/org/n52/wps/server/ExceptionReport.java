/**
 * ﻿Copyright (C) 2006
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
package org.n52.wps.server;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;

/**
 * encapsulates a exception, which occured by service execution and which has to lead to a service Exception as
 * specified in the spec.
 * @author foerster
 *
 */
public class ExceptionReport extends Exception {
	// Universal version identifier for a Serializable class.
	// Should be used here, because HttpServlet implements the java.io.Serializable
	private static final long serialVersionUID = 5784360334341938021L;
	/*
	 * Error Codes specified by the OGC Common Document.
	 */
	public static final String OPERATION_NOT_SUPPORTED = "OperationNotSupported";
	/** Operation request does not include a parameter value, and this server did not declare a default value for that parameter */
	public static final String MISSING_PARAMETER_VALUE = "MissingParameterValue";
	/** Operation request contains an invalid parameter value */
	public static final String INVALID_PARAMETER_VALUE = "InvalidParameterValue";
	public static final String VERSION_NEGOTIATION_FAILED = "VersionNegotiationFailed";
	public static final String INVALID_UPDATE_SEQUENCE = "InvalidUpdateSequence";
	/** No other exceptionCode specified by this service and server applies to this exception */
	public static final String NO_APPLICABLE_CODE = "NoApplicableCode";
	/** The server is too busy to accept and queue the request at this time. */
	public static final String SERVER_BUSY = "ServerBusy";
	/** The file size of one of the input parameters was too large for this process to handle. */
	public static final String FILE_SIZE_EXCEEDED = "FileSizeExceeded";
	/** An error occurs during remote and distributed computation process. */
	public static final String REMOTE_COMPUTATION_ERROR = "RemoteComputationError";
	
	protected String errorKey;
	protected String locator;
	
	public ExceptionReport(String message, String errorKey) {
		super(message);
		this.errorKey = errorKey;
	}
	
	public ExceptionReport(String message, String errorKey, Throwable e) {
		super(message, e);
		this.errorKey = errorKey;
	}
	
	public ExceptionReport(String message, String errorKey, String locator) {
		this(message, errorKey);
		this.locator = locator;
	}
	
	public ExceptionReport(String message, String errorKey, String locator, Throwable e) {
		this(message,errorKey, e);
		this.locator = locator;
	}
	
	public ExceptionReportDocument getExceptionDocument() {
		// Printing serivce Exception
		ExceptionReportDocument report = ExceptionReportDocument.Factory.newInstance();
		net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport exceptionReport = report.addNewExceptionReport();
		//Fix for Bug 903 https://bugzilla.52north.org/show_bug.cgi?id=903
		exceptionReport.setVersion("1.0.0");
		ExceptionType ex = exceptionReport.addNewException();
		ex.setExceptionCode(errorKey);
		ex.addExceptionText(this.getMessage());
		// Adding additional Java exception
		ExceptionType stackTrace = exceptionReport.addNewException();
		stackTrace.addExceptionText(encodeStackTrace(this));
		stackTrace.setExceptionCode("JAVA_StackTrace");
		//	adding Rootcause
		ExceptionType stackTraceRootException = exceptionReport.addNewException();
		if (getCause() != null) {
			stackTraceRootException.addExceptionText(getCause().getMessage());
			stackTraceRootException.addExceptionText(encodeStackTrace(getCause()));
		}
		stackTraceRootException.setExceptionCode("JAVA_RootCause");
		if (locator != null) {
			ex.setLocator(locator);
		}
		return report;
	}

	private String encodeStackTrace(Throwable t) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        t.printStackTrace(p);
        w.flush();
        w.flush();
        return w.toString();
	}
}
