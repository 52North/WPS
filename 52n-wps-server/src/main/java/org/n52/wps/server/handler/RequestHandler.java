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
 Timon Ter Braak, University of Twente, the Netherlands
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

package org.n52.wps.server.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.CapabilitiesRequest;
import org.n52.wps.server.request.DescribeProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.request.RetrieveResultRequest;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class accepts client requests, determines its type and then schedules the {@link ExecuteRequest}'s for
 * execution. The request is executed for a short time, within the client will be served with an immediate
 * result. If the time runs out, the client will be served with a reference to the future result. The client
 * can come back later to retrieve the result. Uses "computation_timeout_seconds" from wps.properties
 * 
 * @author Timon ter Braak
 */
public class RequestHandler {

    private static final String GET_CAPABILITIES_ELEMENT_NAME = "GetCapabilities";

    public static final String VERSION_ATTRIBUTE_NAME = "version";

    private static final String XML_MIMETYPE = "text/xml";

    private static final String EXECUTE_ELEMENT_NAME = "Execute";

    private static final CharSequence DESCRIBE_PROCESS_ELEMENT_NAME = "DescribeProcess";

    private static final String REQUEST_NAME_GET_CAPABILITIES = "GetCapabilities";

    private static final String REQUEST_PARAM_NAME = "request";

    private static final String REQUEST_NAME_DESCRIBE_PROCESS = "DescribeProcess";

    private static final String REQUEST_NAME_EXECUTE = "Execute";

    private static final String REQUEST_NAME_RETRIEVE_RESULT = "RetrieveResult";

    /** Computation timeout in seconds */
    protected static RequestExecutor pool = new RequestExecutor();

    protected OutputStream os;

    private static Logger LOGGER = Logger.getLogger(RequestHandler.class);

    protected String responseMimeType;

    protected Request req;

    // Empty constructor due to classes which extend the RequestHandler
    protected RequestHandler() {

    }

    /**
     * Handles requests of type HTTP_GET (currently capabilities and describeProcess). A Map is used to
     * represent the client input.
     * 
     * @param params
     *        The client input
     * @param os
     *        The OutputStream to write the response to.
     * @throws ExceptionReport
     *         If the requested operation is not supported
     */
    public RequestHandler(Map<String, String[]> params, OutputStream os) throws ExceptionReport {
        this.os = os;
        // sleepingTime is 0, by default.
        /*
         * if(WPSConfiguration.getInstance().exists(PROPERTY_NAME_COMPUTATION_TIMEOUT)) { this.sleepingTime =
         * Integer.parseInt(WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_COMPUTATION_TIMEOUT)); }
         * String sleepTime =
         * WPSConfig.getInstance().getWPSConfig().getServer().getComputationTimeoutMilliSeconds();
         */

        Request req;
        CaseInsensitiveMap ciMap = new CaseInsensitiveMap(params);

        // get the request type
        String requestType = Request.getMapValue(REQUEST_PARAM_NAME, ciMap, true);
        if (requestType.equalsIgnoreCase(REQUEST_NAME_GET_CAPABILITIES)) {
            req = new CapabilitiesRequest(ciMap);
        }
        else if (requestType.equalsIgnoreCase(REQUEST_NAME_DESCRIBE_PROCESS)) {
            req = new DescribeProcessRequest(ciMap);
        }
        else if (requestType.equalsIgnoreCase(REQUEST_NAME_EXECUTE)) {
            req = new ExecuteRequest(ciMap);
        }
        else if (requestType.equalsIgnoreCase(REQUEST_NAME_RETRIEVE_RESULT)) {
            req = new RetrieveResultRequest(ciMap);
        }
        else {
            throw new ExceptionReport("The requested Operation is for HTTP GET not supported or not applicable to the specification: "
                                              + requestType,
                                      ExceptionReport.OPERATION_NOT_SUPPORTED);
        }

        this.req = req;
    }

