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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

import net.opengis.ows.x11.CodeType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessOfferingsDocument.ProcessOfferings;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;


/**
 * This holds a copy of the capabilities skelton stored in the configuration directory.
 * @author foerster
 *
 */
public class CapabilitiesConfiguration {
	private static CapabilitiesDocument capabilitiesDocumentObj;
	private static String filePath;
	public static String ENDPOINT_URL;
	
	
	private CapabilitiesConfiguration() {/*nothing here*/}
	
	public static CapabilitiesDocument getInstance() throws XmlException, IOException {
		if(WPSConfig.getInstance().getWPSConfig().getServer().getCacheCapabilites()){
			if(capabilitiesDocumentObj == null) {
				CapabilitiesConfiguration.initSkeleton();
			}
		}else{
			CapabilitiesConfiguration.initSkeleton();
		}
		return capabilitiesDocumentObj;
	}
	/**
	 * generates an instance of the capabilitiesSkeleton using the passed parameter as a path to the
	 * capabilitiesconfig file. This has to be called first, oin order to get the class working correctly
	 */
	public static CapabilitiesDocument getInstance(String filePath) throws XmlException, IOException {
		CapabilitiesConfiguration.filePath = filePath;
		return getInstance();
	}
	
	private static void initSkeleton() throws XmlException, IOException {
		XmlOptions options = new XmlOptions();
		options.setLoadStripComments();
		CapabilitiesDocument capsSkeleton = CapabilitiesDocument.Factory.parse(new File(CapabilitiesConfiguration.filePath), options);
		//String host = WPSConfiguration.getInstance().getProperty(WebProcessingService.PROPERTY_NAME_HOST_NAME);
		String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
		String hostPort = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		if(host == null) {
			host = InetAddress.getLocalHost().getCanonicalHostName();
		}
		String tempAmazonPublicIP = getAmazonPublicIP();
		if(tempAmazonPublicIP!=null&&tempAmazonPublicIP.length()>0){
			host = tempAmazonPublicIP;
		}
		
		OperationsMetadata opMetadata = capsSkeleton.getCapabilities().getOperationsMetadata();
		ENDPOINT_URL = "http://" + host + ":" + hostPort+ "/" + 
							//WPSConfiguration.getInstance().getProperty(WebProcessingService.PROPERTY_NAME_HOST_PORT) + "/" + 
							WebProcessingService.WEBAPP_PATH + "/" + 
							WebProcessingService.SERVLET_PATH;
		for(Operation op : opMetadata.getOperationArray()) {
			if (op.getDCPArray(0).getHTTP().getGetArray().length != 0) {
				op.getDCPArray(0).getHTTP().getGetArray(0).setHref(ENDPOINT_URL);
			}
			if (op.getDCPArray(0).getHTTP().getPostArray().length != 0) {
				op.getDCPArray(0).getHTTP().getPostArray(0).setHref(ENDPOINT_URL);
			}
		}
		ProcessOfferings processes = capsSkeleton.getCapabilities().addNewProcessOfferings();
		for(String algorithmName : RepositoryManager.getInstance().getAlgorithms()) {
			IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(algorithmName);
			if(algorithm == null){
				continue;
			}
			ProcessDescriptionType description = algorithm.getDescription();
			if(description==null){
				continue;
			}
			ProcessBriefType process = processes.addNewProcess();
			CodeType ct = process.addNewIdentifier();
			ct.setStringValue(algorithmName);
			LanguageStringType title = description.getTitle();
			String processVersion = algorithm.getDescription().getProcessVersion();
			process.setProcessVersion(processVersion);
			process.setTitle(title);
		}
		capabilitiesDocumentObj = capsSkeleton;
	}
	
	public static void reloadSkeleton() throws XmlException, IOException {
		initSkeleton();
	}
	
	/**
	 * allows to check if the capabilitiesSkeleton is available for use.
	 * @return
	 */
	public static boolean ready() {
		return capabilitiesDocumentObj != null;
	}
	
	public static String getAmazonPublicIP(){
		try {
			URL sourceURL = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
		
			//obtain the connection
			HttpURLConnection sourceConnection = (HttpURLConnection) sourceURL.openConnection();
			
			// Set Timeout
			sourceConnection.setConnectTimeout(5000);
			sourceConnection.setReadTimeout(5000);
			
			InputStream stream = sourceConnection.getInputStream();
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
	
			bufferedReader.close();
	
			return stringBuilder.toString();
		} catch (Exception e) {
			
			e.printStackTrace();
			return "";
		}
	}
}
