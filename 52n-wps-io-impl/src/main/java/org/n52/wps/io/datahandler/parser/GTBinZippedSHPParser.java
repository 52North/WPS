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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

public class GTBinZippedSHPParser extends AbstractParser {
	
	public GTBinZippedSHPParser(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	/**
	 * @throws RuntimeException
	 *             if an error occurs while writing the stream to disk or
	 *             unzipping the written file
	 * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
	 */
	@Override
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {
		try {
			String fileName = "tempfile" + UUID.randomUUID() + ".zip";
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			File tempFile = new File(tmpDirPath + File.separatorChar + fileName);
			finalizeFiles.add(tempFile); // mark tempFile for final delete
			try {
				FileOutputStream outputStream = new FileOutputStream(tempFile);
				byte buf[] = new byte[4096];
				int len;
				while ((len = stream.read(buf)) > 0) {
					outputStream.write(buf, 0, len);
				}
				outputStream.close();
				stream.close();
			} catch (FileNotFoundException e) {
				System.gc();
				throw new RuntimeException(e);
			} catch (IOException e1) {
				System.gc();
				throw new RuntimeException(e1);
			}
			File shp = IOUtils.unzip(tempFile, "shp").get(0);
			DataStore store = new ShapefileDataStore(shp.toURI().toURL());
			SimpleFeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			System.gc();
			
			return new GTVectorDataBinding(features);
		} catch (IOException e) {
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		}
	}


	
}