    /**
     * Handles requests of type HTTP_POST (currently executeProcess). A Document is used to represent the
     * client input. This Document must first be parsed from an InputStream.
     * 
     * @param is
     *        The client input
     * @param os
     *        The OutputStream to write the response to.
     * @throws ExceptionReport
     */
    public RequestHandler(InputStream is, OutputStream os) throws ExceptionReport {
        String nodeName, localName, nodeURI, version = null;
        Document doc = null;
        this.os = os;

        // String sleepTime =
        // WPSConfig.getInstance().getWPSConfig().getServer().getComputationTimeoutMilliSeconds();
        // if (sleepTime == null || sleepTime.equals("")) {
        // sleepTime = "5";
        // }

        try {
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                               "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(true);

            // parse the InputStream to create a Document
            doc = fac.newDocumentBuilder().parse(is);

            // Get the first non-comment child.
            Node child = doc.getFirstChild();
            while (child.getNodeName().compareTo("#comment") == 0) {
                child = child.getNextSibling();
            }
            nodeName = child.getNodeName();
            localName = child.getLocalName();
            nodeURI = child.getNamespaceURI();
            Node versionNode = child.getAttributes().getNamedItem(VERSION_ATTRIBUTE_NAME);

            boolean isGetCapabilitiesNode = nodeName.contains(GET_CAPABILITIES_ELEMENT_NAME); // use contains
                                                                                              // to allow
                                                                                              // namespaces
            if (isGetCapabilitiesNode)
                LOGGER.debug("Got GetCapabilities request.");
            else {
                if (versionNode == null) {
                    throw new ExceptionReport("No version parameter supplied.", ExceptionReport.MISSING_PARAMETER_VALUE);
                }
                version = versionNode.getNodeValue();
            }
        }
        catch (SAXException e) {
            throw new ExceptionReport("There went something wrong with parsing the POST data: " + e.getMessage(),
                                      ExceptionReport.NO_APPLICABLE_CODE,
                                      e);
        }
        catch (IOException e) {
            throw new ExceptionReport("There went something wrong with the network connection.",
                                      ExceptionReport.NO_APPLICABLE_CODE,
                                      e);
        }
        catch (ParserConfigurationException e) {
            throw new ExceptionReport("There is a internal parser configuration error",
                                      ExceptionReport.NO_APPLICABLE_CODE,
                                      e);
        }
        catch (NullPointerException e) {
            throw new ExceptionReport("There is a internal parsing error", ExceptionReport.NO_APPLICABLE_CODE, e);
        }

        // get the request type
        if (nodeURI.equals(WebProcessingService.WPS_NAMESPACE) && localName.contains(EXECUTE_ELEMENT_NAME)) {
            if (version == null) {
                throw new ExceptionReport("version is null: ", ExceptionReport.MISSING_PARAMETER_VALUE);
            }
            if ( !version.equals(Request.SUPPORTED_VERSION)) {
                throw new ExceptionReport("version is not supported: ", ExceptionReport.INVALID_PARAMETER_VALUE);
            }

            this.req = new ExecuteRequest(doc);
            if (this.req instanceof ExecuteRequest) {
                setResponseMimeType((ExecuteRequest) this.req);
            }
            else {
                this.responseMimeType = XML_MIMETYPE;
            }
        }
        else if (nodeName.contains(GET_CAPABILITIES_ELEMENT_NAME)) {
            this.req = new CapabilitiesRequest(doc);
            this.responseMimeType = XML_MIMETYPE;
        }
        else if (nodeName.contains(DESCRIBE_PROCESS_ELEMENT_NAME)) {
            this.req = new DescribeProcessRequest(doc);
            this.responseMimeType = XML_MIMETYPE;
        }
        else if (nodeURI.equals(WebProcessingService.WPS_NAMESPACE)) {
            throw new ExceptionReport("specified namespace is not supported: " + nodeURI,
                                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }
        else {
            throw new ExceptionReport("specified operation is not supported: " + nodeName,
                                      ExceptionReport.OPERATION_NOT_SUPPORTED);
        }

    }

    /**
     * Handle a request after its type is determined. The request is scheduled for execution. If the server
     * has enough free resources, the client will be served immediately. If time runs out, the client will be
     * asked to come back later with a reference to the result.
     * 
     * @param req
     *        The request of the client.
     * @throws ExceptionReport
     */
    public void handle() throws ExceptionReport {
        Response resp = null;
        if (this.req == null) {
            throw new ExceptionReport("Internal Error: Request is null in handle()", "");
        }
        if (this.req instanceof ExecuteRequest) {
            ExecuteRequest execReq = (ExecuteRequest) this.req;

            execReq.updateStatusAccepted();

            ExceptionReport exceptionReport = null;
            try {
                if (execReq.isStoreResponse()) {
                    resp = new ExecuteResponse(execReq);
                    InputStream is = resp.getAsStream();
                    IOUtils.copy(is, this.os);
                    is.close();
                    pool.submit(execReq);
                    return;
                }
                try {
                    // retrieve status with timeout enabled
                    try {
                        resp = pool.submit(execReq).get();
                    }
                    catch (ExecutionException ee) {
                        LOGGER.warn("exception while handling ExecuteRequest.");
                        // the computation threw an error
                        // probably the client input is not valid
                        if (ee.getCause() instanceof ExceptionReport) {
                            exceptionReport = (ExceptionReport) ee.getCause();
                        }
                        else {
                            exceptionReport = new ExceptionReport("An error occurred in the computation: "
                                    + ee.getMessage(), ExceptionReport.NO_APPLICABLE_CODE);
                        }
                    }
                    catch (InterruptedException ie) {
                        LOGGER.warn("interrupted while handling ExecuteRequest.");
                        // interrupted while waiting in the queue
                        exceptionReport = new ExceptionReport("The computation in the process was interrupted.",
                                                              ExceptionReport.NO_APPLICABLE_CODE);
                    }
                }
                finally {
                    if (exceptionReport != null) {
                        LOGGER.debug("ExceptionReport not null: " + exceptionReport.getMessage());
                        // NOT SURE, if this exceptionReport is also written to the DB, if required... test
                        // please!
                        throw exceptionReport;
                    }
                    // send the result to the outputstream of the client.
                    /*
                     * if(((ExecuteRequest) req).isQuickStatus()) { resp = new ExecuteResponse(execReq); }
                     */
                    else if (resp == null) {
                        LOGGER.warn("null response handling ExecuteRequest.");
                        throw new ExceptionReport("Problem with handling threads in RequestHandler",
                                                  ExceptionReport.NO_APPLICABLE_CODE);
                    }
                    if ( !execReq.isStoreResponse()) {
                        InputStream is = resp.getAsStream();
                        IOUtils.copy(is, this.os);
                        is.close();
                        LOGGER.info("Served ExecuteRequest.");
                    }
                }
            }
            catch (RejectedExecutionException ree) {
                LOGGER.warn("exception handling ExecuteRequest.", ree);
                // server too busy?
                throw new ExceptionReport("The requested process was rejected. Maybe the server is flooded with requests.",
                                          ExceptionReport.SERVER_BUSY);
            }
            catch (Exception e) {
                LOGGER.error("exception handling ExecuteRequest.", e);
                if (e instanceof ExceptionReport) {
                    throw (ExceptionReport) e;
                }
                throw new ExceptionReport("Could not read from response stream.", ExceptionReport.NO_APPLICABLE_CODE);
            }
        }
        else {
            // for GetCapabilities and DescribeProcess:
            resp = this.req.call();
            try {
                InputStream is = resp.getAsStream();
                IOUtils.copy(is, this.os);
                is.close();
            }
            catch (IOException e) {
                throw new ExceptionReport("Could not read from response stream.", ExceptionReport.NO_APPLICABLE_CODE);
            }

        }
    }

    protected void setResponseMimeType(ExecuteRequest req) {
        if (req.isRawData()) {
            this.responseMimeType = req.getExecuteResponseBuilder().getMimeType();
        }
        else {
            this.responseMimeType = XML_MIMETYPE;
        }
    }

    public String getResponseMimeType() {
        if (this.responseMimeType == null) {
            return XML_MIMETYPE;
        }
        return this.responseMimeType.toLowerCase();
    }

}
