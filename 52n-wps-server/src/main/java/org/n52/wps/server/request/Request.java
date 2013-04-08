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

package org.n52.wps.server.request;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;

/**
 * The client requests some operation from the server. The request generates a unique reference based on the
 * client, time and a count. Not secure! Upcoming references are easily guessed or altered.
 * 
 * @see java.rmi.server.UID
 */
abstract public class Request implements Callable<Response> {

    protected CaseInsensitiveMap map = null;
    protected Document doc = null;
    protected static Logger LOGGER = Logger.getLogger(Request.class);
    protected UUID id = null;
    public static final String SUPPORTED_VERSION = "1.0.0";

    /**
     * Create a Request based on a CaseInsensitiveMap as input (HTTP GET)
     * 
     * @param map
     *        The Map which holds the client input.
     */
    public Request(CaseInsensitiveMap map) {
        super();
        this.map = map;
    }

    /**
     * Create a Request based on a Document as input (HTTP POST)
     * 
     * @param doc
     *        The Document which holds the client input.
     */
    public Request(Document doc) {
        super();
        this.doc = doc;
 }

    /**
     * Returns the user input in Document form
     * 
     * @return Document || null if Request(Map, outputstream) was used
     */
    public Document getDocument() {
        return this.doc;
    }

    /**
     * Returns the user input in Map form
     * 
     * @return Map || null if Request(Document, OutputStream) was used
     */
    public CaseInsensitiveMap getMap() {
        return this.map;
    }

    /**
     * Retrieve a value from an input-map with a lookup-key
     * 
     * @param key
     *        The lookup-key
     * @param map
     *        The input-map to look in
     * @param required
     *        If the key-value pair must be in the map.
     * @return The value of the key-value pair
     */
    public static String getMapValue(String key, CaseInsensitiveMap map, boolean required) throws ExceptionReport {
        if (map.containsKey(key)) {
            return ((String[]) map.get(key))[0];
        }
        else if ( !required) {
            LOGGER.warn("Parameter <" + key + "> not found.");
            return null;
        }
        else {
            throw new ExceptionReport("Parameter <" + key + "> is not specified",
                                      ExceptionReport.MISSING_PARAMETER_VALUE);
        }
    }

    /**
     * Retrieve an array of values from an input-map with a lookup-key
     * 
     * @param key
     *        The lookup-key
     * @param map
     *        The input-map to look in
     * @param required
     *        If the key-value pair must be in the map.
     * @return The array of values of the key-value pair
     */
    public static String[] getMapArray(String key, CaseInsensitiveMap map, boolean required) throws ExceptionReport {
        if (map.containsKey(key)) {
            return (String[]) map.get(key);
        }
        else if ( !required) {
            LOGGER.warn("Parameter <" + key + "> not found.");
            return null;
        }
        else {
            throw new ExceptionReport("Parameter <" + key + "> is not specified",
                                      ExceptionReport.MISSING_PARAMETER_VALUE);
        }
    }

    /**
     * Retrieve a value from the client-input-map with a lookup-key
     * 
     * @param The
     *        lookup-key
     * @return The value of the key-value pair
     */
    protected String getMapValue(String key, boolean required) throws ExceptionReport {
        return Request.getMapValue(key, this.map, required);
    }

    /**
     * Retrieve an array of values from the client-input-map with a lookup-key
     * 
     * @param The
     *        lookup-key
     * @return The array of values of the key-value pair
     */
    protected String[] getMapArray(String key, boolean required) throws ExceptionReport {
        return Request.getMapArray(key, this.map, required);
    }

    /**
     * Returns the version that the client requested.
     * 
     * @return An array of versions that are compatible with the client
     */
    protected String[] getRequestedVersions(boolean mandatory) throws ExceptionReport {
        return getMapArray("version", mandatory);

    }

    /**
     * The process (request) on the server could require a specific version on the client
     * 
     * @param version
     *        The version that is required on the client
     * @return True if the required version matches, False otherwise.
     */
    public boolean requireVersion(String version, boolean mandatory) throws ExceptionReport {
        String[] versions = getRequestedVersions(mandatory);
        if (mandatory && versions == null) {
            throw new ExceptionReport("VERSION parameter required, but null", ExceptionReport.MISSING_PARAMETER_VALUE);
        }
        else if (versions == null && !mandatory) {
            return true;
        }
        for (String v : versions) {
            if (v.equals(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Accumulates the Strings in an array, separated by ", " (without quotes).
     * 
     * @param strings
     *        The array to accumulate
     * @return The accumulated String
     */
    public static String accumulateString(String[] strings) {
        StringBuffer sb = new StringBuffer();
        for (String s : strings) {
            sb.append(s + ", ");
        }
        return sb.toString();
    }

    public UUID getUniqueId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        return this.id;
    }

    abstract public Object getAttachedResult();

    /**
     * After creation a Request is handled. This is done by calling this method. This handling could contain a
     * lot of computations. These computations should be called from within this method.
     * 
     * @return A Reponse to the client Request
     * @see java.util.concurrent.Callable#call()
     */
    abstract public Response call() throws ExceptionReport;

    /**
     * There should be some validation required on the (input of the) clients Request.
     * 
     * @return True if the clients Request can be handled without problems, False otherwise
     */
    abstract public boolean validate() throws ExceptionReport;

}
