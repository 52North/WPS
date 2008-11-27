
/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.sextante;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.impl.ExecuteDocumentImpl;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.server.IAlgorithm;
import org.opengis.coverage.grid.GridCoverage;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTRasterLayer;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.geotools.PostProcessStrategy;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;

public class GenericSextanteProcessDelegator implements IAlgorithm {
	private static Logger LOGGER = Logger.getLogger(GenericSextanteProcessDelegator.class);
	
	private String processID;
	private ProcessDescriptionType processDescription;
	private String errors;
	
	
	public GenericSextanteProcessDelegator(String processID, ProcessDescriptionType processDescriptionType) {
		this.processID = processID.replace("Sextante_","");;
		this.errors = "";
		this.processDescription = processDescriptionType;
		
	}

	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	public String getErrors() {
		return errors;
	}

	public String getWellKnownName() {
		return processID;
	}

	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	public Map run(Map layers, Map wpsParameters) {
		
		Map resultMap = new HashMap();
		/*
		 * 1. Extract the Sextante Classname out of the processIdentifier. 
		 * It can be assumed that the id is also the classname.
		 * 
		*/
		
		
		
		try {
			
			GeoAlgorithm sextanteProcess = Sextante.getAlgorithmFromCommandLineName(processID);
			
			 /* 
			 * 2. Get the parameters needed from either the processdescription or the object itself
			 * e.g.
			 * ParametersSet params = alg.getParameters();
			 * 
			 * 
			 */
			ParametersSet parameterSet = sextanteProcess.getParameters();
			
			
			int numberOfParameters = parameterSet.getNumberOfParameters();
			for(int i = 0; i <numberOfParameters; i++){
				Parameter parameter = parameterSet.getParameter(i);
				String parameterName = parameter.getParameterName();
				
				String type = parameter.getParameterTypeName();
				
				/* 3. Wrap the input from layers into a Sextante known format based on the metadata. 
				 * e.g.
				 * if a vectorlayer is required, do something like:
				 * GTVectorLayer layer =GTVectorLayer.createLayer(ds, ds.getTypeNames()[0]);
				 * 
				 * we probably have to refactor the input stuff and add some metadata to know what koind of input is fed in (for instance vector or raster etc)
				 */
				Object wrappedInput = wrapSextanteInputs(parameter, layers, wpsParameters, parameterName, type);
				if(wrappedInput!=null){
					parameter.setParameterValue(wrappedInput);
				}
			}
			
			 /* 5. Specify the output
	         * e.g.
	         * OutputFactory outputFactory = new GTOutputFactory();
	         * OutputObjectsSet outputs = alg.getOutputObjects();
	         * Output contours = outputs.getOutput(LinesToEquispacedPointsAlgorithm.RESULT);
	         * contours.setFilename("/home/my_user_name/points.shp");
	         */
			 
			//TODO eventually make the outputfactory dynamic based on the requested output type
			//until now, only geotools is supported, which may also supports different formats-->please check.
			 OutputFactory outputFactory = new N52OutputFactory();
	         OutputObjectsSet outputs = sextanteProcess.getOutputObjects();
	        
	         int outputDataCount = outputs.getOutputDataObjectsCount();
	 		 for(int i = 0; i<outputDataCount; i++){
	 			Output outputObject = outputs.getOutput(i);
	 			String name = outputObject.getName();
	 			
	 			 			
	 	         /* 6. Execute
	 	         * e.g.
	 	         * alg.execute(null, outputFactory);
	 	         */
	 			
	 			sextanteProcess.execute(null, outputFactory);
	 			
	 			/* 7. Fetch the results
	 	         * e.g.
	 	         * IVectorLayer result = (IVectorLayer) contours.getOutputObject();
	 	         * and Unwrap it into geotools
	 	         * e.g.
	 	         * FeatureStore fs = (FeatureStore) result.getBaseDataObject();
	 	         */
	 			 Object finalResult = unwrapSextanteResults(outputObject);
	 			 /* 9. Fill the result hashmap
	 	         */ 
	 			 resultMap.put(name, finalResult);
	 	        
	 		 }
	 		 
	 		/* 10. return the results
 	        */
	 		 
			//TODO Error handling
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (WrongOutputIDException e) {
				e.printStackTrace();
			} catch (GeoAlgorithmExecutionException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		       
		return resultMap;
	}

	private Object wrapSextanteInputs(Parameter parameter, Map layers, Map wpsParameters, String parameterName,	String type) throws IOException {
		if(type.equals("Vector Layer")){
			Object vectorLayer = layers.get(parameterName);
			if(vectorLayer==null){
				return null;
			}
			/* 4. Fill the input parameters with the wps input
			 * e.g. 
			 * params.getParameter(LinesToEquispacedPointsAlgorithm.LINES).setParameterValue(layer);
	         * params.getParameter(LinesToEquispacedPointsAlgorithm.DISTANCE).setParameterValue(new Double(5000));
	         *
	         */
			return wrapVectorLayer(vectorLayer);
			
		}
		else if (type.equals("Raster Layer")) {
			Object rasterLayer = layers.get(parameterName);
			if(rasterLayer==null){
				return null;
			}
			return wrapRasterLayer(rasterLayer);
			
			
		}else if (type.equals("Numerical Value")){
			return wpsParameters.get(parameterName);
			
			
		}else if (type.equals("Table Field")){
			Object param = wpsParameters.get(parameterName);
			if(param == null){
				parameter.setParameterValue(0);
				
			}
			return parameter;
			
		}else if (type.equals("Selection")){
			Object param = wpsParameters.get(parameterName);
			return param;
			
		}else if (type.equals("Boolean")){
			Object param = wpsParameters.get(parameterName);
			if(param == null){
				param = false;
			}
			return param;
			
		}
		return null;
		
	}

	private Object unwrapSextanteResults(Output outputObject) throws Exception {
		Object result = outputObject.getOutputObject();
		if(result instanceof IVectorLayer){
			
			IVectorLayer vectorLayer = ((IVectorLayer)result);
			FeatureStore fs = (FeatureStore) vectorLayer.getBaseDataObject();
			return fs.getFeatures();
			
		}else if (result instanceof IRasterLayer){
			IRasterLayer rasterLayer = ((IRasterLayer)result);
			GridCoverage coverage = (GridCoverage) rasterLayer.getBaseDataObject();
			return coverage;
		}
			
		return null;
	}

	private GTRasterLayer wrapRasterLayer(Object rasterLayer) {
		// TODO 
		return null;
	}

	private GTVectorLayer wrapVectorLayer(Object vectorLayer) throws IOException {
		FeatureCollection fc  = (FeatureCollection) vectorLayer;
		DataStore datastore = new CollectionDataStore(fc);
		GTVectorLayer gtVectorLayer =  GTVectorLayer.createLayer(datastore, datastore.getTypeNames()[0]);
		gtVectorLayer.setPostProcessStrategy(new NullStrategy());
		return gtVectorLayer;
	}

}
