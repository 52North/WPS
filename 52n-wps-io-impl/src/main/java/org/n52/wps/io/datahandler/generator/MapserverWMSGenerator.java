/**
 * Copyright (C) 2013
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
 * 
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
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.mapserver.MSMapfileBinding;
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

		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForGeneratorClass(this.getClass().getName());

		for (Property property : properties) {
			if (property.getName().equalsIgnoreCase("Mapserver_workspace")) {
				workspace = property.getStringValue();
			}
			if (property.getName().equalsIgnoreCase("Mapserver_mapfile")) {
				mapfile = property.getStringValue();
			}
			if (property.getName().equalsIgnoreCase("Mapserver_dataRepository")) {
				shapefileRepository = property.getStringValue();
			}
			if (property.getName().equalsIgnoreCase("Mapserver_wmsUrl")) {
				wmsUrl = property.getStringValue();
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
