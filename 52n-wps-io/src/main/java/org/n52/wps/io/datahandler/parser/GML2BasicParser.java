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
	Matthias Mueller, TU Dresden


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

package org.n52.wps.io.datahandler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author foerster
 *
 */
public class GML2BasicParser extends AbstractParser {
	private static Logger LOGGER = Logger.getLogger(GML2BasicParser.class);
		
	public GML2BasicParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {
        Configuration configuration = new GMLConfiguration();		
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);
		
		//parse
		FeatureCollection fc = DefaultFeatureCollections.newCollection();
		try {
			Object parsedData =  parser.parse(stream);
			
			if (parsedData instanceof FeatureCollection) {
				fc = (FeatureCollection) parsedData;
			} else {

				if (((HashMap) parsedData).get("featureMember") instanceof SimpleFeature) {
					SimpleFeature feature = (SimpleFeature) ((HashMap) parsedData)
							.get("featureMember");
					fc.add(feature);
				} else if (((HashMap) parsedData).get("featureMember") instanceof List) {

					List<SimpleFeature> featureList = ((ArrayList<SimpleFeature>) ((HashMap) parsedData)
							.get("featureMember"));
					for (SimpleFeature feature : featureList) {
						fc.add(feature);
					}
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
	
}
