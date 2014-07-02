/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.referencing.CRS;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author Bastian Schaeffer; Matthias Mueller, TU Dresden
 *
 */
public class GTBinZippedWKT64Parser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GTBinZippedWKT64Parser.class);
	
	public GTBinZippedWKT64Parser() {
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
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				System.gc();
				throw new RuntimeException(e);
			}			
			
			finalizeFiles.add(tempFile); // mark for final delete
			stream.close();
			List<File> wktFiles = IOUtils.unzip(tempFile, "wkt");
			finalizeFiles.addAll(wktFiles); // mark for final delete
			
			if (wktFiles == null || wktFiles.size() == 0) {
				throw new RuntimeException(
						"Cannot find a shapefile inside the zipped file.");
			}

			//set namespace namespace
			List<Geometry> geometries = new ArrayList<Geometry>();
		
			//read wkt file
			//please not that only 1 geometry is returned. If multiple geometries are included, perhaps use the read(String wktstring) method
			for(int i = 0; i<wktFiles.size();i++){
				File wktFile = wktFiles.get(i);
				Reader fileReader = new FileReader(wktFile);
				
				WKTReader2 wktReader = new WKTReader2();
				com.vividsolutions.jts.geom.Geometry geometry = wktReader.read(fileReader);
				geometries.add(geometry);
			}

			CoordinateReferenceSystem coordinateReferenceSystem = CRS.decode("EPSG:4326");			
			SimpleFeatureCollection inputFeatureCollection = createFeatureCollection(geometries, coordinateReferenceSystem);

			return new GTVectorDataBinding(inputFeatureCollection);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		} catch (ParseException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);		
		} catch (NoSuchAuthorityCodeException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);		
		} catch (FactoryException e) {
			LOGGER.error(e.getMessage(), e);
				throw new RuntimeException(
						"An error has occurred while accessing provided data", e);			
		}
	}
	
	private SimpleFeatureCollection createFeatureCollection(List<com.vividsolutions.jts.geom.Geometry> geometries, CoordinateReferenceSystem coordinateReferenceSystem){

		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		if(coordinateReferenceSystem==null){
			try {
				coordinateReferenceSystem = CRS.decode("EPSG:4326");
			} catch (NoSuchAuthorityCodeException e) {
			LOGGER.error(e.getMessage(), e);
				throw new RuntimeException(
						"An error has occurred while trying to decode CRS EPSG:4326", e);				
			} catch (FactoryException e) {
			LOGGER.error(e.getMessage());
				throw new RuntimeException(
						"An error has occurred while trying to decode CRS EPSG:432", e);
			}
			typeBuilder.setCRS(coordinateReferenceSystem);
		}
	
		String namespace = "http://www.opengis.net/gml";
		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature");
		typeBuilder.setName(nameType);
		typeBuilder.add("GEOMETRY", geometries.get(0).getClass());
	
		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();		
		
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
	
		for(int i = 0; i<geometries.size();i++){
				SimpleFeature feature = GTHelper.createFeature(""+i, geometries.get(i), featureType, new ArrayList<Property>());
				simpleFeatureList.add(feature);		}
		
		
		SimpleFeatureCollection collection =  new ListFeatureCollection(featureType, simpleFeatureList);
		
		return collection;
	}

}
