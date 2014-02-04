/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.gridgain.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.gridgain.AbstractGridGainAlgorithm;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GridGainSimpleBufferAlgorithm extends AbstractGridGainAlgorithm
{
	public GridGainSimpleBufferAlgorithm()
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
		Map<String, IData> result = new HashMap<String, IData>();
		
		FeatureCollection mergedFeatureCollection = DefaultFeatureCollections.newCollection();
		for (Map<String, IData> data : outputData)
		{
			FeatureCollection singleFeatureCollection = (FeatureCollection) data.get("result").getPayload();
			mergedFeatureCollection.addAll(singleFeatureCollection);
		}		
		result.put("result", new GTVectorDataBinding(mergedFeatureCollection));
		
		return result;
	}

	protected int getNumberOfChunks(Map<String, List<IData>> pInputData)
	{
		return 3;
	}

	protected FeatureCollection[] splitFeatureCollection(FeatureCollection pFeatureCollection, int pNumberOfChucks)
	{
		FeatureCollection<SimpleFeatureType, SimpleFeature>[] result = new FeatureCollection[pNumberOfChucks];
		int chunkSize = (int) Math.floor((double) pFeatureCollection.size()
				/ (double) pNumberOfChucks);
		int currentFeatureCollection = -1;
		Iterator iterator = pFeatureCollection.iterator();
		for (int i = 0; i < pFeatureCollection.size(); i++)
		{
			if (i % chunkSize == 0 && currentFeatureCollection < (pNumberOfChucks - 1))
			{
				currentFeatureCollection++;
				result[currentFeatureCollection] = DefaultFeatureCollections.newCollection();
			}
			result[currentFeatureCollection].add((SimpleFeature) iterator.next());
		}
		return result;
	}

}
