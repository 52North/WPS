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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umn.gis.mapscript.MS_LAYER_TYPE;
import edu.umn.gis.mapscript.rectObj;

/**
 * This Class managed the communication between the input shapefile and the
 * mapscript layer objects. It extracts out from the shapefile different
 * information and creates the corresponding mapscript object.
 * 
 * @author Jacob Mendt
 * 
 * @TODO Layer management
 * @TODO Timestamp
 */
public class MSLayerBinding {

	// metadata parameter
	private String mdSrs;
	private String mdTitle;
	private String mdTimestamp;

	// filesystem paths
	private String dataSourcePath;

	// FeatureSource representation of the shapefile
	private FeatureSource<?, ?> ftSource;

	// mapscript objects
	private MS_LAYER_TYPE geometryType;
	private rectObj bbox;

	private CoordinateReferenceSystem crs;

	private static Logger LOGGER = LoggerFactory.getLogger(MSLayerBinding.class);

	/**
	 * Initializes a new MSLayerBinding Object.
	 * 
	 * @param shapePath
	 *            Path to the shapefile object which should be encapsulated.
	 * @param workspace
	 *            Path to the workspace of the mapfile object.
	 * 
	 * @throws Exception
	 */
	public MSLayerBinding(String shapePath, String workspace) throws Exception {

		/*
		 * Checks if the shapefile path lies within the folder hierarchy of the
		 * mapfile workspace and opens the shapefile for which mapscript objects
		 * should be extracted.
		 */
		if (this.parseDataSourcePath(shapePath, workspace)) {
			LOGGER.debug("Parsing of the relativ data source path successful.");
			if (this.openShapefile(shapePath)) {
				LOGGER.debug("Opening and parsing of the shapefile successful.");
			} else
				LOGGER.error("Error while opening and parsing the shapefile.");
		} else {
			LOGGER.error("Shapefile doesn't lie in the folder hierarchy of the mapfile workspace.");
			throw new Exception("Error while opening shapefile: " + shapePath);
		}

		// parse featureType (POINT,POLYLINE, ...)
		if (this.parseGeometryDescription()) {
			LOGGER.debug("Parsing Geometry sucessful.");
		} else {
			LOGGER.error("Error while parsing Geometry type.");
			throw new Exception("Error while parsing Geometry type.");
		}

		/*
		 * Parsing the code of the CoordinateReferenceSystem. If the Code is
		 * "CRS:84" it replaced the code with the epsg code 4326
		 */
		ReferencedEnvelope envelope = null;
		try {
			envelope = ftSource.getBounds();
			crs = envelope.getCoordinateReferenceSystem();
		} catch (IOException e) {
			LOGGER.error("Error while parsing the CoordinateReferenceSystem from the shapefile.");
			e.printStackTrace();
		}
		try {
			String code = CRS.lookupIdentifier(crs, true);
			if (code.equalsIgnoreCase("CRS:84")) {
				mdSrs = "EPSG:4326";
				LOGGER.debug("Parsing CoordinateReferenceSystem successful.");
			} else {
				mdSrs = code;
				LOGGER.debug("Parsing CoordinateReferenceSystem successful.");
			}
		} catch (FactoryException e) {
			LOGGER.error("Could not parse the CoorinateReferenceSystem");
			e.printStackTrace();
		}

		// create out of the envelope object an rectObj (mapscript)
		bbox = new rectObj(envelope.getMinX(), envelope.getMinY(),
				envelope.getMaxX(), envelope.getMaxY(), 0);

		// parse the name
		mdTitle = ftSource.getName().getLocalPart();

		// creates a timestamp (actual date) for this layer
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd,HH:mm:ss");
		mdTimestamp = dateFormat.format(new Date());
		LOGGER.debug("Creating timestamp object sucessful: " + mdTimestamp);

	}

