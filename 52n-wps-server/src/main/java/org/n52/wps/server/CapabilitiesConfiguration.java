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
package org.n52.wps.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.DCPDocument.DCP;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.RequestMethodType;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessOfferingsDocument.ProcessOfferings;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Encapsulation of the WPS Capabilities document. This class has to be
 * initialized with either a
 * {@linkplain #getInstance(java.io.File) file},
 * {@linkplain #getInstance(java.net.URL) URL},
 * {@linkplain #getInstance(java.lang.String) path} or
 * {@linkplain #getInstance(net.opengis.wps.x100.CapabilitiesDocument) instance}.
 *
 * @author foerster
 * @author Christian Autermann
 *
 */
public class CapabilitiesConfiguration {
    private static final Logger LOG = LoggerFactory
            .getLogger(CapabilitiesConfiguration.class);
    private static final ReentrantLock lock = new ReentrantLock();
    private static CapabilitiesDocument capabilitiesDocumentObj;
    private static CapabilitiesSkeletonLoadingStrategy loadingStrategy;
    public static String ENDPOINT_URL;
    public static String tempAmazonPublicIP = getAmazonPublicIP();

    private CapabilitiesConfiguration() {/*nothing here*/

    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the
     * skeleton.
     * All future calls to {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     *
     * @param filePath the File pointing to a skeleton
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(String filePath)
            throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(filePath));
    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the
     * skeleton.
     * All future calls to {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     *
     * @param file the File pointing to a skeleton
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(File file)
            throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(file));
    }

    /**
     * Gets the WPS Capabilities using the specified URL to obtain the skeleton.
     * All future calls to {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this URL to obtain the skeleton.
     *
     * @param url the URL pointing to a skeleton
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(URL url)
            throws XmlException, IOException {
        return getInstance(new URLLoadingStrategy(url));
    }

    /**
     * Gets the WPS Capabilities using the specified skeleton. All future calls
     * to {@link #getInstance()} and {@link #getInstance(boolean) } will use
     * this skeleton.
     *
     * @param skel the skeleton
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(CapabilitiesDocument skel)
            throws XmlException, IOException {
        return getInstance(new InstanceStrategy(skel));
    }

    /**
     * Gets the WPS Capabilities using the specified strategy. All future calls
     * to {@link #getInstance()} and {@link #getInstance(boolean) } will use
     * this strategy.
     *
     * @param strategy the strategy to load the skeleton
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    private static CapabilitiesDocument getInstance(
            CapabilitiesSkeletonLoadingStrategy strategy)
            throws XmlException, IOException {
        Preconditions.checkNotNull(strategy);
        lock.lock();
        try {
            if (strategy.equals(loadingStrategy)) {
                return getInstance(false);
            } else {
                loadingStrategy = strategy;
                return getInstance(true);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the WPS Capabilities for this service. The capabilities are reloaded
     * if caching is not enabled in the WPS configuration.
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance()
            throws XmlException, IOException {
        return getInstance(!WPSConfig.getInstance().getWPSConfig().getServer()
                .getCacheCapabilites());
    }

    /**
     * Get the WPS Capabilities for this service and optionally force a reload.
     *
     * @param reload if the capabilities should be reloaded
     *
     * @return the capabilities document
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(boolean reload)
            throws XmlException, IOException {
        lock.lock();
        try {
            if (capabilitiesDocumentObj == null || reload) {
                capabilitiesDocumentObj = loadingStrategy.loadSkeleton();
                initSkeleton(capabilitiesDocumentObj);
            }
            return capabilitiesDocumentObj;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL and creating
     * the process offerings.
     *
     * @param skel the skeleton to enrich
     *
     * @throws UnknownHostException if the local host name can not be obtained
     */
    private static void initSkeleton(CapabilitiesDocument skel)
            throws UnknownHostException {
        ENDPOINT_URL = getEndpointURL();
        if (skel.getCapabilities() == null) {
            skel.addNewCapabilities();
        }
        initOperationsMetadata(skel, ENDPOINT_URL);
        initProcessOfferings(skel);
    }

    /**
     * Enriches the capabilities skeleton by creating the process offerings.
     *
     * @param skel the skeleton to enrich
     */
    private static void initProcessOfferings(CapabilitiesDocument skel) {
        ProcessOfferings processes = skel.getCapabilities()
                .addNewProcessOfferings();
        for (String algorithmName : RepositoryManager.getInstance()
                .getAlgorithms()) {
            ProcessDescriptionType description = RepositoryManager
                    .getInstance().getProcessDescription(algorithmName);
            if (description != null) {
                ProcessBriefType process = processes.addNewProcess();
                CodeType ct = process.addNewIdentifier();
                ct.setStringValue(algorithmName);
                LanguageStringType title = description.getTitle();
                String processVersion = description.getProcessVersion();
                process.setProcessVersion(processVersion);
                process.setTitle(title);
            }
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL to the
     * operations meta data.
     *
     * @param skel the skeleton to enrich
     * @param endpointUrl the endpoint URL of the service
     *
     */
    private static void initOperationsMetadata(CapabilitiesDocument skel,
                                               String endpointUrl) {
        if (skel.getCapabilities().getOperationsMetadata() != null) {
            String endpointUrlGet = endpointUrl + "?";
            for (Operation op : skel.getCapabilities()
                    .getOperationsMetadata().getOperationArray()) {
                for (DCP dcp : op.getDCPArray()) {
                    for (RequestMethodType get : dcp.getHTTP().getGetArray()) {
                        
                        get.setHref(endpointUrlGet);
                    }
                    for (RequestMethodType post : dcp.getHTTP().getPostArray()) {
                        post.setHref(endpointUrl);
                    }
                }
            }
        }
    }

    /**
     * Gets the endpoint URL of this service by checking the configuration file
     * and the local host name.
     *
     * @return the endpoint URL
     *
     * @throws UnknownHostException if the local host name could not
     *                              be resolved into an address
     */
    private static String getEndpointURL() throws UnknownHostException {
        WPSConfig config = WPSConfig.getInstance();
        String host = config.getWPSConfig().getServer().getHostname();
        String port = config.getWPSConfig().getServer().getHostport();
        if (host == null) {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        }
        if (tempAmazonPublicIP != null && !tempAmazonPublicIP.isEmpty()) {
            host = tempAmazonPublicIP;
        }

        StringBuilder url = new StringBuilder();
        //TODO what if this service runs on HTTPS?
        url.append("http").append("://").append(host);
        url.append(':').append(port).append('/');
        if (WebProcessingService.WEBAPP_PATH != null &&
            !WebProcessingService.WEBAPP_PATH.isEmpty()) {
            url.append(WebProcessingService.WEBAPP_PATH).append('/');
        }
        url.append(WebProcessingService.SERVLET_PATH);
        return url.toString();
    }

    /**
     * Force a reload of the capabilities skeleton.
     *
     * @throws XmlException if the Capabilities skeleton is not valid
     * @throws IOException  if an IO error occurs
     */
    public static void reloadSkeleton() throws XmlException, IOException {
        getInstance(true);
    }

    /**
     * Checks if the capabilities document is loaded.
     *
     * @return if the capabilities are ready.
     */
    public static boolean ready() {
        lock.lock();
        try {
            return capabilitiesDocumentObj != null;
        } finally {
            lock.unlock();
        }
    }

    public static String getAmazonPublicIP() {
        try {
            URL sourceURL
                    = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");

            //obtain the connection
            HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL
                    .openConnection();

            // Set Timeout
            sourceConnection.setConnectTimeout(5000);
            sourceConnection.setReadTimeout(5000);

            InputStream stream = sourceConnection.getInputStream();

            BufferedReader bufferedReader
                    = new BufferedReader(new InputStreamReader(stream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            LOG
                    .info("Service running in AWS EC2. Hostname overridden with public DNS IP.");
            return stringBuilder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Strategy to load a capabilities skeleton from a URL.
     */
    private static class URLLoadingStrategy
            implements CapabilitiesSkeletonLoadingStrategy {
        private final URL url;

        /**
         * Creates a new strategy using the specified URL.
         *
         * @param file the file
         */
        URLLoadingStrategy(URL url) {
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public CapabilitiesDocument loadSkeleton()
                throws XmlException, IOException {
            XmlOptions options = new XmlOptions().setLoadStripComments();
            return CapabilitiesDocument.Factory.parse(getUrl(), options);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof URLLoadingStrategy) {
                URLLoadingStrategy that = (URLLoadingStrategy) obj;
                return this.getUrl().equals(that.getUrl());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getUrl().hashCode();
        }

        /**
         * Gets the URL of this strategy.
         *
         * @return the URL;
         */
        public URL getUrl() {
            return url;
        }
    }

    /**
     * Strategy to load a capabilities skeleton from a file.
     */
    private static class FileLoadingStrategy extends URLLoadingStrategy {

        /**
         * Creates a new strategy using the specified file.
         *
         * @param file the file
         */
        FileLoadingStrategy(File file) throws MalformedURLException {
            super(file.toURI().toURL());
        }

        /**
         * Creates a new strategy using the specified file.
         *
         * @param file the path to the file
         */
        FileLoadingStrategy(String file) throws MalformedURLException {
            this(new File(Preconditions.checkNotNull(file)));
        }

    }

    /**
     * Strategy to obtain the capabilities skeleton from an existing instance.
     */
    private static class InstanceStrategy
            implements CapabilitiesSkeletonLoadingStrategy {
        private final CapabilitiesDocument instance;

        /**
         * Creates a new strategy using the specified instance.
         *
         * @param instance the instance
         */
        InstanceStrategy(CapabilitiesDocument instance) {
            this.instance = Preconditions.checkNotNull(instance);
        }

        @Override
        public CapabilitiesDocument loadSkeleton() {
            return (CapabilitiesDocument) getInstance().copy();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InstanceStrategy) {
                InstanceStrategy that = (InstanceStrategy) obj;
                return this.getInstance().equals(that.getInstance());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getInstance().hashCode();
        }

        /**
         * Gets the instance of this strategy.
         *
         * @return the instance
         */
        public CapabilitiesDocument getInstance() {
            return instance;
        }
    }

    /**
     * Strategy to load a capabilities skeleton.
     */
    private interface CapabilitiesSkeletonLoadingStrategy {
        /**
         * Loads a CapabilitiesDocument skeleton. Every call to this method
         * should return another instance.
         *
         * @return the capabilities skeleton
         *
         * @throws XmlException if the Capabilities skeleton is not valid
         * @throws IOException  if an IO error occurs
         */
        CapabilitiesDocument loadSkeleton() throws XmlException, IOException;
    }
}
