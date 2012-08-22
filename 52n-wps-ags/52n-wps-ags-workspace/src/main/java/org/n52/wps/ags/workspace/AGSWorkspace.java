/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

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
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.ags.workspace.feature.SpatialRelation;
import org.n52.wps.ags.workspace.feature.SpatialRelationFeaturePair;

import com.esri.arcgis.datasourcesfile.ShapefileWorkspaceFactory;
import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerContext;
import com.esri.arcgis.server.IServerObjectManager;
import com.esri.arcgis.server.ServerConnection;
import com.esri.arcgis.system.IVariantArray;
import com.esri.arcgis.system.ServerInitializer;
import com.esri.arcgis.system.VarArray;


public class AGSWorkspace {
	
	private static Logger LOGGER = Logger.getLogger(AGSWorkspace.class);
	public static IServerContext context;
	private final File workspaceDir;
	
	static {
		initializeServerObjects();
	}

	public AGSWorkspace(File workspace){
		workspaceDir = workspace;
	}

	private static void initializeServerObjects() {
		if (LOGGER.isInfoEnabled()) {
			if (System.getProperty("JINTEGRA_NATIVE_MODE") == null) {
				LOGGER.info("Running geoprocessor in DCOM Mode");
			} else {
				LOGGER.info("Running geoprocessor in Native Mode");
			} 
		}

		try{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Getting AGS connection object ...");
			ServerConnection connection = getAGSConnection();
			IServerObjectManager som = connection.getServerObjectManager();
			context = som.createServerContext("", "");
		}
		catch (AutomationException ae){
			LOGGER.error("Caught J-Integra AutomationException: " + ae.getMessage() + "\n");
		}

		catch (IOException e){
			LOGGER.error("Caught IOException: " + e.getMessage() + "\n");
		}
	}
	
	private static final ServerConnection getAGSConnection() throws IOException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("initializing server ...");
		AGSPropertiesWrapper agsProps = AGSPropertiesWrapper.getInstance();
		ServerInitializer serverInitializer = new ServerInitializer();
		serverInitializer.initializeServer(agsProps.getDomain(), agsProps.getUser(), agsProps.getPass());
		
		ServerConnection connection = null;
		try {
			connection = new ServerConnection();
			connection.connect(agsProps.getIP());
			if (LOGGER.isInfoEnabled())
				LOGGER.info("server initialized!");

		} catch (UnknownHostException e) {
			LOGGER.error("UnknownHostException - Could not connect to AGS host " + agsProps.getDomain() + " with user " + agsProps.getUser());
			throw new IOException("Error connecting to ArcGIS Server.");
		} catch (IOException e) {
			LOGGER.error("IOException - Could not connect to AGS host " + agsProps.getDomain() + " with user " + agsProps.getUser());
			LOGGER.info("Please check firewall setup! - and maybe the folder permissions, too");
			throw new IOException("Error connecting to ArcGIS Server.");
		}

		return connection;
	}

	public final void executeGPTool(String toolName, String toolboxPath, String[] parameters) throws IOException {
		try{
			GeoProcessor gp = (GeoProcessor) (context.createObject(GeoProcessor.getClsid()));

			IVariantArray paramsPreparedForGeoProcessor = (VarArray)(context.createObject(VarArray.getClsid()));
			for (int i = 0; i < parameters.length; i++) {
				paramsPreparedForGeoProcessor.add(parameters[i]);
			}

			if (toolboxPath != null){
				gp.addToolbox(toolboxPath);
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Added: " + toolboxPath);
			}

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Executing GPTool " +toolName);
			gp.execute(toolName, paramsPreparedForGeoProcessor, null);

			if (LOGGER.isInfoEnabled())
				LOGGER.info("done!");
		}
		catch (AutomationException ae){
			LOGGER.error("Caught J-Integra AutomationException: " + ae.getMessage() + "\n");
			throw new IOException("Error executing ArcGIS Server geoprocessor.");
		}

		catch (IOException e){
			LOGGER.error("Caught IOException: " + e.getMessage() + "\n");
			throw new IOException("Error executing ArcGIS Server geoprocessor.");
		}
	}

	public ShapefileWorkspaceFactory initializeShapefileWorkspace() throws IOException {
		return (ShapefileWorkspaceFactory) context.createObject(ShapefileWorkspaceFactory.getClsid());
	}
	
	public final File getWorkspace(){
		return this.workspaceDir;
	}

	public static void shutdown() {
		try {
			context.releaseContext();
		} catch (AutomationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public Object createObject(String clsid) throws IOException {
		return context.createObject(clsid);
	}

	public boolean isReady() {
		try {
			return this.initializeShapefileWorkspace() != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	private SpatialRelationFeaturePair createRelationFeaturePair(List<File> shapeFiles) throws IOException {
		return new SpatialRelationFeaturePair(shapeFiles);
	}

	public boolean evaluateSpatialRelation(SpatialRelation within,
			List<File> createShapefilesForFeatures) throws IOException {
		SpatialRelationFeaturePair pair = createRelationFeaturePair(createShapefilesForFeatures);
		
		switch (within) {
		case CONTAINS:
			return pair.contains();
		case COVERS:
			return pair.covers();
		case CROSSES:
			return pair.crosses();
		case OVERLAPS:
			return pair.overlaps();
		case EQUALS:
			return pair.equals();
		case WITHIN:
			return pair.within();
		case INTERSECTS:
			return !pair.disjoint();
		case DISJOINT:
			return pair.disjoint();
		case TOUCHES:
			return pair.touches();
		default:
			return false;
		}
	}


}
