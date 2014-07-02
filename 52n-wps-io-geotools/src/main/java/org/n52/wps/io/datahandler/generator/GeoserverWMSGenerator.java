/**
 * ﻿Copyright (C) 2007 - ${currentYear} 52°North Initiative for Geospatial Open Source
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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
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
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GeotiffBinding;
import org.n52.wps.io.data.binding.complex.ShapefileBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class GeoserverWMSGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeoserverWMSGenerator.class);	
	private String username;
	private String password;
	private String host;
	private String port;
	
	public GeoserverWMSGenerator() {
		
		super();
		this.supportedIDataTypes.add(GTRasterDataBinding.class);
		this.supportedIDataTypes.add(ShapefileBinding.class);
		this.supportedIDataTypes.add(GeotiffBinding.class);
		this.supportedIDataTypes.add(GTVectorDataBinding.class);
		
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
	    	LOGGER.error("Error generating WMS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WMS output. Reason: " + e);
	    } catch (IOException e) {
	    	LOGGER.error("Error generating WMS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WMS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
	    	LOGGER.error("Error generating WMS output. Reason: ", e);
			throw new RuntimeException("Error generating WMS output. Reason: " + e);
		}	
		return stream;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		File file = null;
		String storeName = "";
		if(coll instanceof GTVectorDataBinding){
			GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
			
			try {
				GenericFileDataWithGT fileData = new GenericFileDataWithGT(gtData.getPayload());
				file = fileData.getBaseFile(true);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException("Error generating shp file for storage in WFS. Reason: " + e1);
			}
			
			//zip shp file
			String path = file.getAbsolutePath();
			String baseName = path.substring(0, path.length() - ".shp".length());
			File shx = new File(baseName + ".shx");
			File dbf = new File(baseName + ".dbf");
			File prj = new File(baseName + ".prj");
			File zipped =org.n52.wps.io.IOUtils.zip(file, shx, dbf, prj);

			file = zipped;
			
		}
		if(coll instanceof GTRasterDataBinding){
			GTRasterDataBinding gtData = (GTRasterDataBinding) coll;
			GenericFileDataWithGT fileData = new GenericFileDataWithGT(gtData.getPayload(), null);
			file = fileData.getBaseFile(true);
			
		}
		if(coll instanceof ShapefileBinding){
			ShapefileBinding data = (ShapefileBinding) coll;
			file = data.getZippedPayload();
			
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
		if(coll instanceof GTVectorDataBinding){
			result = geoserverUploader.uploadShp(file, storeName);			
		}
		if(coll instanceof GTRasterDataBinding){
			result = geoserverUploader.uploadGeotiff(file, storeName);
		}
		
		LOGGER.debug(result);
				
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/wms?Service=WMS&Request=GetCapabilities&Version=1.1.1";
		//String directLink = geoserverBaseURL + "?Service=WMS&Request=GetMap&Version=1.1.0&Layers=N52:"+wmsLayerName+"&WIDTH=300&HEIGHT=300";;
		
		Document doc = createXML("N52:"+storeName, capabilitiesLink);
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
