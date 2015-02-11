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
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.mapserver.MSMapfileBinding;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generator for saving results of a WPS Process in an UMNMapserver. Right now
 * this generator only supports publishing results over an Mapserver-WMS. As
 * input this generator right now only supports GTVectorDataBinding. As template
 * for this class served the GeoserverWMSGenerator.
 * 
 * @author Jacob Mendt
 * 
 * @TODO Support more inputs (shapefile, raster)
 * @TODO Generator for WCS and WFS
 */
public class MapserverWMSGenerator extends AbstractGenerator {

	private String mapfile;
	private String workspace;
	private String shapefileRepository;
	private String wmsUrl;
	
	private static Logger LOGGER = LoggerFactory.getLogger(MapserverWMSGenerator.class);

	/**
	 * Initialize a new MapserverWMSGenerator object. Parse the parameter
	 * Mapserver_workspace, Mapserver_mapfile, Mapserver_dataRepository and
	 * Mapserver_wmsUrl from the config.xml of the WPS.
	 */
	public MapserverWMSGenerator() {

		super();

		this.supportedIDataTypes.add(GTVectorDataBinding.class);

		for (ConfigurationEntry<?> property : properties) {
			if (property.getKey().equalsIgnoreCase("Mapserver_workspace")) {
				workspace = property.getValue().toString();
			}
			if (property.getKey().equalsIgnoreCase("Mapserver_mapfile")) {
				mapfile = property.getValue().toString();
			}
			if (property.getKey().equalsIgnoreCase("Mapserver_dataRepository")) {
				shapefileRepository = property.getValue().toString();
			}
			if (property.getKey().equalsIgnoreCase("Mapserver_wmsUrl")) {
				wmsUrl = property.getValue().toString();
			}
		}
		for (String supportedFormat : supportedFormats) {
			if (supportedFormat.equals("text/xml")) {
				supportedFormats.remove(supportedFormat);
			}
		}
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {

		InputStream stream = null;	
		try {
			Document doc = storeLayer(data);			
			String xmlString = XMLUtil.nodeToString(doc);			
			stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));			
	    } catch(TransformerException ex){
	    	LOGGER.error("Error generating MapServer WMS output. Reason: " + ex);
	    	throw new RuntimeException("Error generating MapServer WMS output. Reason: " + ex);
	    } catch (IOException e) {
	    	LOGGER.error("Error generating MapServer WMS output. Reason: " + e);
	    	throw new RuntimeException("Error generating MapServer WMS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
	    	LOGGER.error("Error generating MapServer WMS output. Reason: " + e);
			throw new RuntimeException("Error generating MapServer WMS output. Reason: " + e);
		}	
		return stream;
	}

	/**
	 * Stores the input data as an layer in the mapserver and creates an
	 * response document.
	 * 
	 * @param coll
	 *            IData has to be instanceof GTVectorDataBinding
	 * 
	 * @return Document XML response document.
	 * 
	 * @throws HttpException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private Document storeLayer(IData coll) throws HttpException, IOException,
			ParserConfigurationException {

		// tests if the mapscript.jar was loaded correctly
		try {
			//MapserverProperties.getInstance().testMapscriptLibrary();
			LOGGER.info("Mapscript is running correctly");
		} catch (Exception e){
			e.printStackTrace();
			LOGGER.warn("Mapscript isn't running correctly");
			return null;
		} 
		
		// adds the IData to the mapserver.
		String wmsLayerName = "";
		if (coll instanceof GTVectorDataBinding) {
			GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
			SimpleFeatureCollection ftColl = (SimpleFeatureCollection) gtData.getPayload();
			wmsLayerName = MSMapfileBinding.getInstance().addFeatureCollectionToMapfile(ftColl, workspace,
					mapfile, shapefileRepository);
			LOGGER.info("Layer was added to the mapfile");
			System.gc();
		}

		// creates the response document
		String capabilitiesLink = wmsUrl + "?Service=WMS&Request=GetCapabilities";
		Document doc = createXML(wmsLayerName, capabilitiesLink);
		LOGGER.info("Capabilities document was generated.");
		
		return doc;

	}

	/**
	 * Creates an response xml, which contains the layer name, the resource link
	 * and a getCapabilities request for the publishing service.
	 * 
	 * @param layerName
	 *            Name of the layer which was added to the mapserver.
	 * @param resourceLink
	 *            Link to the resource (layer) which was added to the mapserver.
	 * @param getCapabilitiesLink
	 *            GetCapabilties request to the publishing service.
	 * 
	 * @return Document XML response document.
	 * 
	 * @throws ParserConfigurationException
	 */
	private Document createXML(String layerName, String getCapabilitiesLink)
			throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().newDocument();

		Element root = doc.createElement("OWSResponse");
		root.setAttribute("type", "WMS");

		Element resourceIDElement = doc.createElement("ResourceID");
		resourceIDElement.appendChild(doc.createTextNode(layerName));
		root.appendChild(resourceIDElement);

		Element getCapabilitiesLinkElement = doc
				.createElement("GetCapabilitiesLink");
		getCapabilitiesLinkElement.appendChild(doc
				.createTextNode(getCapabilitiesLink));
		root.appendChild(getCapabilitiesLinkElement);
		/*
		 * Element directResourceLinkElement =
		 * doc.createElement("DirectResourceLink");
		 * directResourceLinkElement.appendChild
		 * (doc.createTextNode(getMapRequest));
		 * root.appendChild(directResourceLinkElement);
		 */
		doc.appendChild(root);

		return doc;
	}

}
