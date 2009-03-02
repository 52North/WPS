/*******************************************************************************
 * Copyright (C) 2008
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
 * Author: Bastian Baranski (Bastian.Baranski@uni-muenster.de)
 * Created: 03.09.2008
 * Modified: 03.09.2008
 *
 ******************************************************************************/

package org.n52.wps.grid.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.grid.AbstractDistributedAlgorithm;
import org.n52.wps.grid.util.DistributedUtlilities;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.algorithm.SimpleBufferAlgorithm;

/**
 * @author bastian
 * 
 */
public class DistributedSimpleBufferAlgorithm extends AbstractDistributedAlgorithm
{
	private static Logger LOGGER = Logger.getLogger(DistributedSimpleBufferAlgorithm.class);

	/**
	 * 
	 */
	public DistributedSimpleBufferAlgorithm()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.n52.wps.server.IAlgorithm#run(java.util.Map)
	 */
	public Map<String, IData> run(Map<String, List<IData>> inputData)
	{
		SimpleBufferAlgorithm buffer = new SimpleBufferAlgorithm();
		return buffer.run(inputData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.n52.wps.server.IDistributableAlgorithm#split(org.n52.wps.server. IDistributableAlgorithm.WebProcessingServiceInput, int)
	 */
	public List<WebProcessingServiceInput> split(WebProcessingServiceInput pInputDataList, int pMaximumNumberOfNodes)
	{
		List<WebProcessingServiceInput> result = new ArrayList<WebProcessingServiceInput>();

		// get input data	
		if (pInputDataList.getInputData() ==null || !pInputDataList.getInputData() .containsKey("data")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList = pInputDataList.getInputData().get("data");
		if (dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		IData firstInputData = dataList.get(0);
				
		FeatureCollection featureCollection = ((GTVectorDataBinding) firstInputData).getPayload();

		// split input data
		FeatureCollection[] featureCollectionList = DistributedUtlilities.splitFeatureCollection(featureCollection, pMaximumNumberOfNodes);

		for (FeatureCollection fc : featureCollectionList)
		{
			Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
			
			inputData.put("width", pInputDataList.getInputData().get("width"));
			
			List<IData> inputDataList = new ArrayList<IData>();
			inputDataList.add(new GTVectorDataBinding(fc));
			inputData.put("data", inputDataList);

			WebProcessingServiceInput input = new WebProcessingServiceInput(inputData);
			result.add(input);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.IDistributableAlgorithm#merge(java.util.List)
	 */
	public WebProcessingServiceOutput merge(List<WebProcessingServiceOutput> pOutputDataList)
	{
		FeatureCollection mergedFeatureCollection = DefaultFeatureCollections.newCollection();

		for (WebProcessingServiceOutput output : pOutputDataList)
		{
			FeatureCollection singleFeatureCollection = (FeatureCollection) output.getOutputData().get("result").getPayload();
			mergedFeatureCollection.addAll(singleFeatureCollection);
		}

		Map<String, IData> outputData = new HashMap<String, IData>();
		outputData.put("result", new GTVectorDataBinding(mergedFeatureCollection));
		
		WebProcessingServiceOutput result = new WebProcessingServiceOutput(outputData);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.IAlgorithm#getInputDataType(java.lang.String)
	 */
	public Class getInputDataType(String id) {
		SimpleBufferAlgorithm buffer = new SimpleBufferAlgorithm();
		return buffer.getInputDataType(id);
	}

	/* (non-Javadoc)
	 * @see org.n52.wps.server.IAlgorithm#getOutputDataType(java.lang.String)
	 */
	public Class getOutputDataType(String id) {
		SimpleBufferAlgorithm buffer = new SimpleBufferAlgorithm();
		return buffer.getOutputDataType(id);
	}

}
