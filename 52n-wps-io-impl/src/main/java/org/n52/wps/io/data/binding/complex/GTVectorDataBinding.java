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
package org.n52.wps.io.data.binding.complex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.geotools.feature.FeatureCollection;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.datahandler.generator.SimpleGMLGenerator;
import org.n52.wps.io.datahandler.parser.SimpleGMLParser;

public class GTVectorDataBinding implements IComplexData{
	
	protected transient FeatureCollection<?, ?> featureCollection;	
	
	public GTVectorDataBinding(FeatureCollection<?, ?> payload) {
		this.featureCollection = payload;
	}

	public Class<FeatureCollection> getSupportedClass() {
		return FeatureCollection.class;
	}

	public FeatureCollection<?, ?> getPayload() {
			return featureCollection;
	}
	
	public File getPayloadAsShpFile(){
		try {
			return GenericFileData.getShpFile(featureCollection);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not transform Feature Collection into shp file. Reason " +e.getMessage());
		}
		
	}
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		StringWriter buffer = new StringWriter();
		SimpleGMLGenerator generator = new SimpleGMLGenerator();
		generator.write(this, buffer);
		oos.writeObject(buffer.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		SimpleGMLParser parser = new SimpleGMLParser();
			
		InputStream stream = new ByteArrayInputStream(((String) oos.readObject()).getBytes());
		
		// use a default configuration for the parser by requesting the first supported format and schema
		GTVectorDataBinding data = parser.parse(stream, parser.getSupportedFormats()[0], parser.getSupportedEncodings()[0]);
		
		this.featureCollection = data.getPayload();
	}
	
	@Override
    public void dispose() {
        
    }

}
