
package org.n52.wps.unicore.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.unicore.AbstractUnicoreAlgorithm;
import org.opengis.feature.Feature;

public class UnicoreSimpleBufferAlgorithm extends AbstractUnicoreAlgorithm
{
	public UnicoreSimpleBufferAlgorithm()
	{
		super(new org.n52.wps.server.algorithm.SimpleBufferAlgorithm());
	}

	public List<Map<String, List<IData>>> split(Map<String, List<IData>> pInputData)
	{
		List<Map<String, List<IData>>> result = new ArrayList<Map<String, List<IData>>>();
		FeatureCollection featureCollection = ((GTVectorDataBinding) pInputData.get("data").get(0)).getPayload();
		FeatureCollection[] featureCollectionList = splitFeatureCollection(featureCollection, getNumberOfChunks(pInputData));
		for (FeatureCollection fc : featureCollectionList)
		{
			Map<String, List<IData>> chunk = new HashMap<String, List<IData>>();
			chunk.put("width", pInputData.get("width"));
			List<IData> data = new ArrayList<IData>();
			data.add(new GTVectorDataBinding(fc));
			chunk.put("data", data);
			result.add(chunk);
		}
		return result;
	}

	public Map<String, IData> merge(List<Map<String, IData>> outputData)
	{
		FeatureCollection mergedFeatureCollection = DefaultFeatureCollections.newCollection();
		for (Map<String, IData> data : outputData)
		{
			FeatureCollection singleFeatureCollection = (FeatureCollection) data.get("result").getPayload();
			mergedFeatureCollection.addAll(singleFeatureCollection);
		}
		Map<String, IData> result = new HashMap<String, IData>();
		result.put("result", new GTVectorDataBinding(mergedFeatureCollection));
		return result;
	}

	protected int getNumberOfChunks(Map<String, List<IData>> pInputData)
	{
		// return a fixed number
//		return 3;
		
		// return a number depending on an input parameter 
//		int numberOfChunks = ((LiteralIntBinding) pInputData.get("numberOfChunks").get(0)).getPayload().intValue();
//		return numberOfChunks;
		
		// return a number depending on the complexity of input data
		FeatureCollection features = ((GTVectorDataBinding) pInputData.get("data").get(0)).getPayload();
		return features.size() % 5;	
	}

	protected FeatureCollection[] splitFeatureCollection(FeatureCollection pFeatureCollection, int pNumberOfChucks)
	{
		FeatureCollection[] result = new FeatureCollection[pNumberOfChucks];
		int chunkSize = (int) Math.floor((double) pFeatureCollection.size() / (double) pNumberOfChucks);
		int currentFeatureCollection = -1;
		Iterator iterator = pFeatureCollection.iterator();
		for (int i = 0; i < pFeatureCollection.size(); i++)
		{
			if (i % chunkSize == 0 && currentFeatureCollection < (pNumberOfChucks - 1))
			{
				currentFeatureCollection++;
				result[currentFeatureCollection] = DefaultFeatureCollections.newCollection();
			}
			result[currentFeatureCollection].add((Feature) iterator.next());
		}
		return result;
	}

	
}
