/***************************************************************
Copyright ï¿½ 2007 52ï¿½North Initiative for Geospatial Open Source Software GmbH

 Author: Bastian Schäffer, IfGI; Matthias Mueller, TU Dresden

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
package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

public class KMLGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(KMLGenerator.class);
	
	public KMLGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
		File tempFile = null;
		InputStream stream = null;
		try {
			tempFile = File.createTempFile("kml", "xml");
			this.finalizeFiles.add(tempFile);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			this.writeToStream(data, outputStream);
			outputStream.flush();
			outputStream.close();
			
			stream = new FileInputStream(tempFile);
		} catch (IOException e){
			LOGGER.error(e.getMessage());
			throw new IOException("Unable to generate KML");
		}
		
		return stream;
	}

	private void writeToStream(IData coll, OutputStream os) {
		FeatureCollection<?, ?> fc = ((GTVectorDataBinding)coll).getPayload();
		
        Configuration configuration = new KMLConfiguration();
        Encoder encoder = new org.geotools.xml.Encoder(configuration);
       
        try{
            encoder.encode(fc, KML.kml, os);
           
        }catch(IOException e){
        	throw new RuntimeException(e);
        }
	}

}
