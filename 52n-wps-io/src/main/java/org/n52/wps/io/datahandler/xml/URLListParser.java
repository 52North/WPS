/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	 Bastian Sch�ffer, IfGI

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;



import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.n52.wps.commons.context.ExecutionContext;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.URLListDataBinding;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xint.esa.ese.wps.format.urlList.URLListDocument;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class URLListParser extends AbstractXMLParser implements IStreamableParser {
	private static Logger LOGGER = Logger.getLogger(URLListParser.class);

	
	public URLListParser() {
		super();
		LOGGER.info("URLListParser");
		
	}
	
	public URLListParser(boolean pReadWPSConfig) {
		super(pReadWPSConfig);
		LOGGER.info("URLListParser2");
	}

	
		

	@Override
	public URLListDataBinding parseXML(String urllist) {
		LOGGER.info("URLListParser");
		LOGGER.info(urllist);
		URLListDocument urlDom = null;
		try {
			urlDom = URLListDocument.Factory.parse(urllist);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new URLListDataBinding(urlDom);
		}
	
	@Override
	public URLListDataBinding parseXML(InputStream stream) {
		URLListDocument urlDom = null;
		try {
			urlDom = URLListDocument.Factory.parse(stream);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new URLListDataBinding(urlDom);
		}

	@Override
	public IData parse(InputStream input, String mimeType) {
		LOGGER.info("URLListParser6");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {URLListDataBinding.class};
		return supportedClasses;
	}

	@Override
	public Object parseXML(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}
	

	


}
