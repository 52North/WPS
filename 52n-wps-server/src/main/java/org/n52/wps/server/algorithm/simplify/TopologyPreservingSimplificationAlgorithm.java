/***************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server.algorithm.simplify;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAlgorithm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class TopologyPreservingSimplificationAlgorithm extends
		AbstractAlgorithm {

	private List<String> errors = new ArrayList<String>();
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		if(inputData==null || !inputData.containsKey("FEATURES")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> dataList = inputData.get("FEATURES");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
				
		FeatureCollection featureCollection = ((GTVectorDataBinding) firstInputData).getPayload();
		FeatureIterator iter = featureCollection.features();
		
		if( !inputData.containsKey("width")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> widthDataList = inputData.get("TOLERANCE");
		if(widthDataList == null || widthDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		Double tolerance = ((LiteralDoubleBinding) widthDataList.get(0)).getPayload();
		while(iter.hasNext()) {
			Feature f = iter.next();
			Object userData = f.getDefaultGeometry().getUserData();
			
			try{
				Geometry in = f.getDefaultGeometry();
				Geometry out = TopologyPreservingSimplifier.simplify(in, tolerance);
                /*
                 * THIS PASSAGE WAS CONTRIBUTED BY GOBE HOBONA.
                 *The simplification of MultiPolygons produces Polygon geometries. This becomes inconsistent with the original schema (which was of MultiPolygons).
                 *To ensure that the output geometries match that of the original schema we add the Polygon(from the simplication) to a MultiPolygon object
                 *
                 *This is issue is known to affect MultiPolygon geometries only, other geometries need to be tested to ensure conformance with the original (input) schema
                 */  
                if(in.getGeometryType().equals("MultiPolygon") && out.getGeometryType().equals("Polygon"))
                {                   
                    MultiPolygon mp = (MultiPolygon)in;                                               
                    Polygon[] p = {(Polygon)out};
                    mp = new MultiPolygon(p,mp.getFactory());                   
                    f.setDefaultGeometry(mp);
                }
                else if(in.getGeometryType().equals("MultiLineString") && out.getGeometryType().equals("LineString")) {
                	MultiLineString ml = (MultiLineString)in;
                	LineString[] l = {(LineString)out};
                    ml = new MultiLineString(l,ml.getFactory());                   
                    f.setDefaultGeometry(ml);
                }
                else
                f.setDefaultGeometry(out);				
				f.getDefaultGeometry().setUserData(userData);
			}
			catch(IllegalAttributeException e) {
				throw new RuntimeException("geometrytype of result is not matching", e);
			}
		}
		HashMap<String, IData> result = new HashMap<String, IData>();
		result.put("SIMPLIFIED_FEATURES", new GTVectorDataBinding(featureCollection));
		return result;
	}

	public List<String> getErrors() {
		return errors;
	}

	public Class getInputDataType(String id) {
		if(id.equalsIgnoreCase("FEATURES")){
			return GTVectorDataBinding.class;
		}else if(id.equalsIgnoreCase("TOLERANCE")){
			return LiteralDoubleBinding.class;
		}
		return null;
	}

	public Class getOutputDataType(String id) {
		if(id.equalsIgnoreCase("result")){
			return GTVectorDataBinding.class;
		}
		return null;
	}


	

}
