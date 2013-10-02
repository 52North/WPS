/**
 * Copyright (C) 2013
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
 * 
 */
package org.n52.wps.io.datahandler.generator.mapserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umn.gis.mapscript.classObj;
import edu.umn.gis.mapscript.layerObj;
import edu.umn.gis.mapscript.mapObj;
import edu.umn.gis.mapscript.styleObj;

/**
 * This class managed the modifying of the mapfile of an UMN Mapserver. Right
 * now it only supports the adding of a Geotools FeatureCollection.
 * 
 * @author Jacob Mendt
 * 
 * @TODO better solution for replacing the old with the new mapfile.
 * @TODO Giving the ShapefileRepository as an external Input in the method
 *       addFeatureCollectionToMapfile is maybe not the best way.
 * @TODO check if layer is already in mapfile.
 * 
 */
public final class MSMapfileBinding {

	// thread-safe Singleton-Patterns
	private static MSMapfileBinding instance = new MSMapfileBinding();

	private static Logger LOGGER = LoggerFactory.getLogger(MSMapfileBinding.class);

	/**
	 * Default initialization of a new MSMapfileBinding Object.
	 */
	private MSMapfileBinding() {
	}

	/**
	 * Implementation of a thread-safe Singleton Pattern
	 * 
	 * @return MSMapfileBinding.class
	 */
	public static synchronized MSMapfileBinding getInstance() {
		return instance;
	}

