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
import java.util.List;

import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IRelationalOperator;


/**
 * This class provides spatial relation computation
 * capabilities. It utilizies ArcObjects internal {@link IRelationalOperator}
 * in combination with {@link IGeometry}.
 * 
 * @author matthes rieke, Matthias Mueller, TU Dresden; Christin Henzen, TU Dresden
 *
 */
public class SpatialRelationFeaturePair {
	
	IRelationalOperator relationFeature;
	IGeometry geometryFeature;
	private FeatureAccess accessToShapefile;
	
	public SpatialRelationFeaturePair(List<File> shapeFiles) throws IOException {
		// TODO check if context is needed when evaluating the relation! otherwise, release it directly
		accessToShapefile = new FeatureAccess();
		this.relationFeature = accessToShapefile.resolveRelationalOperatorFromShapefile(shapeFiles.get(0));
		this.geometryFeature = accessToShapefile.resolveGeometryFromShapefile(shapeFiles.get(1));
	}
	
	public SpatialRelationFeaturePair(IRelationalOperator rf, IGeometry gf) {
		this.relationFeature = rf;
		this.geometryFeature = gf;
	}

	public boolean disjoint() throws IOException {
		return relationFeature.disjoint(geometryFeature);
	}
	
	public boolean crosses() throws IOException {
		return relationFeature.crosses(geometryFeature);
	}

	public boolean overlaps() throws IOException {
		return relationFeature.overlaps(geometryFeature);
	}
	
	public boolean contains() throws IOException {
		return relationFeature.contains(geometryFeature);
	}
	
	public boolean equals() throws IOException {
		return relationFeature.equals(geometryFeature);
	}
	
	public boolean touches() throws IOException {
		return relationFeature.touches(geometryFeature);
	}
	
	public boolean within() throws IOException {
		return relationFeature.within(geometryFeature);
	}
	
	public boolean covers() throws IOException {
		return relationFeature.relation(geometryFeature, "RELATE (G1, G2, 'TTTFTTFFT')");
	}

	public void releaseContext() {
		accessToShapefile.releaseContext();
	}
	
}