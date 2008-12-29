
/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany
	Victor Olaya, Universtity of Jaume, Spain

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.server.IAlgorithm;
import org.opengis.coverage.grid.GridCoverage;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.geotools.GTRasterLayer;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;

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
				if(wrappedInput !=null){
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
	 			 if(finalResult==null){
	 				 throw new RuntimeException("Error while executing process " + processID + ". Sextante Results are null");
	 			 }
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

	private Object wrapSextanteInputs(Parameter parameter, Map layers, Map wpsParameters, String parameterName,	String type) throws IOException, NullParameterAdditionalInfoException {
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
			
			
		}else if (type.equals("Numerical Value") || type.equals("String")){
 			return wpsParameters.get(parameterName);
		}else if (type.equals("Multiple Input")){
			return createMultipleInputArray(parameter, layers);
		}else if (type.equals("Selection")){			
			Object param = wpsParameters.get(parameterName);
			if(param instanceof String){
				AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
				String[] values = ai.getValues();
				for(int i = 0; i<values.length;i++){
					if(values[i].equals((String)param)){
						return new Integer(i);
					}
				}
			}
			
			if(param instanceof Integer){
				return param;
			}
			else{
				return null;
			}
			
		}else if (type.equals("Selection")){
			Object parameterType = parameter.getParameterValueAsObject();
			Object param = null;
			if(parameterType instanceof String){
				param = wpsParameters.get(parameterName);
			}
			if(parameterType instanceof Integer){
				if(wpsParameters.get(parameterName)==null){
					return null;
				}
				param =  new Integer((String)wpsParameters.get(parameterName));
				//mapping
				AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
				String[] values = ai.getValues();
				for(int i = 0; i<values.length;i++){
					if(values[i].equals(param)){
						param = new Integer(i);
						break;
					}
				}
			}
			return param;
			
		}else if (type.equals("Boolean")){
			Object param = wpsParameters.get(parameterName);
			if(param == null){
				param = false;
			}
			return param;
			
		}
		
		else if (type.equals("Fixed Table")){
			boolean bIsNumberOfRowsFixed;
			int iCols, iRows;
			int iCol, iRow;
			int iToken = 0;
			FixedTableModel tableModel;
			Object value = layers.get(parameterName);
			if(value==null){
				return null;
			}
			String sValue = value.toString();
			StringTokenizer st = new StringTokenizer(sValue, ",");
			String sToken;
			AdditionalInfoFixedTable ai;
			ai = (AdditionalInfoFixedTable) parameter.getParameterAdditionalInfo();
			iCols = ai.getColsCount();
			iRows = (int) (st.countTokens() / iCols) + 1;
			bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();
			tableModel = new FixedTableModel(ai.getCols(), iRows, bIsNumberOfRowsFixed);
			if (bIsNumberOfRowsFixed){
				if (iRows != ai.getRowsCount()){
					return null;
				}
			}
			else{
				if (st.countTokens() % iCols != 0){
					return null;
				}
			}
			
			while (st.hasMoreTokens()){
				iRow =  (int) Math.floor(iToken / (double) iCols);
				iCol = iToken % iCols;
				sToken = st.nextToken().trim();
				tableModel.setValueAt(sToken, iRow, iCol);
				iToken++;
			}
			
			return tableModel;
		}
		return null;
		
	}

	private Object unwrapSextanteResults(Output outputObject) throws Exception {
		Object result = outputObject.getOutputObject();
		if(result instanceof IVectorLayer){
			
			IVectorLayer vectorLayer = ((IVectorLayer)result);
			FeatureStore fs = (FeatureStore) vectorLayer.getBaseDataObject();
			return (FeatureCollection)fs.getFeatures();
			
		}else if (result instanceof IRasterLayer){
			IRasterLayer rasterLayer = ((IRasterLayer)result);
			GridCoverage coverage = (GridCoverage) rasterLayer.getBaseDataObject();
			return coverage;
		}
			
		return null;
	}

	private GTRasterLayer wrapRasterLayer(Object rasterLayer) {
		if(!(rasterLayer instanceof GridCoverage)){
			return null;
		}
		GridCoverage coverage = (GridCoverage) rasterLayer;
		GTRasterLayer sextanteRasterLayer = new GTRasterLayer();
		sextanteRasterLayer.create(coverage);
		
		return sextanteRasterLayer;
		
	}

	private GTVectorLayer wrapVectorLayer(Object vectorLayer) throws IOException {
		if(!(vectorLayer instanceof FeatureCollection)){
			return null;
		}
		FeatureCollection fc  = (FeatureCollection) vectorLayer;
		DataStore datastore = new CollectionDataStore(fc);
		GTVectorLayer gtVectorLayer =  GTVectorLayer.createLayer(datastore, datastore.getTypeNames()[0]);
		gtVectorLayer.setPostProcessStrategy(new NullStrategy());
		gtVectorLayer.setName("VectorLayer");
		return gtVectorLayer;
	}
	
	private ArrayList createMultipleInputArray(Parameter parameter, Map layers)
					throws NullParameterAdditionalInfoException, IOException{
			String parameterName = parameter.getParameterName();
			Object layer = layers.get(parameterName);
			ArrayList list = new ArrayList();
			AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput)parameter.getParameterAdditionalInfo();
			switch (ai.getDataType()){
				case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
					wrapRasterLayer(layer);
					break;
				case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
					wrapVectorLayer(layer);
					break;
				case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
					wrapVectorLayer(layer);
					break;
				case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
					wrapVectorLayer(layer);
					break;
				case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
					wrapVectorLayer(layer);
					break;		
				default:
			}
	
			return list;
	
	 	}

}
