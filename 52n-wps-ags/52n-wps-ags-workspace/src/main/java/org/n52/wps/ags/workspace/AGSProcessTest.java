/***************************************************************
Copyright © 2012 52°North Initiative for Geospatial Open Source Software GmbH

 Author: < >

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.ags.workspace;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.esri.arcgis.datasourcesfile.ShapefileWorkspaceFactory;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnumGeometry;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryCollection;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.ITopologicalOperator;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.geoprocessing.IGeoProcessorResult;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerConnection;
import com.esri.arcgis.server.IServerContext;
import com.esri.arcgis.server.IServerObjectManager;
import com.esri.arcgis.server.ServerConnection;
import com.esri.arcgis.system.IVariantArray;
import com.esri.arcgis.system.ServerInitializer;
import com.esri.arcgis.system.VarArray;

public class AGSProcessTest {

	private static final Logger logger = LoggerFactory.getLogger(AGSProcessTest.class.getName());
	private static String domain   = "gin-rieke";
	private static String user     = "arcgismanager";	// arcgismanager account
	private static String password = "2asdf3";	// arcgismanager password
	private static String host     = "localhost";
	private static File workspaceDirectory = new File("C:/arcgisserver/wpsBackendDirectory");


	private String toolname;
	private String[] params;
	private IServerObjectManager som;
	private IServerContext context;

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		AGSProcessTest tester = new AGSProcessTest();
		tester.initializeServerContext();
		tester.testIntersectAnalysis();
		tester.testClipAnalysis();
		tester.testBufferAnalysis();
		tester.createFeatureClassWorkspace();
	}

	public AGSProcessTest() {
		createServerObjects();
	}


	private void testIntersectAnalysis() {
		configureForIntersect();
		launchProcessor();
	}

	private void testBufferAnalysis() {
		configureForBuffer();
		launchProcessor();		
	}

	private void testClipAnalysis() {
		configureForClip();
		launchProcessor();		
	}

	private IGeoProcessorResult launchProcessor() {
		logger.info("Execute tool (\"" + toolname + "\"):");
		for (int i = 0; i < params.length; i++){
			logger.info("     Input " + i + ": " + params[i]);
		}

		return executeProcess(toolname, params);
	}

	private IGeoProcessorResult executeProcess(String toolname2, String[] params2) {
		IVariantArray fParams = null;
		try {
//			IServerContext context = initializeServerContext();
			fParams = (VarArray)(context.createObject(VarArray.getClsid()));
		} catch (AutomationException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		for (int i = 0; i < params.length; i++) {
			try {
				fParams.add(params2[i]);
			} catch (AutomationException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			} catch (IOException e) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}		
		}

		IGeoProcessorResult result = null;
		try {
			IServerContext context = initializeServerContext();
			result = initiliazeGeoProcesser(context).execute(toolname, fParams, null);
			context.releaseContext();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		return result;
	}


	private IServerContext initializeServerContext() throws AutomationException, IOException {
		context = som.createServerContext("", "");
		return context;
	}

	private void configureForIntersect() {
		toolname = "Intersect_analysis";

		File inputFeature1 = new File(workspaceDirectory, "feature1.shp");
		File inputFeature2 = new File(workspaceDirectory, "feature2.shp");
		File outputFeature = new File(workspaceDirectory, "intersect1.shp");

		if (!inputFeature1.exists()) {
			throw new IllegalStateException("File not found: "+inputFeature1.getAbsolutePath());
		}
		if (!inputFeature2.exists()) {
			throw new IllegalStateException("File not found: "+inputFeature2.getAbsolutePath());
		}

		params = new String[2];
		params[0] = inputFeature1.getAbsolutePath() +" ; "+ inputFeature2.getAbsolutePath();
		params[1] = outputFeature.getAbsolutePath();
	}

	private void configureForClip() {
		toolname = "Clip_analysis";

		File inputFeature1 = new File(workspaceDirectory, "feature1.shp");
		File inputFeature2 = new File(workspaceDirectory, "feature2.shp");
		File outputFeature = new File(workspaceDirectory, "clip1.shp");

		if (!inputFeature1.exists()) {
			throw new IllegalStateException("File not found: "+inputFeature1.getAbsolutePath());
		}
		if (!inputFeature2.exists()) {
			throw new IllegalStateException("File not found: "+inputFeature2.getAbsolutePath());
		}

		params = new String[3];

		params[0] = inputFeature1.getAbsolutePath();
		params[1] = inputFeature2.getAbsolutePath();
		params[2] = outputFeature.getAbsolutePath();
	}

	private void configureForBuffer() {
		toolname = "Buffer_analysis";
		File inputFeature1 = new File(workspaceDirectory, "feature1.shp");
		File outputFeature = new File(workspaceDirectory, "buffer1.shp");

		if (!inputFeature1.exists()) {
			throw new IllegalStateException("File not found: "+inputFeature1.getAbsolutePath());
		}

		params = new String[3];

		params[0] = inputFeature1.getAbsolutePath();
		params[1] = outputFeature.getAbsolutePath();
		params[2] = "0.05";
	}

	private void createServerObjects() {
		ServerInitializer serverInitializer = new ServerInitializer();
		serverInitializer.initializeServer(domain, user, password);

		IServerConnection connection = null;
		try {
			connection = new ServerConnection();
			connection.connect(host);
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		logger.info("Connected to server.");

		try {
			som = connection.getServerObjectManager();
		} catch (AutomationException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

	}

	private GeoProcessor initiliazeGeoProcesser(IServerContext context) throws AutomationException, IOException {
		GeoProcessor gp = (GeoProcessor) context.createObject(GeoProcessor.getClsid());
		gp.setOverwriteOutput(true);
		return gp;
	}

	private ShapefileWorkspaceFactory initializeShapefileWorkspace(IServerContext context) throws AutomationException, IOException {
		return (ShapefileWorkspaceFactory) context.createObject(ShapefileWorkspaceFactory.getClsid());
	}
	
	private void createFeatureClassWorkspace() throws AutomationException, IOException {
		File inputFeature1 = new File(workspaceDirectory, "feature1.shp");
		File inputFeature2 = new File(workspaceDirectory, "feature2.shp");

		System.out.println(intersects(inputFeature1, inputFeature2));
	}

	private boolean intersects(File inputFeature1, File inputFeature2) throws AutomationException, IOException {
		IRelationalOperator geom1 = resolveGeometryFromShapefile(inputFeature1);
		IRelationalOperator geom2 = resolveGeometryFromShapefile(inputFeature2);
		return !geom1.disjoint((IGeometry) geom2);
	}

	private IRelationalOperator resolveGeometryFromShapefile(File inputFeature1) throws AutomationException, IOException {
//		IServerContext context = initializeServerContext();
		String name = inputFeature1.getName();
		Workspace srcWorkspace = new Workspace(initializeShapefileWorkspace(context).openFromFile(inputFeature1.getParent(), 0));
		IFeatureClass srcFeatureClass = srcWorkspace.openFeatureClass(removeFileExtension(name));
		System.out.println(srcFeatureClass.getShapeFieldName());
		
		IFeatureCursor cur = srcFeatureClass.search(null, false);
		IFeature feature = cur.nextFeature();
		List<IGeometry> geometries = new ArrayList<IGeometry>();
		while (feature != null) {
			System.out.println(feature.getShape().getGeometryType());
			System.out.println(feature.getShape().getEnvelope().getLowerLeft().getX());
			feature = cur.nextFeature();
		}

		ITopologicalOperator resultPolygon = (ITopologicalOperator) context.createObject(Polygon.getClsid());
		IGeometryCollection geometriesToUnion = (IGeometryCollection) context.createObject(GeometryBag.getClsid());
		for (IGeometry geom : geometries) {
			geometriesToUnion.addGeometry(geom, null, null);
		}
		resultPolygon.constructUnion((IEnumGeometry) geometriesToUnion);
		return (IRelationalOperator) resultPolygon;
	}

	private String removeFileExtension(String name) {
		int index = name.lastIndexOf(".");
		return name.substring(0, index);
	}


}