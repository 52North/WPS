
/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework
is extensible in terms of processes and data handlers.

 Copyright (C) 2006 by con terra GmbH

 Authors:
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany
	Victor Olaya, Universtity of Extremadura, Spain

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

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.FileDataBinding;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.IAlgorithm;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.geotools.GTRasterLayer;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class GenericSextanteProcessDelegator implements IAlgorithm, SextanteConstants {

	private static Logger LOGGER = LoggerFactory.getLogger(GenericSextanteProcessDelegator.class);

	private String processID;
	private ProcessDescriptionType processDescription;
	private List<String> errors;
	private GeoAlgorithm sextanteProcess;


	public GenericSextanteProcessDelegator(String processID, ProcessDescriptionType processDescriptionType) {
		this.processID = processID.replace("Sextante_","");;
		errors = new ArrayList<String>();
		this.processDescription = processDescriptionType;
		sextanteProcess = Sextante.getAlgorithmFromCommandLineName(processID);

	}

	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getWellKnownName() {
		return processID;
	}

	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		Map<String, IData> resultMap = new HashMap<String, IData>();
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
				
				boolean missingMandatoryParameter = false;
				
				if (parameter instanceof ParameterRasterLayer){
					AdditionalInfoRasterLayer ai = (AdditionalInfoRasterLayer) parameter.getParameterAdditionalInfo();
					if (ai.getIsMandatory() && (inputData.get(parameterName) == null) ){
						missingMandatoryParameter = true;
					}
				}
				else if (parameter instanceof ParameterVectorLayer){
					AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) parameter.getParameterAdditionalInfo();
					if (ai.getIsMandatory() && (inputData.get(parameterName) == null) ){
						missingMandatoryParameter = true;
					}

				}else if (parameter instanceof ParameterMultipleInput) {
					AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) parameter
							.getParameterAdditionalInfo();
					if (ai.getIsMandatory() && (inputData.get(parameterName) == null) ){
						missingMandatoryParameter = true;
					}
				}
				
				if(missingMandatoryParameter){
					LOGGER.error("Missing parameter: " + parameterName);
					throw new RuntimeException("Error while executing process " + processID + ". Missing parameter: " + parameterName);
				}	
				if(!(inputData.get(parameterName) == null)){
					Object wrappedInput = wrapSextanteInputs(parameter, inputData.get(parameterName), parameterName, type);

					if(wrappedInput !=null){
						parameter.setParameterValue(wrappedInput);
					}
				}
			}

			/* 4. Adjust output grid extent if needed */
			if (sextanteProcess.getUserCanDefineAnalysisExtent()){
				AnalysisExtent ge = null;
				try{
					ge = getGridExtent(
							(Double)inputData.get( GRID_EXTENT_X_MIN).get(0).getPayload(),
							(Double)inputData.get(GRID_EXTENT_X_MAX).get(0).getPayload(),
							(Double)inputData.get(GRID_EXTENT_Y_MIN).get(0).getPayload(),
							(Double)inputData.get(GRID_EXTENT_Y_MAX).get(0).getPayload(),
							(Double)inputData.get(GRID_EXTENT_CELLSIZE).get(0).getPayload());
				}
				catch(Exception e){}
				sextanteProcess.setAnalysisExtent(ge);
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
	         /* 6. Execute
	 	         * e.g.
	 	         * alg.execute(null, outputFactory);
	 	         */

	 		sextanteProcess.execute(null, outputFactory);
	         
	 		OutputObjectsSet outputs = sextanteProcess.getOutputObjects();

	         int outputDataCount = outputs.getOutputDataObjectsCount();
	 		 for(int i = 0; i<outputDataCount; i++){
	 			Output outputObject = outputs.getOutput(i);
	 			String name = outputObject.getName();


	 			/* 7. Fetch the results
	 	         * e.g.
	 	         * IVectorLayer result = (IVectorLayer) contours.getOutputObject();
	 	         * and Unwrap it into geotools
	 	         * e.g.
	 	         * FeatureStore fs = (FeatureStore) result.getBaseDataObject();
	 	         */
	 			 IData finalResult = unwrapSextanteResults(outputObject);
	 			 if(finalResult==null){
	 				 throw new RuntimeException("Error while executing process " + processID + ". Sextante Results are null");
	 			 }
	 			 /* 9. Fill the result hashmap
	 	         */

	 			 resultMap.put(name, finalResult);

	 		 }

	 		/* 10. return the results
 	        */


			} catch (InstantiationException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ".");
			} catch (IllegalAccessException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ".");
			} catch (ClassNotFoundException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ". ");
			} catch (WrongOutputIDException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ". ");
			} catch (GeoAlgorithmExecutionException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ".");
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ".");
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new RuntimeException("Error while executing process " + processID + ".");
			}

		return resultMap;
	}


	private AnalysisExtent getGridExtent(double xMin, double xMax,
									double yMin, double yMax,
									double cellSize){

		AnalysisExtent ge = new AnalysisExtent();
		ge.setCellSize(cellSize);
		ge.setXRange(xMin, xMax, true);
		ge.setYRange(yMin, yMax, true);

		return ge;

	}

	private Object wrapSextanteInputs(Parameter parameter, List<IData> wpsInputParameters , String parameterName,	String type) throws IOException, NullParameterAdditionalInfoException {
		if(type.equals("Vector Layer") && wpsInputParameters.size() == 1){
			IData vectorLayer = wpsInputParameters.get(0);
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
		else if (type.equals("Raster Layer")&& wpsInputParameters.size() == 1) {
			IData rasterLayer = wpsInputParameters.get(0);;
			if(rasterLayer==null){
				return null;
			}
			return wrapRasterLayer(rasterLayer);


		}else if (type.equals("Numerical Value") || type.equals("String")
					|| type.equals("Band") || type.equals("Table Field")  && wpsInputParameters.size() == 1){
 			return wpsInputParameters.get(0).getPayload();
		}else if (type.equals("Multiple Input")){
			return createMultipleInputArray(parameter, wpsInputParameters);
		}else if (type.equals("Selection") && wpsInputParameters.size() == 1){
				IData param = wpsInputParameters.get(0);
				if(param.getSupportedClass().equals(String.class)){
					AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
					String[] values = ai.getValues();
					for(int i = 0; i<values.length;i++){
						if(values[i].equals(param.getPayload())){
							return new Integer(i);
						}
					}
				}

				/*if(param.getSupportedClass().equals(String.class)){
					AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
					String[] values = ai.getValues();
					for(int i = 0; i<values.length;i++){
						if(values[i].equals(param.getPayload())){
							return new Integer(i);
						}
					}
				}*/
				else{
					return null;
				}

		}else if (type.equals("Boolean") && wpsInputParameters.size() == 1){
			IData param = wpsInputParameters.get(0);
			if(param == null){
				return false;
			}
			return param.getPayload();

		}else if (type.equals("Point") && wpsInputParameters.size() == 1){
			IData param = wpsInputParameters.get(0);
			if(param == null){
				return false;
			}
			String[] sValue = param.getPayload().toString().split(",");
			return new Point2D.Double(Double.parseDouble(sValue[0]),
									  Double.parseDouble(sValue[1]));

		}

		else if (type.equals("Fixed Table") && wpsInputParameters.size() == 1){
			boolean bIsNumberOfRowsFixed;
			int iCols, iRows;
			int iCol, iRow;
			int iToken = 0;
			FixedTableModel tableModel;
			IData param = wpsInputParameters.get(0);
			if(param==null){
				return null;
			}
			String sValue = param.getPayload().toString();
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

	private IData unwrapSextanteResults(Output outputObject) throws Exception {
		Object result = outputObject.getOutputObject();
		if(result instanceof IVectorLayer){

			IVectorLayer vectorLayer = ((IVectorLayer)result);
			vectorLayer.open();
			FeatureStore<?, ?> fs = (FeatureStore<?, ?>) vectorLayer.getBaseDataObject();
			return new GTVectorDataBinding(fs.getFeatures());

		}else if (result instanceof IRasterLayer){
			IRasterLayer rasterLayer = ((IRasterLayer)result);
			GridCoverage coverage = (GridCoverage) rasterLayer.getBaseDataObject();
			return new GTRasterDataBinding((GridCoverage2D)coverage);
		}else if(result instanceof ITable){
			FileOutputChannel outputChannel = (FileOutputChannel) outputObject.getOutputChannel();
			File output = new File(outputChannel.getFilename());
			return new FileDataBinding(output);
		}
		//TODO Extend for literal output

		return null;
	}

	private GTRasterLayer wrapRasterLayer(IData rasterLayer) {
		if(!(rasterLayer.getPayload() instanceof GridCoverage)){
			return null;
		}
		GridCoverage coverage = (GridCoverage) rasterLayer.getPayload();
		GTRasterLayer sextanteRasterLayer = new GTRasterLayer();
		sextanteRasterLayer.create(coverage);

		return sextanteRasterLayer;

	}

	private GTVectorLayer wrapVectorLayer(IData vectorLayer) throws IOException {
		if(!(vectorLayer.getPayload() instanceof FeatureCollection)){
			return null;
		}
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc  = (FeatureCollection<SimpleFeatureType, SimpleFeature>) vectorLayer.getPayload();
		DataStore datastore = new CollectionDataStore(fc);
		FeatureSource<?, ?> fsource = datastore.getFeatureSource(datastore.getTypeNames()[0]);		
		GTVectorLayer gtVectorLayer = new GTVectorLayer();
//		NoPostprocessingGTVectorLayer gtVectorLayer = new NoPostprocessingGTVectorLayer();
		gtVectorLayer.create(fsource);
//		GTVectorLayer gtVectorLayer =  GTVectorLayer.createLayer(datastore, datastore.getTypeNames()[0]);
//		gtVectorLayer.setPostProcessStrategy(new NullStrategy());
		gtVectorLayer.setName("VectorLayer");
		return gtVectorLayer;
	}

	private ArrayList<ILayer> createMultipleInputArray(Parameter parameter, List<IData> wpsInputParameters)
					throws NullParameterAdditionalInfoException, IOException{
			ArrayList<ILayer> list = new ArrayList<ILayer>();
			for(IData data : wpsInputParameters){
				AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput)parameter.getParameterAdditionalInfo();
				switch (ai.getDataType()){
					case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
						list.add(wrapRasterLayer(data));
						break;
					case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
						list.add(wrapVectorLayer(data));
						break;
					case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
						list.add(wrapVectorLayer(data));
						break;
					case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
						list.add(wrapVectorLayer(data));
						break;
					case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
						list.add(wrapVectorLayer(data));
						break;
					default:
				}
			}

			return list;

	 	}

	public Class<?> getInputDataType(String id) {
		ParametersSet parameterSet = sextanteProcess.getParameters();

		int numberOfParameters = parameterSet.getNumberOfParameters();
		for(int i = 0; i <numberOfParameters; i++){
			Parameter parameter = parameterSet.getParameter(i);
			String parameterName = parameter.getParameterName();
			
			if(!parameterName.equals(id)){
				continue;
			}
			String type = parameter.getParameterTypeName();

			if(type.equals("Vector Layer")){
				return GTVectorDataBinding.class;
			}
			else if (type.equals("Raster Layer")) {
				return GTRasterDataBinding.class;
			}else if (type.equals("Numerical Value")){
				return LiteralDoubleBinding.class;
			}else if (type.equals("String")){
				return LiteralStringBinding.class;
			}else if (type.equals("Multiple Input")){
				InputDescriptionType[] inputs = processDescription.getDataInputs().getInputArray();
				for(InputDescriptionType input : inputs){
					if(input.getIdentifier().getStringValue().equals(id)){
						if(input.isSetLiteralData()){
							String datatype = input.getLiteralData().getDataType().getStringValue();
							if(datatype.contains("tring")){
									return LiteralStringBinding.class;
							}
							if(datatype.contains("ollean")){
								return LiteralBooleanBinding.class;
							}
							if(datatype.contains("loat") || datatype.contains("ouble")){
								return LiteralDoubleBinding.class;
							}
							if(datatype.contains("nt")){
								return LiteralIntBinding.class;
							}
						}
						if(input.isSetComplexData()){
							 String mimeType = input.getComplexData().getDefault().getFormat().getMimeType();
							 if(mimeType.contains("xml") || (mimeType.contains("XML"))){
								 return GTVectorDataBinding.class;
							 }else{
								 return GTRasterDataBinding.class;
							 }
						}
					}
				}

			}else if (type.equals("Selection")){
					return LiteralIntBinding.class;
			}else if (type.equals("Boolean")){
				return LiteralBooleanBinding.class;
			}
			else if (type.equals("Fixed Table")){
				return LiteralStringBinding.class;
			}
		}
		return null;

	}

	public Class<?> getOutputDataType(String id) {
		OutputDescriptionType[] outputs = processDescription.getProcessOutputs().getOutputArray();

		for(OutputDescriptionType output : outputs){

			if(!output.getIdentifier().getStringValue().equals(id)){
				continue;
			}
			if(output.isSetLiteralOutput()){
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if(datatype.contains("tring")){
					return LiteralStringBinding.class;
				}
				if(datatype.contains("ollean")){
					return LiteralBooleanBinding.class;
				}
				if(datatype.contains("loat") || datatype.contains("ouble")){
					return LiteralDoubleBinding.class;
				}
				if(datatype.contains("nt")){
					return LiteralIntBinding.class;
				}
			}
			if(output.isSetComplexOutput()){
				String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
				if(mimeType.contains("xml") || (mimeType.contains("XML"))){
					return GTVectorDataBinding.class;
				}else{
					return GTRasterDataBinding.class;
				}
			}
		}
		return null;
	}








}
