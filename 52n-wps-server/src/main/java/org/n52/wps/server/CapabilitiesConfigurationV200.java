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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import net.opengis.ows.x20.AddressType;
import net.opengis.ows.x20.CodeType;
import net.opengis.ows.x20.ContactType;
import net.opengis.ows.x20.DCPDocument.DCP;
import net.opengis.ows.x20.HTTPDocument.HTTP;
import net.opengis.ows.x20.KeywordsType;
import net.opengis.ows.x20.LanguageStringType;
import net.opengis.ows.x20.OperationDocument.Operation;
import net.opengis.ows.x20.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x20.RequestMethodType;
import net.opengis.ows.x20.ResponsiblePartySubsetType;
import net.opengis.ows.x20.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x20.ServiceProviderDocument.ServiceProvider;
import net.opengis.wps.x20.CapabilitiesDocument;
import net.opengis.wps.x20.ContentsDocument.Contents;
import net.opengis.wps.x20.ProcessDescriptionType;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x20.ProcessSummaryType;
import net.opengis.wps.x20.WPSCapabilitiesType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.iceland.w3c.W3CConstants;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Encapsulation of the WPS Capabilities document. This class has to be initialized with either a
 * {@linkplain #getInstance(java.io.File) file}, {@linkplain #getInstance(java.net.URL) URL},
 * {@linkplain #getInstance(java.lang.String) path} or
 * {@linkplain #getInstance(net.opengis.wps.x200.CapabilitiesDocument) instance}.
 * 
 * @author foerster
 * @author Christian Autermann
 * @author Benjamin Pross
 * 
 */
public class CapabilitiesConfigurationV200 {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesConfigurationV200.class);

    private static final ReentrantLock lock = new ReentrantLock();

    private static CapabilitiesDocument capabilitiesDocumentObj;

    private static CapabilitiesSkeletonLoadingStrategy loadingStrategy;
   
    @Inject
	private static WPSConfig wpsConfig;
    @Inject
    private static ConfigurationManager configurationManager;
    private static org.n52.wps.webapp.entities.ServiceIdentification serviceIdentificationConfigurationModule;	
    private static org.n52.wps.webapp.entities.ServiceProvider serviceProviderConfigurationModule;	

    private CapabilitiesConfigurationV200() {
        /* nothing here */
    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     * 
     * @param filePath
     *        the File pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(String filePath) throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(filePath));
    }

    /**
     * Gets the WPS Capabilities using the specified file to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this file to obtain the skeleton.
     * 
     * @param file
     *        the File pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(File file) throws XmlException, IOException {
        return getInstance(new FileLoadingStrategy(file));
    }

    /**
     * Gets the WPS Capabilities using the specified URL to obtain the skeleton. All future calls to
     * {@link #getInstance()} and {@link #getInstance(boolean)
     * } will use this URL to obtain the skeleton.
     * 
     * @param url
     *        the URL pointing to a skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(URL url) throws XmlException, IOException {
        return getInstance(new URLLoadingStrategy(url));
    }

    /**
     * Gets the WPS Capabilities using the specified skeleton. All future calls to {@link #getInstance()} and
     * {@link #getInstance(boolean) } will use this skeleton.
     * 
     * @param skel
     *        the skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(CapabilitiesDocument skel) throws XmlException, IOException {
        return getInstance(new InstanceStrategy(skel));
    }

    /**
     * Gets the WPS Capabilities using the specified strategy. All future calls to {@link #getInstance()} and
     * {@link #getInstance(boolean) } will use this strategy.
     * 
     * @param strategy
     *        the strategy to load the skeleton
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    private static CapabilitiesDocument getInstance(CapabilitiesSkeletonLoadingStrategy strategy) throws XmlException,
            IOException {
        Preconditions.checkNotNull(strategy);
        lock.lock();
        try {
            if (strategy.equals(loadingStrategy)) {
                return getInstance(false);
            }
            loadingStrategy = strategy;
            return getInstance(true);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Get the WPS Capabilities for this service. The capabilities are reloaded if caching is not enabled in
     * the WPS configuration.
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance() throws XmlException, IOException {
        boolean cached = wpsConfig.getServerConfigurationModule().isCacheCapabilites();
        return getInstance( !cached);
    }

    /**
     * Get the WPS Capabilities for this service and optionally force a reload.
     * 
     * @param reload
     *        if the capabilities should be reloaded
     * 
     * @return the capabilities document
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
     */
    public static CapabilitiesDocument getInstance(boolean reload) throws XmlException, IOException {
        lock.lock();
        try {
            if (capabilitiesDocumentObj == null || reload) {
            	
            	if(loadingStrategy == null){
            		loadingStrategy = new CreateInstanceStrategy();
            	}
            	
                capabilitiesDocumentObj = loadingStrategy.loadSkeleton();
                initSkeleton(capabilitiesDocumentObj);
            }
            return capabilitiesDocumentObj;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL and creating the process offerings.
     * 
     * @param skel
     *        the skeleton to enrich
     * 
     * @throws UnknownHostException
     *         if the local host name can not be obtained
     */
    private static void initSkeleton(CapabilitiesDocument skel) throws UnknownHostException {
        String endpoint = getEndpointURL();
        if (skel.getCapabilities() == null) {
            skel.addNewCapabilities();
        }
        initOperationsMetadata(skel, endpoint);
        initProcessOfferings(skel);
    }

    /**
     * Enriches the capabilities skeleton by creating the process offerings.
     * 
     * @param skel
     *        the skeleton to enrich
     */
    private static void initProcessOfferings(CapabilitiesDocument skel) {
        Contents contents = skel.getCapabilities()
                .addNewContents();
        for (String algorithmName : RepositoryManager.getInstance()
                .getAlgorithms()) {
        	try {
        		ProcessOffering offering = (ProcessOffering) RepositoryManager
                        .getInstance().getProcessDescription(algorithmName).getProcessDescriptionType(WPSConfig.VERSION_200);
        		
        		ProcessDescriptionType description = offering.getProcess();
        		
                if (description != null) {
                    ProcessSummaryType process = contents.addNewProcessSummary();
                    CodeType ct = process.addNewIdentifier();
                    ct.setStringValue(algorithmName);
                    //a title is mandatory for a process offering
                    LanguageStringType title = null;
                    try {
                        title = description.getTitleArray(0);						
					} catch (Exception e) {
						throw new RuntimeException(String.format("Process offering for process '{}' not valid. No title specified.", algorithmName));
					}
                    String processVersion = offering.getProcessVersion();
                    process.setProcessVersion(processVersion);
                    
                    process.setJobControlOptions(offering.getJobControlOptions());
                    
                    process.setOutputTransmission(offering.getOutputTransmission());

                    process.addNewTitle().setStringValue(title.getStringValue());
                    
                    LOG.trace("Added algorithm to process offerings: {}\n\t\t{}", algorithmName, process);
                }	
        	}
        	catch (RuntimeException e) {
        		LOG.warn("Exception during instantiation of process {}", algorithmName, e);
        	}
        }
    }

    /**
     * Enriches a capabilities skeleton by adding the endpoint URL to the operations meta data.
     * 
     * @param skel
     *        the skeleton to enrich
     * @param endpointUrl
     *        the endpoint URL of the service
     * 
     */
    private static void initOperationsMetadata(CapabilitiesDocument skel, String endpointUrl) {
        if (skel.getCapabilities().getOperationsMetadata() != null) {
            String endpointUrlGet = endpointUrl + "?";
            for (Operation op : skel.getCapabilities().getOperationsMetadata().getOperationArray()) {
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
     * Gets the endpoint URL of this service by checking the configuration file and the local host name.
     * 
     * @return the endpoint URL
     * 
     * @throws UnknownHostException
     *         if the local host name could not be resolved into an address
     */
    private static String getEndpointURL() throws UnknownHostException {
        return wpsConfig.getServiceEndpoint();
    }

    /**
     * Force a reload of the capabilities skeleton.
     * 
     * @throws XmlException
     *         if the Capabilities skeleton is not valid
     * @throws IOException
     *         if an IO error occurs
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
        }
        finally {
            lock.unlock();
        }
    }
    
	public static ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

    /**
     * Strategy to load a capabilities skeleton from a URL.
     */
    private static class URLLoadingStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final URL url;

        /**
         * Creates a new strategy using the specified URL.
         * 
         * @param file
         *        the file
         */
        URLLoadingStrategy(URL url) {
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public CapabilitiesDocument loadSkeleton() throws XmlException, IOException {
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
         * @param file
         *        the file
         */
        FileLoadingStrategy(File file) throws MalformedURLException {
            super(file.toURI().toURL());
        }

        /**
         * Creates a new strategy using the specified file.
         * 
         * @param file
         *        the path to the file
         */
        FileLoadingStrategy(String file) throws MalformedURLException {
            this(new File(Preconditions.checkNotNull(file)));
        }

    }

    /**
     * Strategy to obtain the capabilities skeleton from an existing instance.
     */
    private static class InstanceStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final CapabilitiesDocument instance;

        /**
         * Creates a new strategy using the specified instance.
         * 
         * @param instance
         *        the instance
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
     * Strategy to obtain the capabilities skeleton from an existing instance.
     */
    private static class CreateInstanceStrategy implements CapabilitiesSkeletonLoadingStrategy {
        private final CapabilitiesDocument instance;

        /**
         * Creates a new strategy using the specified instance.
         * 
         * @param instance
         *        the instance
         */
        CreateInstanceStrategy() {
            this.instance = CapabilitiesDocument.Factory.newInstance();
            
            serviceIdentificationConfigurationModule = getConfigurationManager().getCapabilitiesServices().getServiceIdentification();
            
            serviceProviderConfigurationModule = getConfigurationManager().getCapabilitiesServices().getServiceProvider();
            
            WPSCapabilitiesType wpsCapabilities = instance.addNewCapabilities();
            
            XmlCursor c = instance.newCursor();
            c.toFirstChild();
            c.toLastAttribute();
            c.setAttributeText(W3CConstants.QN_SCHEMA_LOCATION_PREFIXED, "http://www.opengis.net/wps/2.0 http://schemas.opengis.net/wps/2.0/wps.xsd");
            
            wpsCapabilities.addNewService().setStringValue("WPS");//Fixed to WPS TODO: put in WPSConfig or so
            
            wpsCapabilities.setVersion(WPSConfig.VERSION_200);
            
            ServiceProvider serviceProvider = wpsCapabilities.addNewServiceProvider();
            
            serviceProvider.setProviderName(serviceProviderConfigurationModule.getProviderName() != null ? serviceProviderConfigurationModule.getProviderName() : "");
            
            serviceProvider.addNewProviderSite().setHref(serviceProviderConfigurationModule.getProviderSite() != null ? serviceProviderConfigurationModule.getProviderSite() : "");
                        
            ResponsiblePartySubsetType responsiblePartySubsetType = serviceProvider.addNewServiceContact();
            
            responsiblePartySubsetType.setIndividualName(serviceProviderConfigurationModule.getIndividualName() != null ? serviceProviderConfigurationModule.getIndividualName() : "");
            
            ContactType contactType = responsiblePartySubsetType.addNewContactInfo();
            
            AddressType addressType = contactType.addNewAddress();
            
            addressType.setAdministrativeArea(serviceProviderConfigurationModule.getAdministrativeArea() != null ? serviceProviderConfigurationModule.getAdministrativeArea() : "");
            
            addressType.setCity(serviceProviderConfigurationModule.getCity() != null ? serviceProviderConfigurationModule.getCity() : "");
            
            addressType.setCountry(serviceProviderConfigurationModule.getCountry() != null ? serviceProviderConfigurationModule.getCountry() : "");
            
            addressType.setPostalCode(serviceProviderConfigurationModule.getPostalCode() != null ? serviceProviderConfigurationModule.getPostalCode() : "");
     
            addressType.addDeliveryPoint(serviceProviderConfigurationModule.getDeliveryPoint() != null ? serviceProviderConfigurationModule.getDeliveryPoint() : "");
            
            addressType.addElectronicMailAddress(serviceProviderConfigurationModule.getEmail() != null ? serviceProviderConfigurationModule.getEmail() : "");
            
            ServiceIdentification serviceIdentification = wpsCapabilities.addNewServiceIdentification();
            
            serviceIdentification.addNewTitle().setStringValue(serviceIdentificationConfigurationModule.getTitle() != null ? serviceIdentificationConfigurationModule.getTitle() : "");
            
            serviceIdentification.addAccessConstraints(serviceIdentificationConfigurationModule.getAccessConstraints() != null ? serviceIdentificationConfigurationModule.getAccessConstraints() : "");
            
            serviceIdentification.addNewAbstract().setStringValue(serviceIdentificationConfigurationModule.getServiceAbstract() != null ? serviceIdentificationConfigurationModule.getServiceAbstract() : "");
            
            serviceIdentification.addNewServiceType().setStringValue("WPS");//Fixed to WPS
            
            String[] versionArray = serviceIdentificationConfigurationModule.getServiceTypeVersions().split(";");  
            
            for (String version : versionArray) {
				if(version.trim() != ""){
					serviceIdentification.addNewServiceTypeVersion().setStringValue(version);
				}
			}
            
            serviceIdentification.setFees(serviceIdentificationConfigurationModule.getFees() != null ? serviceIdentificationConfigurationModule.getFees() : "");
            
            String[] keywordArray = serviceIdentificationConfigurationModule.getKeywords().split(";");  
            
            KeywordsType keywordsType = serviceIdentification.addNewKeywords();
            
            for (String keyword : keywordArray) {
				if(keyword.trim() != ""){
					keywordsType.addNewKeyword().setStringValue(keyword);
				}
			}
            
            OperationsMetadata operationsMetadata = wpsCapabilities.addNewOperationsMetadata();
            
            String wpsWebappPath = wpsConfig.getServiceBaseUrl();
            
            String getHREF = wpsWebappPath.endsWith("/") ? wpsWebappPath.substring(0, wpsWebappPath.length()-1) : wpsWebappPath;
            
            getHREF = getHREF.concat("?");
            
            String postHREF = wpsWebappPath;
            
            addOperation(operationsMetadata, "GetCapabilities", getHREF, postHREF);
            addOperation(operationsMetadata, "DescribeProcess", getHREF, postHREF);
            addOperation(operationsMetadata, "Execute", "", postHREF);
            addOperation(operationsMetadata, "GetStatus", getHREF, postHREF);
            addOperation(operationsMetadata, "GetResult", getHREF, postHREF);
            
            wpsCapabilities.addNewLanguages().addLanguage("en-US");
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
        
        private void addOperation(OperationsMetadata operationsMetadata, String operationName, String getHREF, String postHREF){
                 
            Operation operation = operationsMetadata.addNewOperation();
            
            operation.setName(operationName);
            
            HTTP http = operation.addNewDCP().addNewHTTP();
            
            if(getHREF != null && !getHREF.equals("")){
            	http.addNewGet().setHref(getHREF);            	            	
            }
            if(postHREF != null && !postHREF.equals("")){
            	http.addNewPost().setHref(postHREF);            	            	
            }
        	
        }
    }
    
    /**
     * Strategy to load a capabilities skeleton.
     */
    private interface CapabilitiesSkeletonLoadingStrategy {
        /**
         * Loads a CapabilitiesDocument skeleton. Every call to this method should return another instance.
         * 
         * @return the capabilities skeleton
         * 
         * @throws XmlException
         *         if the Capabilities skeleton is not valid
         * @throws IOException
         *         if an IO error occurs
         */
        CapabilitiesDocument loadSkeleton() throws XmlException, IOException;
    }
}
