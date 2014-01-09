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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileUtilities;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IComplexData;


public class ShapefileBinding implements IComplexData{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger LOGGER = LoggerFactory.getLogger(ShapefileBinding.class);
	
	
	protected File shpFile;
	protected String mimeType;
	
	public ShapefileBinding(File shapeFile){
		this.shpFile = shapeFile;
		mimeType = IOHandler.MIME_TYPE_ZIPPED_SHP;
	}
	
	@Override
	public File getPayload() {
		return shpFile;
	}

	@Override
	public Class getSupportedClass() {
		return File.class;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public File getZippedPayload(){
		String path = shpFile.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		File zipped = null;
		try {
			zipped = IOUtils.zip(shpFile, shx, dbf, prj);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return zipped;

	}
	
	public GTVectorDataBinding getPayloadAsGTVectorDataBinding(){
		try {
			DataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(store.getTypeNames()[0]).getFeatures();
//			System.gc(); XXX WTF, dude? System.gc() is the root of evil.
			return new GTVectorDataBinding(features);
		} catch (MalformedURLException e) {
			LOGGER.error("Something went wrong while creating data store.", e);
			throw new RuntimeException("Something went wrong while creating data store.", e);
		} catch (IOException e) {
			LOGGER.error("Something went wrong while converting shapefile to FeatureCollection", e);
			throw new RuntimeException("Something went wrong while converting shapefile to FeatureCollection", e);
		}
	}
	
    @Override
	public void dispose(){
        FileUtils.deleteQuietly(shpFile);
	}
	
	
}
