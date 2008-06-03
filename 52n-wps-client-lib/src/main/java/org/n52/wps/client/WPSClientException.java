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
