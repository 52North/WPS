/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
