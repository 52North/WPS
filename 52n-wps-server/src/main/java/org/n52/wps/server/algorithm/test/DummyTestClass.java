package org.n52.wps.server.algorithm.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;



public class DummyTestClass extends AbstractSelfDescribingAlgorithm {
	private final String inputID1 = "ComplexInputData";
	private final String inputID2 = "LiteralInputData";
	private final String inputID3 = "BBOXInputData";
	private final String outputID1 = "ComplexOutputData";
	private final String outputID2 = "LiteralOutputData";
	private final String outputID3 = "BBOXOutputData";
	
	private List<String> errors = new ArrayList<String>();

	public List<String> getErrors() {
		return errors;
	}

	public Class getInputDataType(String id) {
		if (id.equalsIgnoreCase(inputID1)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(inputID2)) {
			return LiteralStringBinding.class;
		}
		if (id.equalsIgnoreCase(inputID3)) {
			return GTReferenceEnvelope.class;
		}
		return null;
	}

	public Class getOutputDataType(String id) {
		if (id.equalsIgnoreCase(outputID1)) {
			return GenericFileDataBinding.class;
		}
		if (id.equalsIgnoreCase(outputID2)) {
			return LiteralStringBinding.class;
		}
		if (id.equalsIgnoreCase(outputID3)) {
			return GTReferenceEnvelope.class;
		}
		return null;
	}
	
	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(inputID1);
		identifierList.add(inputID2);
		identifierList.add(inputID3);
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add(outputID1);
		identifierList.add(outputID2);
		identifierList.add(outputID3);
		return identifierList;
	}
	

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		HashMap<String,IData> result = new HashMap<String,IData>();
		result.put(outputID1, inputData.get(inputID1).get(0));
		result.put(outputID2, inputData.get(inputID2).get(0));
		result.put(outputID3, inputData.get(inputID3).get(0));
		
		return result;
	}
	
	

}