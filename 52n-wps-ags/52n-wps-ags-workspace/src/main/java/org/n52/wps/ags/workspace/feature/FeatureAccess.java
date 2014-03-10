/***************************************************************
Copyright (C) 2009-2013
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.ags.workspace.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.ags.workspace.ServerContextFactory;
import org.n52.wps.ags.workspace.ServerContextFactory.LockedServerContext;

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

/**
 * Convenience class to provide access to features
 * stored in a shapefile.
 * 
 * @author matthes rieke, Matthias Mueller, TU Dresden; Christin Henzen, TU Dresden
 *
 */
public class FeatureAccess {

	private LockedServerContext context;
	
	public FeatureAccess() throws IOException {
		this.context = ServerContextFactory.retrieveContext();
	}

	
	public IRelationalOperator resolveRelationalOperatorFromShapefile(File inputFeature) throws IOException {
		IGeometry result = resolveGeometryFromShapefile(inputFeature);
		if (result instanceof IRelationalOperator) {
			return (IRelationalOperator) result;
		}
		return null;
	}
	
	public IGeometry resolveGeometryFromShapefile(File inputFeature) throws IOException {
		String name = inputFeature.getName();
		ShapefileWorkspaceFactory shpWorkspace = (ShapefileWorkspaceFactory) this.context.getContext().createObject(
				ShapefileWorkspaceFactory.getClsid());
		Workspace srcWorkspace = new Workspace(shpWorkspace.openFromFile(inputFeature.getParent(), 0));
		IFeatureClass srcFeatureClass = srcWorkspace.openFeatureClass(removeFileExtension(name));

		List<IGeometry> geometries = resolveGeometriesFromFeatureClass(srcFeatureClass);

		if (geometries.size() == 1) return geometries.get(0);
		
		return (IGeometry) createUnionFromGeometries(geometries);
	}

	private List<IGeometry> resolveGeometriesFromFeatureClass(IFeatureClass srcFeatureClass) throws IOException {
		ArrayList<IGeometry> result = new ArrayList<IGeometry>();
		
		IFeatureCursor cur = srcFeatureClass.search(null, false);
		IFeature feature = cur.nextFeature();
		while (feature != null) {
			result.add(feature.getShape());
			feature = cur.nextFeature();
		}
		
		return result;
	}
	
	private IGeometry createUnionFromGeometries(List<IGeometry> geometries) throws IOException {
		ITopologicalOperator resultPolygon = (ITopologicalOperator) this.context.getContext().createObject(Polygon.getClsid());
		IGeometryCollection geometriesToUnion = (IGeometryCollection) this.context.getContext().createObject(GeometryBag.getClsid());
		
		for (IGeometry geom : geometries) {
			geometriesToUnion.addGeometry(geom, null, null);
		}
		resultPolygon.constructUnion((IEnumGeometry) geometriesToUnion);
		
		return (IGeometry) resultPolygon;
	}


	private String removeFileExtension(String name) {
		int index = name.lastIndexOf(".");
		return name.substring(0, index);
	}
	
	public void releaseContext() {
		ServerContextFactory.returnContext(this.context);
	}


}
