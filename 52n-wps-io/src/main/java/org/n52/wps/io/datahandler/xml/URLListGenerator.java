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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;

import net.opengis.examples.packet.DataType;
import net.opengis.examples.packet.GMLPacketDocument;
import net.opengis.examples.packet.GMLPacketType;
import net.opengis.examples.packet.PropertyType;
import net.opengis.examples.packet.StaticFeatureType;
import net.opengis.examples.packet.PropertyType.Value;
import net.opengis.gml.CoordType;
import net.opengis.gml.LineStringPropertyType;
import net.opengis.gml.LinearRingMemberType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PointPropertyType;
import net.opengis.gml.PolygonType;

import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.URLListDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.opengis.feature.simple.SimpleFeature;
import org.w3c.dom.Node;

import xint.esa.ssegrid.wps.javaSAGAProfile.URLListDocument;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class URLListGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	
	
	public URLListGenerator()
	{
		super();
	}
	
	public URLListGenerator(boolean pReadWPSConfig)
	{
		super(pReadWPSConfig);
	}

	@Override
	public OutputStream generate(IData coll) {
		// TODO Auto-generated method stub
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(coll, baos);
		return baos;
	}

	@Override
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {URLListDataBinding.class};
		return supportedClasses;
		
	}

	@Override
	public void writeToStream(IData data, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		//write(data, w);	
		//TODO
	}

	@Override
	public Node generateXML(IData coll, String schema) {
		// TODO Auto-generated method stub
		return ((URLListDataBinding)coll).getPayload().getDomNode();
		
	}

}
