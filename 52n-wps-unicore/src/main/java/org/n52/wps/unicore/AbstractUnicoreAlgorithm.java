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
package org.n52.wps.unicore;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.unicore.client.UnicoreClient;

public abstract class AbstractUnicoreAlgorithm extends AbstractObservableAlgorithm implements IUnicoreAlgorithm
{
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractUnicoreAlgorithm.class);

	protected IAlgorithm embeddedAlgorithm;

	public IAlgorithm getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public AbstractUnicoreAlgorithm(IAlgorithm pEmbeddedAlgorithm)
	{
		super();
		this.embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public AbstractUnicoreAlgorithm(String wellKnownName, IAlgorithm pEmbeddedAlgorithm)
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
		try
		{

			List<Map<String, List<IData>>> inputDataList = split(pInputData);

			UnicoreClient client = new UnicoreClient();

			List<UnicoreAlgorithmOutput> outputDataList = client.perform(UnicoreAlgorithmInput.transform(inputDataList, embeddedAlgorithm.getDescription()
					.getIdentifier().getStringValue()));

			Map<String, IData> outputData = merge(UnicoreAlgorithmOutput.transform(outputDataList));

			return outputData;
		}
		catch (Exception e)
		{
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
}
