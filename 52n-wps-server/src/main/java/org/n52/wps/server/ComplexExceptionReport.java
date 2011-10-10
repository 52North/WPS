package org.n52.wps.server;

import org.w3c.dom.Document;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;

/**
 * In some case the ExceptionReport document is more complex and built from the throwing
 * method.
 * @Christophe Noel
 *
 */
public class ComplexExceptionReport extends ExceptionReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1094119849288321576L;
	protected ExceptionReportDocument exceptionReportDom;
	public ComplexExceptionReport(String message, ExceptionReportDocument exceptionReportDom, String errorKey) {
		super(message,errorKey);
		this.exceptionReportDom = exceptionReportDom;
	}
	
	
	
	public ExceptionReportDocument getExceptionDocument() {
		return this.exceptionReportDom;
	}
	
}
