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
package org.n52.wps.io.datahandler.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author foerster
 *
 */
public class GML2BasicParser extends AbstractXMLParser implements IStreamableParser {
	private static Logger LOGGER = Logger.getLogger(GML2BasicParser.class);
		
	public GML2BasicParser() {
	 super();	
	}
	
	public GML2BasicParser(boolean pReadWPSConfig) {
		super(pReadWPSConfig);
		
	}

	private GTVectorDataBinding parseXML(File file) {
		 //setup the encoder with gml2 configuration
		GTVectorDataBinding data = null;
		try {
			data = parseXML((InputStream)new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return data;		
	}

	public GTVectorDataBinding parseXML(String gml) {
		File f = null;
		FileOutputStream fos = null;
		try{
			f = File.createTempFile("wps", "tmp");
			fos = new FileOutputStream(f);
			if(gml.startsWith("<xml-fragment")) {
				gml = gml.replaceFirst("<xml-fragment .*?>", "");
				gml = gml.replaceFirst("</xml-fragment>", "");	
			}
			// TODO find a better solution. XML-beans hands in inappropriate XML, so the namespaces have to be set manually.
			if (gml.indexOf("xmlns:xsi=") < 0)
			{
				gml = gml.replaceFirst("<wfs:FeatureCollection", "<wfs:FeatureCollection xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"");
			}
			StringReader sr = new StringReader(gml);
			int i = sr.read();
			while(i != -1){
				fos.write(i);
				i = sr.read();
			}
			fos.close();
			GTVectorDataBinding data = parseXML(f);
			f.delete();
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			if (f != null) f.delete();
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}
	
	public GTVectorDataBinding parseXML(InputStream stream) {
        Configuration configuration = new GMLConfiguration();		
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);
		
		//parse
		FeatureCollection fc = DefaultFeatureCollections.newCollection();
		try {
			Object parsedData =  parser.parse(stream);
			if(parsedData instanceof FeatureCollection){
				fc = (FeatureCollection) parsedData;
			}else{
				List<SimpleFeature> featureList = ((ArrayList<SimpleFeature>)((HashMap) parsedData).get("featureMember"));
				for(SimpleFeature feature : featureList){
					fc.add(feature);
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		
		GTVectorDataBinding data = new GTVectorDataBinding(fc);
		
		return data;
	}	

	
	
	public GTVectorDataBinding parseXML(URI uri) {
		try{
			URL url = uri.toURL();
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			InputStream stream = connection.getInputStream();
			GTVectorDataBinding data = parseXML(stream);
			return data;
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}
	

	public GTVectorDataBinding parse(InputStream input, String mimeType) {
		return parseXML(input);
	}

	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {GTVectorDataBinding.class};
		return supportedClasses;
	
	}

	


}
