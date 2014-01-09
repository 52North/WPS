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


package org.n52.wps.server.request;

import java.util.ArrayList;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.RequestHandler;
import org.n52.wps.server.response.CapabilitiesResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles a CapabilitesRequest
 */
public class CapabilitiesRequest extends Request {

    private static final String ACCEPT_VERSIONS_ELEMENT_NAME = "AcceptVersions";
    private static final String PARAM_SERVICE = "service";
    private static final String PARAM_VERSION = "version";
    private static final Object REQUEST_DOC = "document";

    /**
     * Creates a CapabilitesRequest based on a Map (HTTP_GET)
     * 
     * @param ciMap
     *        The client input
     * @throws ExceptionReport
     */
    public CapabilitiesRequest(CaseInsensitiveMap ciMap) throws ExceptionReport {
        super(ciMap);
        //Fix for https://bugzilla.52north.org/show_bug.cgi?id=907
        String providedAcceptVersionsString = Request.getMapValue("acceptversions", ciMap, false);

        if (providedAcceptVersionsString != null) {

            String[] providedAcceptVersions = providedAcceptVersionsString.split(",");

            if (providedAcceptVersions != null) {
                map.put("version", providedAcceptVersions);
            }
        }
    }

    public CapabilitiesRequest(Document doc) throws ExceptionReport {
        super(doc);
        this.map = new CaseInsensitiveMap();

        Node fc = this.doc.getFirstChild();
        String name = fc.getNodeName();
        this.map.put(REQUEST_DOC, name);

        Node serviceItem = fc.getAttributes().getNamedItem("service");
        if (serviceItem != null) {
            String service = serviceItem.getNodeValue();
            String[] serviceArray = {service};

            this.map.put(PARAM_SERVICE, serviceArray);
        }

        NodeList nList = doc.getFirstChild().getChildNodes();
        ArrayList<String> versionList = new ArrayList<String>();

        for (int i = 0; i < nList.getLength(); i++) {
            Node n = nList.item(i);
            if (n.getLocalName() != null) {
                if (n.getLocalName().equalsIgnoreCase(ACCEPT_VERSIONS_ELEMENT_NAME)) {

                    NodeList nList2 = n.getChildNodes();

                    for (int j = 0; j < nList2.getLength(); j++) {
                        Node n2 = nList2.item(j);

                        if (n2.getLocalName() != null
                                && n2.getLocalName().equalsIgnoreCase(RequestHandler.VERSION_ATTRIBUTE_NAME)) {
                            versionList.add(n2.getTextContent());
                        }
                    }
                    break;
                }
            }
        }

        if ( !versionList.isEmpty()) {
            this.map.put(PARAM_VERSION, versionList.toArray(new String[versionList.size()]));
        }

    }

    /**
     * Validates the client input
     * 
     * @throws ExceptionReport
     * @return True if the input is valid, False otherwise
     */
    public boolean validate() throws ExceptionReport {
        String services = getMapValue(PARAM_SERVICE, true);
        if ( !services.equalsIgnoreCase("wps")) {
            throw new ExceptionReport("Parameter <service> is not correct, expected: WPS , got: " + services,
                                      ExceptionReport.INVALID_PARAMETER_VALUE, "service");
        }

        String[] versions = getMapArray(PARAM_VERSION, false);
        if ( !requireVersion(SUPPORTED_VERSION, false)) {
            throw new ExceptionReport("Requested versions are not supported, you requested: "
                    + Request.accumulateString(versions), ExceptionReport.VERSION_NEGOTIATION_FAILED, "version");
        }

        return true;
    }

    /**
     * Actually serves the Request.
     * 
     * @throws ExceptionReport
     * @return Response The result of the computation
     */
    public Response call() throws ExceptionReport {
        validate();
        LOGGER.info("Handled GetCapabilitiesRequest successfully!");
        return new CapabilitiesResponse(this);
    }

    /**
     * Not used in this class. Returns null;
     */
    public Object getAttachedResult() {
        return null;
    }
}
