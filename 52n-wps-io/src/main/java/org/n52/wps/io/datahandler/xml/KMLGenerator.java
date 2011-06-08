/***************************************************************
Copyright ï¿½ 2007 52ï¿½North Initiative for Geospatial Open Source Software GmbH

 Author: Bastian Schäffer, IfGI

 Contact: Andreas Wytzisk, 
 52ï¿½North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundationï¿½s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.datahandler.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class KMLGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	
	private static Logger LOGGER = Logger.getLogger(KMLGenerator.class);
		
		
	public void write(IData coll, Writer writer) {
		throw new RuntimeException("Write method unsupported for KML generator");
	}

	

	public Node generateXML(IData coll, String schema) {
		File f = null;
		FileWriter writer = null;
		try {
			f = File.createTempFile("gml2", "xml");
			FileOutputStream outputStream = new FileOutputStream(f);
			this.writeToStream(coll, outputStream);
			outputStream.flush();
			outputStream.close();
			if(f.length() <= 0) {
				return null;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Node node = builder.parse(f);
			if (f != null) f.delete();
			return node;
		}
		catch (IOException e){
			throw new RuntimeException(e);
		}
		catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		catch(SAXException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (writer != null) try { writer.close(); } catch (Exception e) { }
			if (f != null) f.delete();
		}
		
	}

	public OutputStream generate(IData coll) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(coll, baos);		
		return baos;
	}

	public void writeToStream(IData coll, OutputStream os) {
		FeatureCollection fc = ((GTVectorDataBinding)coll).getPayload();
		
        Configuration configuration = new KMLConfiguration();
        Encoder encoder = new org.geotools.xml.Encoder(configuration);
       
        try{
            encoder.encode(fc, KML.kml, os);
           
        }catch(IOException e){
        	throw new RuntimeException(e);
        }
        			
	}

	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTVectorDataBinding.class};
		return supportedClasses;
	
	}


}
