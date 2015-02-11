/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server;

// FvK: added Property Change Listener support
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.server.handler.RequestHandler;
import org.n52.wps.util.XMLBeansHelper;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

/**
 * This WPS supports HTTP GET for describeProcess and getCapabilities and XML-POST for execute.
 *
 * @author foerster, Benjamin Pross, Daniel Nüst
 *
 */
@RequestMapping("/" + WPSConfig.SERVLET_PATH)
public class WebProcessingService implements ServletContextAware, ServletConfigAware{

    private static final String SPECIAL_XML_POST_VARIABLE = "request";

    private static final String XML_CONTENT_TYPE = "text/xml";

    private static final int MAXIMUM_REQUEST_SIZE = 128 << 20;

    private static final String CAPABILITES_SKELETON_NAME = "wpsCapabilitiesSkeleton.xml";

    private static final String PUBLIC_CONFIG_FILE_DIR = "config";

    public static String WPS_NAMESPACE_1_0_0 = "http://www.opengis.net/wps/1.0.0";
    
    public static String WPS_NAMESPACE_2_0_0 = "http://www.opengis.net/wps/2.0.0";

    public static String DEFAULT_LANGUAGE = "en-US";

    protected static Logger LOGGER = LoggerFactory.getLogger(WebProcessingService.class);

    private static String applicationBaseDir = null;
    
    private ServletContext servletContext;

	@Autowired
	private ConfigurationManager configurationManager;

    public WebProcessingService() {
        LOGGER.info("NEW {}", this);
    }

    /**
     *
     * TODO: DNU: check if this method can be re-enabled or if we can achieve this functinality any other way
     *
     * Returns a preconfigured OutputStream It takes care of: - caching - content-Encoding, zipping content
     *
     * @param hsRequest
     *        the HttpServletRequest
     * @param hsResponse
     *        the HttpServlerResponse
     * @return the preconfigured OutputStream
     * @throws IOException
     *         a task of the tomcat
     */
    private static OutputStream getConfiguredOutputStream(HttpServletRequest hsRequest, HttpServletResponse hsResponse) throws IOException {
        /*
         * Forbids clients to cache the response May solve problems with proxies and bad implementations
         */
        hsResponse.setHeader("Expires", "0");
        if (hsRequest.getProtocol().equals("HTTP/1.1")) {
            hsResponse.setHeader("Cache-Control", "no-cache");
        } else if (hsRequest.getProtocol().equals("HTTP/1.0")) {
            hsResponse.setHeader("Pragma", "no-cache");
        }

        // Enable/disable gzip compression
        if (hsRequest.getHeader("Accept-Encoding") != null
                && hsRequest.getHeader("Accept-Encoding").indexOf("gzip") >= 0) {
            hsResponse.setHeader("Content-Encoding", "gzip");
            LOGGER.info("gzip-Compression for output enabled");
            return new GZIPOutputStream(hsResponse.getOutputStream());
        } // else {
        LOGGER.info("gzip-Compression for output disabled");
        return hsResponse.getOutputStream();
        // }
    }

