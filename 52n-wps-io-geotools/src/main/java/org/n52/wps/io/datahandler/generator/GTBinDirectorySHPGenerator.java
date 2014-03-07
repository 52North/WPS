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
package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Geometry;

public class GTBinDirectorySHPGenerator {

	
	
	public File writeFeatureCollectionToDirectory(IData data)
			throws IOException {
		return writeFeatureCollectionToDirectory(data, null);
	}

	public File writeFeatureCollectionToDirectory(IData data, File parent) throws IOException {
		GTVectorDataBinding binding = (GTVectorDataBinding) data;
		FeatureCollection originalCollection = binding.getPayload();

		FeatureCollection collection = createCorrectFeatureCollection(originalCollection);
		
		return createShapefileDirectory(collection, parent);
	}
	
	private FeatureCollection createCorrectFeatureCollection(
			FeatureCollection fc) {
		
		FeatureCollection resultFeatureCollection = DefaultFeatureCollections.newCollection();
		SimpleFeatureType featureType = null;
		FeatureIterator iterator = fc.features();
		String uuid = UUID.randomUUID().toString();
		int i = 0;
		while(iterator.hasNext()){
			SimpleFeature feature = (SimpleFeature) iterator.next();
		
			if(i==0){
				featureType = GTHelper.createFeatureType(feature.getProperties(), (Geometry)feature.getDefaultGeometry(), uuid, feature.getFeatureType().getCoordinateReferenceSystem());
				QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
				SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
			}
			Feature resultFeature = GTHelper.createFeature("ID"+i, (Geometry)feature.getDefaultGeometry(), featureType, feature.getProperties());
		
			resultFeatureCollection.add(resultFeature);
			i++;
		}
		return resultFeatureCollection;
		
	}

	/**
	 * Transforms the given {@link FeatureCollection} into a zipped SHP file
	 * (.shp, .shx, .dbf, .prj) and returs its Base64 encoding
	 * 
	 * @param collection
	 *            the collection to transform
	 * @return the zipped shapefile
	 * @throws IOException
	 *             If an error occurs while creating the SHP file or encoding
	 *             the shapefile
	 * @throws IllegalAttributeException
	 *             If an error occurs while writing the features into the the
	 *             shapefile
	 */
	private File createShapefileDirectory(FeatureCollection collection, File parent)
			throws IOException, IllegalAttributeException {
		if (parent == null) {
			File tempBaseFile = File.createTempFile("resolveDir", ".tmp");
			tempBaseFile.deleteOnExit();
			parent = tempBaseFile.getParentFile();
		}

		if (parent == null || !parent.isDirectory()) {
			throw new IllegalStateException("Could not find temporary file directory.");
		}		
		
		File shpBaseDirectory = new File(parent, UUID.randomUUID().toString());
		
		if (!shpBaseDirectory.mkdir()) {
			throw new IllegalStateException("Could not create temporary shp directory.");
		}
		
		File tempSHPfile = File.createTempFile("shp", ".shp", shpBaseDirectory);
		tempSHPfile.deleteOnExit();
		DataStoreFactorySpi dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", tempSHPfile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		newDataStore.createSchema((SimpleFeatureType) collection.getSchema());
		if(collection.getSchema().getCoordinateReferenceSystem()==null){
			try {
				newDataStore.forceSchemaCRS(CRS.decode("4326"));
			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			newDataStore.forceSchemaCRS(collection.getSchema()
				.getCoordinateReferenceSystem());
		}

		Transaction transaction = new DefaultTransaction("create");

		String typeName = newDataStore.getTypeNames()[0];
		FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
				.getFeatureSource(typeName);
		featureStore.setTransaction(transaction);
		try {
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (Exception problem) {
			transaction.rollback();
		} finally {
			transaction.close();
		}

		// Zip the shapefile
		String path = tempSHPfile.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		
		// mark created files for delete
		tempSHPfile.deleteOnExit();
		shx.deleteOnExit();
		dbf.deleteOnExit();
		prj.deleteOnExit();
		shpBaseDirectory.deleteOnExit();
		
		return shpBaseDirectory;
	}

}
