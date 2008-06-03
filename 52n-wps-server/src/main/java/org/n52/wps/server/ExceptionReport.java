/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


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

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server;

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
		ExceptionType ex = exceptionReport.addNewException();
		ex.setExceptionCode(errorKey);
		ex.addExceptionText(this.getMessage());
		// Adding additional Java exception
		ExceptionType stackTrace = exceptionReport.addNewException();
		stackTrace.addExceptionText(encodeStackTrace(this.getStackTrace()));
		stackTrace.setExceptionCode("JAVA_StackTrace");
		//	adding Rootcause
		ExceptionType stackTraceRootException = exceptionReport.addNewException();
		if(this.getCause() != null) {
			stackTraceRootException.addExceptionText(this.getCause().getMessage());
			stackTraceRootException.addExceptionText(encodeStackTrace(this.getCause().getStackTrace()));
		}
		stackTraceRootException.setExceptionCode("JAVA_RootCause");
		if(locator != null) {
			ex.setLocator(locator);
		}
		return report;
	}
	private String encodeStackTrace(StackTraceElement[] elems) {
		StringBuffer exceptionBuffer = new StringBuffer();
		for(StackTraceElement stackTraceElem : elems) {
			exceptionBuffer.append(stackTraceElem.getClassName() + "." + 
									stackTraceElem.getMethodName() + ":" + 
									stackTraceElem.getLineNumber());
			exceptionBuffer.append("\n");
		}
		return exceptionBuffer.toString();
	}
}
