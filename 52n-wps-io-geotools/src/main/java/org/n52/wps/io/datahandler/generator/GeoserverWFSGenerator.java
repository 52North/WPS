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
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class GeoserverWFSGenerator extends AbstractGeoserverWXSGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeoserverWFSGenerator.class);
	
	public GeoserverWFSGenerator() {		
		super();
		this.supportedIDataTypes.add(GTVectorDataBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
	
		InputStream stream = null;	
		try {
			Document doc = storeLayer(data);			
			String xmlString = XMLUtil.nodeToString(doc);			
			stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));			
	    } catch(TransformerException e){
	    	LOGGER.error("Error generating WFS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WFS output. Reason: " + e);
	    } catch (IOException e) {
	    	LOGGER.error("Error generating WFS output. Reason: ", e);
	    	throw new RuntimeException("Error generating WFS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
	    	LOGGER.error("Error generating WFS output. Reason: ", e);
			throw new RuntimeException("Error generating WFS output. Reason: " + e);
		}	
		return stream;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
		File file = null;
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

		
		String layerName = zipped.getName();
		layerName = layerName +"_" + UUID.randomUUID();
		GeoServerUploader geoserverUploader = new GeoServerUploader(username, password, host, port);
		
		String result = geoserverUploader.createWorkspace();		
		LOGGER.debug(result);
		result = geoserverUploader.uploadShp(zipped, layerName);		
		LOGGER.debug(result);
				
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/wfs?Service=WFS&Request=GetCapabilities&Version=1.1.0";
		//String directLink = geoserverBaseURL + "?Service=WFS&Request=GetFeature&Version=1.1.0&typeName=N52:"+file.getName().subSequence(0, file.getName().length()-4);
		
		//delete shp files
		zipped.delete();
		file.delete();
		shx.delete();
		dbf.delete();
		prj.delete();
		Document doc = createXML("N52:"+file.getName().subSequence(0, file.getName().length()-4), capabilitiesLink);
		return doc;
	
	}
	
	private Document createXML(String layerName, String getCapabilitiesLink) throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().newDocument();
		
		Element root = doc.createElement("OWSResponse");
		root.setAttribute("type", "WFS");
		
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
