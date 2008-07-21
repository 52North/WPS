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
Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany


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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.handler.RequestHandler;
/**
 * This WPS supports HTTP GET for describeProcess and getCapabilities and XML-POST for execute.
 * @author foerster
 *
 */
public class WebProcessingService extends HttpServlet {

	// Universal version identifier for a Serializable class.
	// Should be used here, because HttpServlet implements the java.io.Serializable
	private static final long serialVersionUID = 8943233273641771839L;
	public static String PROPERTY_NAME_WEBAPP_PATH = "webappPath";
	public static String BASE_DIR = null;
	public static String WEBAPP_PATH = null;
	public static String SERVLET_PATH = "WebProcessingService"; 
	public static String WPS_NAMESPACE = "http://www.opengis.net/wps/1.0.0";
	public static String DEFAULT_LANGUAGE = "en-US";
	private static Logger LOGGER = Logger.getLogger(WebProcessingService.class);
	
	/**
	 * 
	 * Returns a preconfigured OutputStream
	 * It takes care of: 
	 * - caching
	 * - content-Encoding
	 * 
	 * @param hsRequest the HttpServletRequest
	 * @param hsResponse the HttpServlerResponse
	 * @return the preconfigured OutputStream
	 * @throws IOException a task of the tomcat
	 */
	private OutputStream getConfiguredOutputStream(
			HttpServletRequest hsRequest, HttpServletResponse hsResponse) throws IOException{
		/*
		 * Forbids clients to cache the response
		 * May solve problems with proxies and bad implementations
		 */
		hsResponse.setHeader("Expires", "0");
		if (hsRequest.getProtocol().equals("HTTP/1.1")) hsResponse.setHeader("Cache-Control","no-cache");
		else if (hsRequest.getProtocol().equals("HTTP/1.0")) hsResponse.setHeader("Pragma","no-cache");

		// Enable/disable gzip compression
		if (hsRequest.getHeader("Accept-Encoding") != null && hsRequest.getHeader("Accept-Encoding").indexOf("gzip") >= 0){
			hsResponse.setHeader( "Content-Encoding", "gzip" );
			LOGGER.info("gzip-Compression for output enabled");
			return new GZIPOutputStream(hsResponse.getOutputStream() );
		} //else {
			LOGGER.info("gzip-Compression for output disabled");
			return hsResponse.getOutputStream();
		//}
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// this is important to set the lon lat support for correct CRS transformation.
		//TODO: Might be changed to an additional configuration parameter.
		System.setProperty("org.geotools.referencing.forceXY", "true");
		
		BasicConfigurator.configure();
		LOGGER.info("WebProcessingService initializing...");

		try{
			if(WPSConfig.getInstance() ==null){
				LOGGER.error("Initialization failed! Please look at the properties file!");
				return;
			}
		}catch(Exception e){
			LOGGER.error("Initialization failed! Please look at the properties file!");
			return;
		}
		LOGGER.info("Initialization of wps properties successful!");
			
		//call RepositoyManager to initialize
		RepositoryManager.getInstance();
		LOGGER.info("Algorithms initialized");
		
		
		Parser[] parsers = WPSConfig.getInstance().getRegisteredParser();
		ParserFactory.initialize(parsers);
				
		Generator[] generators = WPSConfig.getInstance().getRegisteredGenerators();
		GeneratorFactory.initialize(generators);	
			
		
		//String customWebappPath = WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_WEBAPP_PATH);
		String customWebappPath = WPSConfig.getInstance().getWPSConfig().getServer().getWebappPath();
		if(customWebappPath != null) {
			WEBAPP_PATH = customWebappPath;
		}
		else {
			WEBAPP_PATH = "wps";
			LOGGER.warn("No custom webapp path found, use default wps");
		}
		LOGGER.info("webappPath is set to: " + customWebappPath);
		
		BASE_DIR = this.getServletContext().getRealPath("");
		try {
			CapabilitiesConfiguration.getInstance(
					BASE_DIR + System.getProperty("file.separator") +
					"config" + System.getProperty("file.separator") +
					"wpsCapabilitiesSkeleton.xml");
		}
		catch(IOException e) {
			LOGGER.error("error while initializing capabilitiesConfiguration", e);
		}
		catch(XmlException e) {
			LOGGER.error("error while initializing capabilitiesConfiguration", e);
		}
		
		// Get an instance of the database for initialization of the database
		DatabaseFactory.getDatabase();
		
		LOGGER.info("WPS up and running!");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	//	OutputStream out = getConfiguredOutputStream(req, res);	
		OutputStream out = res.getOutputStream();
		try {
			RequestHandler handler = new RequestHandler((Map<String, String[]>)req.getParameterMap(), out);
			String mimeType = handler.getResponseMimeType();
			res.setContentType(mimeType);
			handler.handle();
						
			res.setStatus(HttpServletResponse.SC_OK);
		} catch(ExceptionReport e) {
			handleException(e, res, out);
		}
		out.flush();
		out.close();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		//OutputStream out = getConfiguredOutputStream(req, res);
		OutputStream out = res.getOutputStream();
		try {
			InputStream is = req.getInputStream();
//			if (req.getParameterMap().containsKey("request")){
//				is = new ByteArrayInputStream(req.getParameter("request").getBytes("UTF-8"));
//			}

//			 WORKAROUND	cut the parameter name "request" of the stream		
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
    	    StringWriter sw=new StringWriter();
    	    int k;
    	    while((k=br.read())!=-1){
    	    	sw.write(k);
    	    }
    	    LOGGER.debug(sw);
    	    String s;
    	    String reqContentType = req.getContentType();
    	    if (sw.toString().startsWith("request=")){
    	    	if(reqContentType.equalsIgnoreCase("text/plain")) {
    	    		s = sw.toString().substring(8);
    	    	}
    	    	else {
    	    		s = URLDecoder.decode(sw.toString().substring(8), "UTF-8");
    	    	}
    	    	LOGGER.debug(s);
    	    } else{
    	    	s = sw.toString();
    	    }

    	    
    	    is = new ByteArrayInputStream(s.getBytes("UTF-8"));
			if(is != null) {
				
				RequestHandler handler = new RequestHandler(is, out);
				String mimeType = handler.getResponseMimeType();
				res.setContentType(mimeType);
				handler.handle();
							
				res.setStatus(HttpServletResponse.SC_OK);
			}
			else{
				handleException(new ExceptionReport("POST Message is null", ExceptionReport.MISSING_PARAMETER_VALUE), res, out);
			}
		} 
		catch(ExceptionReport e) {
			handleException(e, res, out);
		}
		out.flush();
		out.close();
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if(SERVLET_PATH == null) {
			req.getContextPath();
		}
		super.service(req, res);
	}

	private void handleException(ExceptionReport exception, HttpServletResponse res, OutputStream os) {
		res.setContentType("text/xml");
		try {
//			exception.getExceptionDocument().save(res.getWriter());
			exception.getExceptionDocument().save(os);
			LOGGER.debug(exception.toString());
//			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch(IOException e){
			LOGGER.warn("exception occured while writing ExceptionReport to stream");
			try {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error occured, while writing OWS Exception output");
				}
			catch(IOException ex) {
				LOGGER.error("error while writing error code to client!");
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		DatabaseFactory.getDatabase().shutdown();
	}
}