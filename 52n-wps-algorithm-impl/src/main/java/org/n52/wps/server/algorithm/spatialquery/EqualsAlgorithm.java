package org.n52.wps.server.algorithm.spatialquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class EqualsAlgorithm extends AbstractSelfDescribingAlgorithm {

	Logger LOGGER = LoggerFactory.getLogger(EqualsAlgorithm.class);
	private final String inputID1 = "LAYER1";
	private final String inputID2 = "LAYER2";
	private final String outputID = "RESULT";
	private List<String> errors = new ArrayList<String>();

	public List<String> getErrors() {
		return errors;
	}

	public Class<GTVectorDataBinding> getInputDataType(String id) {
		if (id.equalsIgnoreCase(inputID1) || id.equalsIgnoreCase(inputID2)) {
			return GTVectorDataBinding.class;
		}
		return null;
	}

	public Class<LiteralBooleanBinding> getOutputDataType(String id) {
		if(id.equalsIgnoreCase(outputID)){
			return LiteralBooleanBinding.class;
		}
		return null;
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		if(inputData==null || !inputData.containsKey(inputID1)){
			throw new RuntimeException("Error while allocating input parameters");
		}
		if(inputData==null || !inputData.containsKey(inputID2)){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> firstDataList = inputData.get(inputID1);
		if(firstDataList == null || firstDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = firstDataList.get(0);
				
		FeatureCollection<?, ?> firstCollection = ((GTVectorDataBinding) firstInputData).getPayload();

		List<IData> secondDataList = inputData.get(inputID2);
		if(secondDataList == null || secondDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData secondInputData = secondDataList.get(0);
				
		FeatureCollection<?, ?> secondCollection = ((GTVectorDataBinding) secondInputData).getPayload();
		
		FeatureIterator<?> firstIterator = firstCollection.features();
		
		FeatureIterator<?> secondIterator = secondCollection.features();
		
		if(!firstIterator.hasNext()){
			throw new RuntimeException("Error while iterating over features in layer 1");
		}
		
		if(!secondIterator.hasNext()){
			throw new RuntimeException("Error while iterating over features in layer 2");
		}
		
		SimpleFeature firstFeature = (SimpleFeature) firstIterator.next();
		
		SimpleFeature secondFeature = (SimpleFeature) secondIterator.next();
		
		boolean equals = ((Geometry)firstFeature.getDefaultGeometry()).equals((Geometry)secondFeature.getDefaultGeometry());
			
		HashMap<String, IData> result = new HashMap<String, IData>();

		result.put(outputID,
				new LiteralBooleanBinding(equals));
		return result;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifiers = new ArrayList<String>(2);
		identifiers.add(inputID1);
		identifiers.add(inputID2);
		return identifiers;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifiers = new ArrayList<String>(1);
		identifiers.add(outputID);
		return identifiers;
	}


}
