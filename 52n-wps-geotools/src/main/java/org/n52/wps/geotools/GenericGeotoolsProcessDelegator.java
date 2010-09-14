package org.n52.wps.geotools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.Parameter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.IAlgorithm;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;


public class GenericGeotoolsProcessDelegator implements IAlgorithm {

	protected String processID;
	protected ProcessDescriptionType processDescription;
	private List<String> errors;
	
	public GenericGeotoolsProcessDelegator(String processIdentifier, ProcessDescriptionType processDescription){
		this.processID = processIdentifier;
		this.processDescription = processDescription;
	}
	
	@Override
	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	@Override
	public List<String> getErrors() {
		return errors;		
	}

	@Override
	public Class getInputDataType(String id) {
		return getDataType(id);
	}

	@Override
	public Class getOutputDataType(String id) {
		return getDataType(id);
	}

	@Override
	public String getWellKnownName() {
		return processID;
	}

	@Override
	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		Map<String, Object> geotoolsInputs = new HashMap<String, Object>();
		Name name = new NameImpl("gt",processID.replace("gt:", ""));
		Process process = Processors.createProcess(name);
		ProcessFactory processFactory = Processors.createProcessFactory(name);
		//special case. if envelope is required, set to the max extend of the input.
		Name processName = processFactory.getNames().iterator().next();
		Set<String> inputKeys = inputData.keySet();   
		 Map<String, Parameter<?>> inputParameterInfo = processFactory.getParameterInfo(processName);
		 Set<String> parameterIDs = inputParameterInfo.keySet();
		 for(String parameterID : parameterIDs){
			 Parameter<?> parameter = inputParameterInfo.get(parameterID);
			 
			 if(parameter.type.equals(Envelope.class)){
				
				 for(String key : inputKeys){
						//only one input per identifier is allowed
						Object value = inputData.get(key).get(0).getPayload();
						if(value instanceof FeatureCollection){
							FeatureCollection fc = (FeatureCollection) value;
							ReferencedEnvelope envelope = fc.getBounds();
							geotoolsInputs.put(parameter.title.toString(), envelope);
						}else if(value instanceof GridCoverage2D){
							GridCoverage2D grid = (GridCoverage2D) value;
							org.opengis.geometry.Envelope envelope = grid.getEnvelope();
							Envelope envelopeSimple = envelope;
							geotoolsInputs.put(parameter.title.toString(), envelopeSimple);
						}else{
							throw new RuntimeException("Execution of process " + processID + " failed. Reason: Could not derive required input envelope from inputdata");
						}
					}
			 }
		 }
		
	
		for(String key : inputKeys){
			//only one input per identifier is allowed
			Object value = inputData.get(key).get(0).getPayload();
			geotoolsInputs.put(key, value);
		}
		ProgressListener progress = new NullProgressListener();
		Map<String, Object> geotoolsResults = null;
		try {
			geotoolsResults = process.execute(geotoolsInputs, progress);
		} catch (ProcessException e) {
			e.printStackTrace();
			throw new RuntimeException("Execution of process " + processID + " failed. Reason: " + e.getLocalizedMessage());
		}
		Map<String, IData> results = new HashMap<String, IData>();
		Set<String> outputKeys = geotoolsResults.keySet();
		for(String key : inputKeys){
			Object result = geotoolsResults.get(key);
			results.put(key, getDataTypeForParameter(result));
		}
		return results;
	}

	
	protected Class getDataType(String identifier){
		InputDescriptionType[] inputs = processDescription.getDataInputs().getInputArray();
		for(InputDescriptionType input : inputs){
			if(input.getIdentifier().getStringValue().equals(identifier)){
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
		return null;
	}
	
	protected IData getDataTypeForParameter(Object output) {
	
    	if(output instanceof FeatureCollection){
    		return new GTVectorDataBinding((FeatureCollection) output);
    	}
    	if(output instanceof GridCoverage2D){
    		return new GTRasterDataBinding((GridCoverage2D) output);
    	}
    	if(output instanceof Double){
    		return new LiteralDoubleBinding((Double) output);
    	}
    	if(output instanceof Integer){
    		return new LiteralIntBinding((Integer) output);
    	}
    	if(output instanceof String){
    		return new LiteralStringBinding((String) output);
    	}
    	if(output instanceof Boolean){
    		return new LiteralBooleanBinding((Boolean) output);
    	}
    	
    	return null;
	}
}