    public void init() {
        LOGGER.info("*** WebProcessingService initializing... ***");
        WPSConfig conf = WPSConfig.getInstance(servletContext);
        
        WPSConfig.getInstance().setConfigurationManager(configurationManager);
        
        // this is important to set the lon lat support for correct CRS transformation.
        // TODO: Might be changed to an additional configuration parameter.
        System.setProperty("org.geotools.referencing.forceXY", "true");

        LOGGER.info("WebProcessingService initializing...");

        try {
            if (conf == null) {
                LOGGER.error("Initialization failed! Please look at the properties file!");
                return;
            }
        }
        catch (RuntimeException e) {
            LOGGER.error("Initialization failed! Please look at the properties file!", e);
            return;
        }
        LOGGER.info("Initialization of wps properties successful!\n\t\tWPSConfig: {}", conf);

        applicationBaseDir = servletContext.getRealPath("");
        LOGGER.debug("Application base dir is {}", applicationBaseDir);

		Map<String, ConfigurationModule> parserMap = WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getActiveConfigurationModulesByCategory(ConfigurationCategory.PARSER);
        ParserFactory.initialize(parserMap);
        LOGGER.info("Initialized {}", ParserFactory.getInstance());

		Map<String, ConfigurationModule> generatorMap = WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getActiveConfigurationModulesByCategory(ConfigurationCategory.GENERATOR);
        GeneratorFactory.initialize(generatorMap);
        LOGGER.info("Initialized {}", GeneratorFactory.getInstance());

        RepositoryManager repoManager = RepositoryManager.getInstance();
        LOGGER.info("Initialized {}", repoManager);

        IDatabase database = DatabaseFactory.getDatabase();
        LOGGER.info("Initialized {}", database);

        try {
            String capsConfigPath = getApplicationBaseDir() + File.separator + PUBLIC_CONFIG_FILE_DIR
                    + File.separator + CAPABILITES_SKELETON_NAME;
            CapabilitiesDocument capsDoc = CapabilitiesConfiguration.getInstance(capsConfigPath);
            LOGGER.debug("Initialized capabilities document:\n{}", capsDoc);
        }
        catch (IOException | XmlException e) {
            LOGGER.error("error while initializing capabilitiesConfiguration", e);
        }

        LOGGER.info("Service base url is {} | Service endpoint is {} | Used config file is {}",
                    conf.getServiceBaseUrl(),
                    conf.getServiceEndpoint(),
                    WPSConfig.getConfigPath());

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        // it will listen to changes of the wpsCapabilities
        WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME,
                                                          new PropertyChangeListener() {
                                                              @Override
                                                              public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                                                                  LOGGER.info(this.getClass().getName()
                                                                          + ": Received Property Change Event: "
                                                                          + propertyChangeEvent.getPropertyName());
                                                                  try {
                                                                      CapabilitiesConfiguration.reloadSkeleton();
                                                                  }
                                                                  catch (IOException e) {
                                                                      LOGGER.error("error while initializing capabilitiesConfiguration",
                                                                                   e);
                                                                  }
                                                                  catch (XmlException e) {
                                                                      LOGGER.error("error while initializing capabilitiesConfiguration",
                                                                                   e);
                                                                  }
                                                              }
                                                          });

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        // it will listen to changes of the wpsConfiguration
        WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME,
                                                          new PropertyChangeListener() {
                                                              public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                                                                  LOGGER.info(this.getClass().getName()
                                                                          + ": Received Property Change Event: "
                                                                          + propertyChangeEvent.getPropertyName());
                                                                  try {
                                                                      CapabilitiesConfiguration.reloadSkeleton();
                                                                  }
                                                                  catch (IOException e) {
                                                                      LOGGER.error("error while initializing capabilitiesConfiguration",
                                                                                   e);
                                                                  }
                                                                  catch (XmlException e) {
                                                                      LOGGER.error("error while initializing capabilitiesConfiguration",
                                                                                   e);
                                                                  }
                                                              }
                                                          });
        LOGGER.info("*** WPS up and running! ***");
    }

    public static String getApplicationBaseDir() {
        return applicationBaseDir;
    }

    @RequestMapping(method = RequestMethod.GET)
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String requestedVersion = null;
        try {
            OutputStream out = res.getOutputStream(); // closed by res.flushBuffer();
            RequestHandler handler = new RequestHandler(req.getParameterMap(), out);
            String mimeType = handler.getResponseMimeType();
            requestedVersion = handler.getRequestedVersion();         
            res.setContentType(mimeType);
            handler.handle();

            res.setStatus(HttpServletResponse.SC_OK);
        }
        catch (ExceptionReport e) {
            handleException(e, res, requestedVersion);
        }
        catch (RuntimeException e) {
            ExceptionReport er = new ExceptionReport("Error handing request: " + e.getMessage(),
                                                     ExceptionReport.NO_APPLICABLE_CODE,
                                                     e);
            handleException(er, res, requestedVersion);
        }
        finally {
            if (res != null) {
                res.flushBuffer();
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        BufferedReader reader = null;

        String requestedVersion = null;
        
        try {
            String contentType = req.getContentType();
            String characterEncoding = req.getCharacterEncoding();
            if (characterEncoding == null || characterEncoding.length() == 0) {
                characterEncoding = "UTF-8"; // default character encoding if unspecified
            }

            int contentLength = req.getContentLength();
            if (contentLength > MAXIMUM_REQUEST_SIZE) {
                LOGGER.warn("POST request rejected, request size of " + contentLength + " too large.");
                ExceptionReport er = new ExceptionReport("Request body too large, limited to " + MAXIMUM_REQUEST_SIZE
                        + " bytes", ExceptionReport.NO_APPLICABLE_CODE);
                handleException(er, res, "2.0.0");
            }

            LOGGER.debug("Received POST: Content-Type = " + contentType + ", Character-Encoding = " + characterEncoding
                    + ", Content-Length = " + contentLength);

            int requestSize = 0;

            StringWriter writer = contentLength > 0 ? new StringWriter(contentLength) : new StringWriter();
            reader = req.getReader();
            char[] buffer = new char[8192];
            int read;
            while ( (read = reader.read(buffer)) != -1 && requestSize < MAXIMUM_REQUEST_SIZE) {
                writer.write(buffer, 0, read);
                requestSize += read;
            }

            LOGGER.debug("POST request contained  " + requestSize + " characters");

            // Protect against denial of service attacks.
            if (requestSize >= MAXIMUM_REQUEST_SIZE && reader.read() > -1) {
                LOGGER.warn("POST request rejected, request size of " + requestSize + " too large.");
                ExceptionReport er = new ExceptionReport("Request body too large, limited to " + MAXIMUM_REQUEST_SIZE
                        + " bytes", ExceptionReport.NO_APPLICABLE_CODE);
                handleException(er, res, "2.0.0");
            }

            String documentString = writer.toString();

            // Perform URL decoding, if necessary
            // if ("application/x-www-form-urlencoded".equals(contentType)) {
            if ( (contentType).startsWith("application/x-www-form-urlencoded")) {
                if (documentString.startsWith(SPECIAL_XML_POST_VARIABLE + "=")) {
                    // This is a hack to permit xml to be easily submitted via a form POST.
                    // By convention, we are allowing users to post xml if they name it
                    // with a POST parameter "request" although this is not
                    // valid per the specification.
                    documentString = documentString.substring(SPECIAL_XML_POST_VARIABLE.length() + 1);
                    LOGGER.debug("POST request form variable removed");
                }
                documentString = URLDecoder.decode(documentString, characterEncoding);
                LOGGER.debug("Decoded of POST:\n" + documentString + "\n");
            }

            RequestHandler handler = new RequestHandler(new ByteArrayInputStream(documentString.getBytes("UTF-8")),
                                                        res.getOutputStream());
            requestedVersion = handler.getRequestedVersion();
            String mimeType = handler.getResponseMimeType();
            res.setContentType(mimeType);

            handler.handle();

            res.setStatus(HttpServletResponse.SC_OK);
        }
        catch (ExceptionReport e) {
            handleException(e, res, requestedVersion);
        }
        catch (Exception e) {
            ExceptionReport er = new ExceptionReport("Error handing request: " + e.getMessage(), ExceptionReport.NO_APPLICABLE_CODE, e);
            handleException(er, res, requestedVersion);
        }
        finally {
            if (res != null) {
                res.flushBuffer();
            }

            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void handleException(ExceptionReport exception, HttpServletResponse res, String version) {
        res.setContentType(XML_CONTENT_TYPE);
        try {
            LOGGER.debug(exception.toString());
            // DO NOT MIX getWriter and getOuputStream!
            exception.getExceptionDocument(version).save(res.getOutputStream(),
                                                  XMLBeansHelper.getXmlOptions());

            res.setStatus(exception.getHTTPStatusCode());
        }
        catch (IOException e) {
            LOGGER.warn("exception occured while writing ExceptionReport to stream");
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              "error occured, while writing OWS Exception output");
            }
            catch (IOException ex) {
                LOGGER.error("error while writing error code to client!");
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        LOGGER.debug("Finalizing {}", this);
        super.finalize();
        DatabaseFactory.getDatabase().shutdown();
    }

	@Override
	public void setServletConfig(ServletConfig servletConfig) {}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;		
	}
}