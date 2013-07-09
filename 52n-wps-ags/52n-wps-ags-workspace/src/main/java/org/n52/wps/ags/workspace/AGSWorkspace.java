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
import java.util.List;

import org.slf4j.Logger;
import org.n52.wps.ags.workspace.ServerContextFactory.LockedServerContext;
import org.n52.wps.ags.workspace.feature.SpatialRelation;
import org.n52.wps.ags.workspace.feature.SpatialRelationFeaturePair;

import com.esri.arcgis.geoprocessing.GeoProcessor;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerContext;
import com.esri.arcgis.system.IVariantArray;
import com.esri.arcgis.system.VarArray;


public class AGSWorkspace {

	private static Logger LOGGER = LoggerFactory.getLogger(AGSWorkspace.class);
	private final File workspaceDir;


	public AGSWorkspace(File workspace){
		workspaceDir = workspace;
	}



	public final void executeGPTool(String toolName, String toolboxPath, String[] parameters) throws IOException {
		LockedServerContext context = null;
		try {
			context = ServerContextFactory.retrieveContext();
			GeoProcessor gp = (GeoProcessor) (context.getContext().createObject(GeoProcessor.getClsid()));

			IVariantArray paramsPreparedForGeoProcessor = (VarArray)(context.getContext().createObject(VarArray.getClsid()));
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
		} finally {
			if (context != null) 
				ServerContextFactory.returnContext(context);
		}
	}


	public final File getWorkspace(){
		return this.workspaceDir;
	}

	public static void shutdown() {
		try {
			ServerContextFactory.releaseAllCachedContexts();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public Object createObject(String clsid, IServerContext context) throws IOException {
		return context.createObject(clsid);
	}

	public boolean isReady() throws IOException {
		LockedServerContext context = ServerContextFactory.retrieveContext();
		boolean result = context.getContext() != null;
		ServerContextFactory.returnContext(context);
		return result;
	}



	private SpatialRelationFeaturePair createRelationFeaturePair(List<File> shapeFiles) throws IOException {
		return new SpatialRelationFeaturePair(shapeFiles);
	}

	public boolean evaluateSpatialRelation(SpatialRelation relation,
			List<File> createShapefilesForFeatures) throws IOException {
		SpatialRelationFeaturePair pair = createRelationFeaturePair(createShapefilesForFeatures);

		boolean result = false;

		switch (relation) {
		case CONTAINS:
			result = pair.contains();
			break;
		case COVERS:
			result = pair.covers();
			break;
		case CROSSES:
			result = pair.crosses();
			break;
		case OVERLAPS:
			result = pair.overlaps();
			break;
		case EQUALS:
			result = pair.equals();
			break;
		case WITHIN:
			result = pair.within();
			break;
		case INTERSECTS:
			result = !pair.disjoint();
			break;
		case DISJOINT:
			result = pair.disjoint();
			break;
		case TOUCHES:
			result = pair.touches();
			break;
		}

		pair.releaseContext();
		return result;
	}


}
