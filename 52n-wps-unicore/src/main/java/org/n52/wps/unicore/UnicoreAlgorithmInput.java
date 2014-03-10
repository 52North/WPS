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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

public class UnicoreAlgorithmInput implements Serializable
{
	protected transient Map<String, List<IData>> data;
	protected transient String embeddedAlgorithm;

	public String getEmbeddedAlgorithm()
	{
		return embeddedAlgorithm;
	}

	public UnicoreAlgorithmInput(Map<String, List<IData>> pData, String pEmbeddedAlgorithm)
	{
		data = pData;
		embeddedAlgorithm = pEmbeddedAlgorithm;
	}

	public Map<String, List<IData>> getData()
	{
		return data;
	}

	public static List<UnicoreAlgorithmInput> transform(List<Map<String, List<IData>>> pData, String pAlgorithmIdentifier)
	{
		List<UnicoreAlgorithmInput> result = new ArrayList<UnicoreAlgorithmInput>();
		for (Map<String, List<IData>> input : pData)
		{
			result.add(new UnicoreAlgorithmInput(input, pAlgorithmIdentifier));
		}
		return result;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(data);
		oos.writeObject(embeddedAlgorithm);
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		data = (Map<String, List<IData>>) oos.readObject();
		embeddedAlgorithm = (String) oos.readObject();
	}
}
