/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 */
package org.n52.wps.server.algorithm.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

/**
 * This class can be used to test asynchronous requests with literal and BBOX output data.
 * It is basically the same as the DummyTestClass, only it lets the Thread sleep 5 seconds 
 * to allow the test client to request a status update.
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class LongRunningDummyTestClass extends AbstractSelfDescribingAlgorithm {
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

	public Class<?> getInputDataType(String id) {
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
	@Override
	public BigInteger getMinOccurs(String identifier){
		return new BigInteger("0");
	}	

	public Class<?> getOutputDataType(String id) {
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
		if(inputData.containsKey(inputID1)){
			result.put(outputID1, inputData.get(inputID1).get(0));
		}
		if(inputData.containsKey(inputID2)){
			result.put(outputID2, inputData.get(inputID2).get(0));
		}
		if(inputData.containsKey(inputID3)){
			result.put(outputID3, inputData.get(inputID3).get(0));
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			errors.add(e.getMessage());
		}
			
		return result;
	}
	
	@Override
	public String[] getSupportedCRSForBBOXInput(String identifier){
		String[] supportedCRS = new String[2];
		supportedCRS[0] = "EPSG:4328";
		supportedCRS[1] = "EPSG:5628";
		
		return supportedCRS;
	}
	
	@Override
	public String[] getSupportedCRSForBBOXOutput(String identifier){
		String[] supportedCRS = new String[2];
		supportedCRS[0] = "EPSG:4328";
		supportedCRS[1] = "EPSG:5628";
		
		return supportedCRS;
	}
	
	

}