	/**
	 * This method adds a FeatureCollection as an extra layer to the default
	 * mapfile of this MSMapfileBinding object.
	 * 
	 * @param ftColl
	 *            Input FeatureCollection which should be added as a layer to
	 *            the mapfile.
	 * @param workspace
	 *            Path to the workspace folder of the mapfile object.
	 * @param mapfile
	 *            Name of the mapfile object in the workspace folder.
	 * @param dataRep
	 *            Path to the data repository of the mapserver.
	 * 
	 * @return The name of the layer of featureCollection which was added to the
	 *         mapfile.
	 */
	public synchronized String addFeatureCollectionToMapfile(
			SimpleFeatureCollection ftColl, String workspacePath, String mapfilePath,
			String dataRepPath) {

		// saves the featureCollection as a shapefile in the dataRepository of
		// the mapfile
		String shapefilePath = null;
		try {
			shapefilePath = this.saveFeatureCollectionToShapefile(ftColl,
					workspacePath, dataRepPath);
			LOGGER.debug("Saving FeatureCollection as a Shapefile to the data repository of the mapfile successful.");
		} catch (IOException e1) {
			LOGGER.error("Error while saving FeatureCollection as a Shapefile to the data repository of the mapfile.");
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			LOGGER.error("Error while saving FeatureCollection as a Shapefile to the data repository of the mapfile.");
			e1.printStackTrace();
		} catch (Exception e) {
			LOGGER.error("Error while saving FeatureCollection as a Shapefile to the data repository of the mapfile.");
			e.printStackTrace();
		}

		// makes a temporary copy of the mapfile
		String orgMapfilePath = workspacePath + mapfilePath;
		String tmpMapfilePath = workspacePath + System.currentTimeMillis()
				+ ".map";
		try {
			if (copyMapfile(orgMapfilePath, tmpMapfilePath)) {
				LOGGER.debug("Creating of a temporary copy of the mapfile successful.");
			} else {
				LOGGER.error("Error while creating a temporary copy of the mapfile.");
				return null;
			}
		} catch (IOException e) {
			LOGGER.error("Error while creating a temporary copy of the mapfile.");
			e.printStackTrace();
		}

		// adds a the shapefile as a layer to the mapfile
		String layerName = null;
		try {
			layerName = this.addShapefileAsLayerToMapfile(shapefilePath,
					tmpMapfilePath, workspacePath);
			LOGGER.debug("Adding shapefile as a layer to the mapfile successful.");
		} catch (Exception e) {
			LOGGER.error("Error while adding shapefile as a layer to the mapfile.");
			e.printStackTrace();
		}

		// replace original mapfile through the temporary mapfile
		try {
			if (this.deleteMapfile(orgMapfilePath)) {
				if (this.copyMapfile(tmpMapfilePath, orgMapfilePath)) {
					this.deleteMapfile(tmpMapfilePath);
					LOGGER.info("Adding a FeatureCollection as a layer to the mapfile successful.");
					return layerName;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error while updating the original mapfile");
			e.printStackTrace();
		}

		// default return
		return null;
	}

	/**
	 * This method adds a shapfile as an extra layer to the default mapfile of
	 * this MSMapfileBinding object.
	 * 
	 * @param shapefilePath
	 *            Path to the shapefile which should be added to the mapfile.
	 * @param tmpMapfilePath
	 *            Path to the temporary copy of the mapfile.
	 * @param workspacePath
	 *            to the workspace folder of the mapfile object
	 * 
	 * @return The name of the added layer.
	 * 
	 * @TODO Check if layer already exist in the mapfile.
	 */
	private synchronized String addShapefileAsLayerToMapfile(
			String shapefilePath, String tmpMapfilePath, String workspacePath) {

		// initalize mapfile object
		mapObj mapfileObj = new mapObj(tmpMapfilePath);

		try {
			// initialize a BindingClass between the shapefile and mapscript
			// objects
			MSLayerBinding msLayer = null;
			try {
				msLayer = new MSLayerBinding(shapefilePath, workspacePath);
				LOGGER.debug("Initialzing MSLayerBinding object successful.");
			} catch (Exception e) {
				LOGGER.error("Error while initializing MSLayerBinding object.");
				e.printStackTrace();
			}

			if (msLayer != null) {

				// initalize a new mapfile layer object
				layerObj layer = new layerObj(mapfileObj);

				/*
				 * setting the propertys of the layer
				 */
				layer.setData(msLayer.getDataSourcePath());
				layer.setDump(1); // True -> Switch to allow MapServer to return
									// data in GML format
				layer.setName(msLayer.getMdTitle());
				layer.setProjection(msLayer.getMdCRS());
				layer.setStatus(1); // means ON - Sets the current status of the
									// layer. Often modified by MapServer
									// itself. Default turns the layer on
									// permanently.
				layer.setType(msLayer.getGeometryType());

				// set metadata
				layer.setMetaData("wms_title", msLayer.getMdTitle());
				layer.setMetaData("wms_timestamp", msLayer.getMdTimestamp()); // only date of the timestamp when the layer was added to the mapfile

				/*
				 * create a class and a style object of the layer and set styles
				 * and properties
				 */
				classObj layClass = new classObj(layer);
				layClass.setName(msLayer.getMdTitle());
				styleObj layStyle = new styleObj(layClass);
				layStyle.setColor(MSColorStyles.getDefaultColor());
				layStyle.setOutlinecolor(MSColorStyles.getDefaultOutlineColor());

				/*
				 * checks if the layerCRS is already supported by the wms, if
				 * not adds the layer CRS to the global metadata tag "wms_srs"
				 */

				String wmsSupportedCRS = mapfileObj.getMetaData("wms_srs");
				mapfileObj.setMetaData(
						"wms_srs",
						this.getMDSupportedCRS(wmsSupportedCRS,
								msLayer.getMdCRS()));
				// saves the new layer
				mapfileObj.save(tmpMapfilePath);

				LOGGER.debug("Setting layer data successful.");
				return msLayer.getMdTitle();
			}
		} catch (Exception e) {
			LOGGER.error("Error while adding shapefile to mapfile.");
			e.printStackTrace();
		} finally {
			mapfileObj = null;
		}

		// default return
		return null;
	}

	/**
	 * Checks if the metadata tag "wms_srs" already contains the crs of the
	 * layer
	 * 
	 * @param wmsSupportedCrs
	 *            content of the metadata tag "wms_srs"
	 * @param mdCrs
	 *            epsg code of the layer
	 * @return
	 */
	private String getMDSupportedCRS(String wmsSupportedCrs, String mdCrs) {
		String[] aCrs = wmsSupportedCrs.split(" ");
		for (int i = 0; i < aCrs.length; i++) {
			if (aCrs[i].equalsIgnoreCase(mdCrs)) {
				return wmsSupportedCrs;
			}
		}
		wmsSupportedCrs = wmsSupportedCrs + " " + mdCrs;
		return wmsSupportedCrs;
	}

	/**
	 * Creates a copy of the mapfile
	 * 
	 * @param String
	 *            orgMapfilePath Path to the original mapfile.
	 * @param String
	 *            tmpMapfilePath Path to the temporary mapfile.
	 * 
	 * @return <tt>true</tt> if the copying of the mapfile went okay.
	 * 
	 * @throws IOException
	 *             If something went wrong while copying the content of the
	 *             original mapfile to the temporary mapfile.
	 */
	private synchronized boolean copyMapfile(String orgMapfilePath,
			String tmpMapfilePath) throws IOException {

		// does a tmp copy of the mapfile
		File orgMapfile = new File(orgMapfilePath);
		File tmpMapfile = new File(tmpMapfilePath);
		FileChannel inCh = new FileInputStream(orgMapfile).getChannel();
		FileChannel outCh = new FileOutputStream(tmpMapfile).getChannel();
		try {
			inCh.transferTo(0, inCh.size(), outCh);
			LOGGER.debug("Creating of an temporary copy of the mapfile successful.");
			return true;
		} catch (IOException e) {
			LOGGER.error("Error while copying mapfile");
			e.printStackTrace();

		} finally {
			if (inCh != null)
				inCh.close();
			if (outCh != null)
				outCh.close();
		}

		// default return
		return false;
	}

	/**
	 * Deletes the mapfile.
	 * 
	 * @param mapfilePath
	 *            Path to the mapfile which should be delete.
	 * 
	 * @return <tt>true</tt> if the mapfile was deleted successful.
	 */
	private boolean deleteMapfile(String mapfilePath) {
		File tmpFile = new File(mapfilePath);
		if (tmpFile.exists()) {
			tmpFile.delete();
			return true;
		} else {
			LOGGER.debug("Mapfile doesn't exist.");
			return false;
		}
	}

	/**
	 * Saves the FeatureCollection as a shapefile in the data repository of the
	 * mapserver. If the CRS of the FeatureCollection is not WGS84 the
	 * FeatureCollection will be transformed to WGS 84
	 * 
	 * @param ftColl
	 *            FeatureCollection which should be saved as shapefile
	 * @param workspacePath
	 *            Path to the workspace folder of the mapfile object.
	 * @param dataRepository
	 *            Path to the data repository of the mapserver.
	 * 
	 * @return Path to the shapefile, which lies in the data repository of the
	 *         mapserver.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	private String saveFeatureCollectionToShapefile(
			SimpleFeatureCollection ftColl, String workspacePath,
			String dataRepository) throws IOException, URISyntaxException,
			Exception {
		
		//FeatureCollection ftColl = createCorrectFeatureCollection(ftCollWrong);
		//FeatureCollectionTester tester = new FeatureCollectionTester();
		//tester.testFeatureCollection(ftColl);
		
		SimpleFeatureType TYPE = (SimpleFeatureType) ftColl.getSchema();

		String shapefilePath;

		// Create the shapefilePath
		if (dataRepository.endsWith("/") || dataRepository.endsWith("\\")) {
			shapefilePath = workspacePath + dataRepository + "shape_"
					+ TYPE.getName().getLocalPart().toLowerCase() + "_"
					+ System.currentTimeMillis() + ".shp";
		} else
			shapefilePath = workspacePath + dataRepository + "/shape_"
					+ TYPE.getName().getLocalPart().toLowerCase() + "_"
					+ System.currentTimeMillis() + ".shp";

		/*
		 * Get an output file name and create the new shapefile
		 */
		File shapefile = new File(shapefilePath);
		URL url = shapefile.toURI().toURL();
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", url);
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);

		// Initialize the CRS for transformation
		CoordinateReferenceSystem sourceCrs = TYPE
				.getCoordinateReferenceSystem();

		newDataStore.createSchema(TYPE);
		newDataStore.forceSchemaCRS(sourceCrs);

		/*
		 * Write the features to the shapefile
		 */
		Transaction transaction = new DefaultTransaction("create");
		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
		if (featureSource instanceof FeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(ftColl);
				transaction.commit();
				LOGGER.debug("Saving FeatureCollection as shapefile successful.");
			} catch (Exception problem) {
				LOGGER.error("Error while saving FeatureCollection as shapefile.");
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		} else {
			LOGGER.error(typeName + " does not support read/write access");
			return null;
		}

		// returns the path to the shapefile
		return url.getPath();
	}
}