	/**
	 * Parse the relativ data source path for referencing the data source folder
	 * in the mapfile. Data source folder have to be in the folder hierarchy of
	 * the workspace.
	 * 
	 * @param shapePath
	 *            Path to the shapefile.
	 * @param workspace
	 *            Path to the folder/workspace of the mapfile.
	 * 
	 * @return <tt>true</tt> if relativ data source path is set and lies in the
	 *         folder hierarchy of the workspace
	 */
	private boolean parseDataSourcePath(String shapePath, String workspace) {
		if (shapePath.contains(workspace)) {
			int tmp = shapePath.length()
					- (shapePath.length() - workspace.length() - 1);
			dataSourcePath = shapePath.substring(tmp, shapePath.length());
			LOGGER.debug("Shapefile lies in the folder hierarchy of the mapfile workspace.");
			return true;
		} else {
			LOGGER.warn("Shapefile doesn't lies in the folder hierarchy of the mapfile workspace.");
			return false;
		}
	}

	/**
	 * Opens the shapefile as a GeoTools FeatureSource for further use in the
	 * MSLayerBinding class.
	 * 
	 * @param shapePath
	 *            Path to the shapefile
	 * 
	 * @return <tt>true</tt> if the shapefile could be opened successful.
	 */
	private boolean openShapefile(String shapePath) {
		ftSource = null;
		try {
			FileDataStore store = FileDataStoreFinder.getDataStore(new File(
					shapePath));
			ftSource = store.getFeatureSource();
		} catch (IOException e) {
			LOGGER.error("Error while opening the shapefile.");
			e.printStackTrace();
		}

		// tests if the featureSource is empty
		if (ftSource == null) {
			LOGGER.debug("Could not open shapefile: " + shapePath);
			return false;
		} else if (ftSource != null) {
			LOGGER.debug("Open shapefile as FeatureSource successful.");
			return true;
		}

		// default return
		return false;
	}

	/**
	 * Parse the GeometryType from the FeatureSource and creates an mapscript
	 * MS_LAYER_TYPE object. Right know this class supports the geometryType
	 * POINT, MULTIPOINT, LINE, MULTILINESTRING, POLYGON, MULTIPOLYGON
	 * 
	 * @return <tt>true</tt> if the GeometryType could be parsed
	 */
	private boolean parseGeometryDescription() {
		GeometryDescriptor geomDescription = ftSource.getSchema()
				.getGeometryDescriptor();

		if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("POINT")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_POINT;
			return true;
		} else if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("MULTIPOINT")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_POINT;
			return true;
		} else if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("LINE")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_LINE;
			return true;
		} else if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("MULTILINESTRING")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_LINE;
			return true;
		} else if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("POLYGON")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_POLYGON;
			return true;
		} else if (geomDescription.getType().getName().toString()
				.equalsIgnoreCase("MULTIPOLYGON")) {
			geometryType = MS_LAYER_TYPE.MS_LAYER_POLYGON;
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @return CoordinateReferenceSystem
	 */
	public CoordinateReferenceSystem getCRS() {
		return crs;
	}

	/**
	 * 
	 * @return String EPSG Code for the SRS of the layer
	 */
	public String getMdCRS() {
		return mdSrs;
	}

	/**
	 * 
	 * @return String Title of the layer
	 */
	public String getMdTitle() {
		return mdTitle;
	}

	/**
	 * 
	 * @return String Timestamp of the layer
	 */
	public String getMdTimestamp() {
		return mdTimestamp;
	}

	/**
	 * 
	 * @return GeomtryType Right now the class supports POINT, MULTIPOINT, LINE,
	 *         MULTILINESTRING, POLYGON and MULTIPOLYGON
	 */
	public MS_LAYER_TYPE getGeometryType() {
		return geometryType;
	}

	/**
	 * 
	 * @return String Path to the shapefile data source of the layer
	 */
	public String getDataSourcePath() {
		return dataSourcePath;
	}

	/**
	 * 
	 * @return rectObj BoundingBox of the layer
	 */
	public rectObj getBBox() {
		return bbox;
	}

	/**
	 * 
	 * @return String BoundingBox as a String
	 */
	public String getMdBBox() {
		return bbox.getMinx() + " " + bbox.getMiny() + " " + bbox.getMaxx()
				+ " " + bbox.getMaxy();
	}

}
