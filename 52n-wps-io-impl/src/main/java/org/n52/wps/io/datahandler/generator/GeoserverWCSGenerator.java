/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: 

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpException;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GeotiffBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


//TODO: compact the 3 OWS Generators into a single one

public class GeoserverWCSGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeoserverWCSGenerator.class);
	private String username;
	private String password;
	private String host;
	private String port;
	
	public GeoserverWCSGenerator() {
		
		super();
		this.supportedIDataTypes.add(GTRasterDataBinding.class);
		this.supportedIDataTypes.add(GeotiffBinding.class);
		
		properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("Geoserver_username")){
				username = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_password")){
				password = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_host")){
				host = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_port")){
				port = property.getStringValue();
			}
		}
		if(port == null){
			port = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		}
		
		for(String supportedFormat : supportedFormats){
			if(supportedFormat.equals("text/xml")){
				supportedFormats.remove(supportedFormat);
			}
		}
		
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {

		InputStream stream = null;	
		try {
			Document doc = storeLayer(data);
			String xmlString = XMLUtil.nodeToString(doc);			
			stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));			
	    } catch(TransformerException e){
	    	LOGGER.error("Error generating WCS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WCS output. Reason: " + e);
	    } catch (IOException e) {
	    	LOGGER.error("Error generating WCS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WCS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
	    	LOGGER.error("Error generating WCS output. Reason: ", e);
			throw new RuntimeException("Error generating WCS output. Reason: " + e);
		}	
		return stream;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		File file = null;
		String storeName = "";
		
		if(coll instanceof GTRasterDataBinding){
			GTRasterDataBinding gtData = (GTRasterDataBinding) coll;
			GenericFileData fileData = new GenericFileData(gtData.getPayload(), null);
			file = fileData.getBaseFile(true);			
		}
		if(coll instanceof GeotiffBinding){
			GeotiffBinding data = (GeotiffBinding) coll;
			file = (File) data.getPayload();
		}
		
		storeName = file.getName();			
	
		storeName = storeName +"_" + UUID.randomUUID();
		GeoServerUploader geoserverUploader = new GeoServerUploader(username, password, host, port);
		
		String result = geoserverUploader.createWorkspace();
		LOGGER.debug(result);
		if(coll instanceof GTRasterDataBinding){
			result = geoserverUploader.uploadGeotiff(file, storeName);
		}		
		LOGGER.debug(result);
				
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/wcs?Service=WCS&Request=GetCapabilities&Version=1.1.1";
				
		Document doc = createXML(storeName, capabilitiesLink);
		return doc;
	
	}
	
	private Document createXML(String layerName, String getCapabilitiesLink) throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().newDocument();
		
		Element root = doc.createElement("OWSResponse");
		root.setAttribute("type", "WMS");
		
		Element resourceIDElement = doc.createElement("ResourceID");
		resourceIDElement.appendChild(doc.createTextNode(layerName));
		root.appendChild(resourceIDElement);
		
		Element getCapabilitiesLinkElement = doc.createElement("GetCapabilitiesLink");
		getCapabilitiesLinkElement.appendChild(doc.createTextNode(getCapabilitiesLink));
		root.appendChild(getCapabilitiesLinkElement);
		/*
		Element directResourceLinkElement = doc.createElement("DirectResourceLink");
		directResourceLinkElement.appendChild(doc.createTextNode(getMapRequest));
		root.appendChild(directResourceLinkElement);
		*/
		doc.appendChild(root);
		
		return doc;
	}
	
}
