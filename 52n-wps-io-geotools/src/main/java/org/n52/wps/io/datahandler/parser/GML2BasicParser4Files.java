/**
 * ﻿Copyright (C) 2007
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
 */


package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

/**
 * This parser handles xml files compliant to GML2.
 * @author schaeffer
 *
 */
public class GML2BasicParser4Files extends AbstractParser {
	private static Logger LOGGER = LoggerFactory.getLogger(GML2BasicParser4Files.class);

	
	public GML2BasicParser4Files() {
		super();
		supportedIDataTypes.add(GenericFileDataBinding.class);
	}
	
	public GenericFileDataBinding parse(InputStream stream, String mimeType, String schema) {
		
		FileOutputStream fos = null;
		try{
			File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".gml2");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GenericFileDataBinding data = parseXML(tempFile);
			
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	private GenericFileDataBinding parseXML(File file) {
		
		SimpleFeatureCollection fc = new GML2BasicParser().parseSimpleFeatureCollection(file);
		
		GenericFileDataBinding data = null;
		try {
			data = new GenericFileDataBinding(new GenericFileData(fc));
		} catch (IOException e) {
			LOGGER.error("Exception while trying to wrap GenericFileData around GML2 FeatureCollection.", e);
		}
				
		return data;
	}

}
