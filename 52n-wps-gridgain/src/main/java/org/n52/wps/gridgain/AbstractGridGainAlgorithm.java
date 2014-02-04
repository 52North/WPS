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
package org.n52.wps.gridgain;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridFactory;
import org.gridgain.grid.GridTaskFuture;
import org.n52.wps.gridgain.client.GridGainTask;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.unicore.UnicoreAlgorithmInput;
import org.n52.wps.unicore.UnicoreAlgorithmOutput;

public abstract class AbstractGridGainAlgorithm extends AbstractObservableAlgorithm implements IGridGainAlgorithm
{
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractGridGainAlgorithm.class);
	
	protected IAlgorithm embeddedAlgorithm;

	public IAlgorithm getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public AbstractGridGainAlgorithm(IAlgorithm pEmbeddedAlgorithm)
	{
		super();
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public AbstractGridGainAlgorithm(String wellKnownName, IAlgorithm pEmbeddedAlgorithm)
	{
		super(wellKnownName);
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public List<String> getErrors()
	{
		return embeddedAlgorithm.getErrors();
	}

	public Class getInputDataType(String id)
	{
		return embeddedAlgorithm.getInputDataType(id);
	}

	public Class getOutputDataType(String id)
	{
		return embeddedAlgorithm.getOutputDataType(id);
	}

	public Map<String, IData> run(Map<String, List<IData>> pInputData)
	{
		UnicoreAlgorithmOutput algorithmOutput;
		
		try
		{
			UnicoreAlgorithmInput algorithmInput = new UnicoreAlgorithmInput(pInputData, embeddedAlgorithm.getDescription().getIdentifier().getStringValue());
			
			GridFactory.start();
			Grid grid = GridFactory.getGrid();
			GridTaskFuture<UnicoreAlgorithmOutput> future = grid.execute(GridGainTask.class, algorithmInput);
			
			algorithmOutput = future.get();
		}
		catch (GridException e)
		{
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		finally
		{
			GridFactory.stop(true);
		}

		return algorithmOutput.getData();
	}
}
