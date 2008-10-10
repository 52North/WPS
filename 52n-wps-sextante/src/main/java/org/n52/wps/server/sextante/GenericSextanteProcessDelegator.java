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

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.server.IAlgorithm;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;

public class GenericSextanteProcessDelegator implements IAlgorithm {
	private static Logger LOGGER = Logger.getLogger(GenericSextanteProcessDelegator.class);
	
	private String processID;
	private ProcessDescriptionType processDescription;
	private String errors;
	
	
	public GenericSextanteProcessDelegator(String processID, File file) {
		this.processID = processID;
		errors = "";
		try {
			processDescription = ProcessDescriptionType.Factory.parse(file);
		} catch (XmlException e) {
			LOGGER.error("Could not initialzize WPS Sextante Process " +processID);
			e.printStackTrace();
			errors = "Could not initialzize WPS Sextante Process " +processID;
			throw new RuntimeException("Could not initialzize WPS Sextante Process " +processID);
		} catch (IOException e) {
			LOGGER.error("Could not initialzize WPS Sextante Process " +processID);
			e.printStackTrace();
			errors = "Could not initialzize WPS Sextante Process " +processID;
			throw new RuntimeException("Could not initialzize WPS Sextante Process " +processID);
			
		}
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
			Class<GeoAlgorithm> processClass = (Class<GeoAlgorithm>) Class.forName("es.unex.sextante.vectorTools.linesToEquispacedPoints.LinesToEquispacedPointsAlgorithm");
			GeoAlgorithm sextanteProcess = processClass.newInstance();
			
			
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
				parameter.setParameterValue(wrappedInput);
				
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
			 OutputFactory outputFactory = null;//new GTOutputFactory();
	         OutputObjectsSet outputs = sextanteProcess.getOutputObjects();
	         
	         int outputDataCount = outputs.getOutputDataObjectsCount();
	 		 for(int i = 0; i<outputDataCount; i++){
	 			Output outputObject = outputs.getOutput(i);
	 			String name = outputObject.getName();
	 			
	 			//TODO extract filename from config
	 			//how do I know the output format?
	 			outputObject.setFilename("");
	 			
	 			
	 			
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
			}
		       
		return resultMap;
	}

	private Object wrapSextanteInputs(Parameter parameter, Map layers, Map wpsParameters, String parameterName,	String type) {
		if(type.equals("Vector Layer")){
			Object vectorLayer = layers.get(parameterName);
		
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
			return wrapRasterLayer(rasterLayer);
			
			
		}else if (type.equals("Numerical Value")){
			return wpsParameters.get(parameterName);
			
			
		}//TODO extend
		throw new RuntimeException("Input paramter " + parameterName +"could not be resolved");
		
	}

	private Object unwrapSextanteResults(Output outputObject) {
		Object result = outputObject.getOutputObject();
		if(result instanceof OutputVectorLayer){
			//TODO turn it into a featurecollection or the requested format
		}else if (result instanceof OutputRasterLayer){
			//TODO turn it into a GT raster object or the requested format
		}
			
		return null;
	}

	private Object wrapRasterLayer(Object rasterLayer) {
		// TODO 
		return null;
	}

	private Object wrapVectorLayer(Object vectorLayer) {
		// TODO 
		return null;
	}

}
