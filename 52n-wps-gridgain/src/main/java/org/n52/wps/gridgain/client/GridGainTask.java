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
package org.n52.wps.gridgain.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gridgain.grid.GridException;
import org.gridgain.grid.GridJob;
import org.gridgain.grid.GridJobAdapter;
import org.gridgain.grid.GridJobResult;
import org.gridgain.grid.GridTaskSplitAdapter;
import org.n52.wps.gridgain.algorithm.GridGainSimpleBufferAlgorithm;
import org.n52.wps.io.data.IData;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;

public class GridGainTask extends GridTaskSplitAdapter<UnicoreAlgorithmInput, UnicoreAlgorithmOutput>
{

	@Override
	protected Collection<? extends GridJob> split(int gridSize, UnicoreAlgorithmInput algorithmInput) throws GridException
	{
		// split the input data
		GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
		List<Map<String, List<IData>>> inputDataList = algorithm.split(algorithmInput.getData());

		// convert data structure
		List<UnicoreAlgorithmInput> inputData = new ArrayList<UnicoreAlgorithmInput>();
		for (int i = 0; i < inputDataList.size(); i++)
		{
			inputData.add(new UnicoreAlgorithmInput(inputDataList.get(0), algorithmInput.getEmbeddedAlgorithm()));
		}

		List<GridJob> jobs = new ArrayList<GridJob>(inputData.size());
		for (UnicoreAlgorithmInput input : inputData)
		{
			jobs.add(new GridJobAdapter<UnicoreAlgorithmInput>(input)
			{
				public Serializable execute()
				{

					UnicoreAlgorithmInput input = getArgument();
					GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
					Map<String, IData> result = algorithm.getEmbeddedAlgorithm().run(input.getData());
					UnicoreAlgorithmOutput output = new UnicoreAlgorithmOutput(result);
					return output;
				}
			});

		}
		return jobs;

	}

	public UnicoreAlgorithmOutput reduce(List<GridJobResult> resultList) throws GridException
	{
		// convert data structure
		List<Map<String, IData>> outputData = new ArrayList<Map<String, IData>>();

		for (GridJobResult result : resultList)
		{
			UnicoreAlgorithmOutput output = result.getData();
			outputData.add(output.getData());

		}

		// merge the output data
		GridGainSimpleBufferAlgorithm algorithm = new GridGainSimpleBufferAlgorithm();
		UnicoreAlgorithmOutput result = new UnicoreAlgorithmOutput(algorithm.merge(outputData));

		return result;

	}

}
