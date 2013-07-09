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
package org.n52.wps.io.datahandler.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * 
 * 
 * This class parses json into JTS geometries.
 *         
 *  @author BenjaminPross(bpross-52n)
 * 
 */
public class GeoJSONParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(GeoJSONParser.class);

	public GeoJSONParser() {
		super();
		supportedIDataTypes.add(JTSGeometryBinding.class);
	}

	@Override
	public IData parse(InputStream input, String mimeType, String schema) {

		String geojsonstring = "";

		String line = "";

		BufferedReader breader = new BufferedReader(
				new InputStreamReader(input));

		try {
			while ((line = breader.readLine()) != null) {
				geojsonstring = geojsonstring.concat(line);
			}
		} catch (IOException e) {
			LOGGER.error("Exception while reading inputstream.", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		if (geojsonstring.contains("FeatureCollection")) {

			try {
				FeatureCollection<?, ?> featureCollection = new FeatureJSON()
						.readFeatureCollection(geojsonstring);

				return new GTVectorDataBinding(featureCollection);

			} catch (IOException e) {
				LOGGER.info("Could not read FeatureCollection from inputstream");
			}

		} else if (geojsonstring.contains("Feature")) {

			try {
				SimpleFeature feature = new FeatureJSON().readFeature(geojsonstring);

				List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();

				ListFeatureCollection featureCollection = new ListFeatureCollection(
						feature.getFeatureType(), featureList);

				return new GTVectorDataBinding(featureCollection);

			} catch (IOException e) {
				LOGGER.info("Could not read Feature from inputstream");
			}

		} else if (geojsonstring.contains("GeometryCollection")) {

			try {
				GeometryCollection g = new GeometryJSON().readGeometryCollection(geojsonstring);

				return new JTSGeometryBinding(g);

			} catch (IOException e) {
				LOGGER.info("Could not read GeometryCollection from inputstream.");
			}

		} else if(geojsonstring.contains("Point") || 
				geojsonstring.contains("LineString") ||
				geojsonstring.contains("Polygon") ||
				geojsonstring.contains("MultiPoint") ||
				geojsonstring.contains("MultiLineString") ||
				geojsonstring.contains("MultiPolygon")){

			try {
				Geometry g = new GeometryJSON().read(geojsonstring);

				return new JTSGeometryBinding(g);

			} catch (IOException e) {
				LOGGER.info("Could not read single Geometry from inputstream.");
			}

		}
		LOGGER.error("Could not parse inputstream, returning null.");
		return null;
	}

}
