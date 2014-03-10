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
package org.n52.wps.unicore.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression
{
	public static synchronized byte[] toByteArray(final Object pData) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(pData);
		oos.close();
		return baos.toByteArray();
	}
	
	public static synchronized byte[] createCompressedData(byte[] data, boolean compressionEnabled) throws IOException
	{
		if (compressionEnabled)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream zos = new GZIPOutputStream(baos);
			byte[] buffer = new byte[1024];
			for (int length; (length = bais.read(buffer, 0, 1024)) > 0;)
			{
				zos.write(buffer, 0, length);
			}
			zos.close();
			baos.close();
			return baos.toByteArray();
		}
		else
		{
			return data;
		}

	}

	public static synchronized byte[] createUncompressedData(InputStream is, boolean compressionEnabled) throws IOException
	{
		if (compressionEnabled)
		{
			is = new GZIPInputStream(is);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (int length; (length = is.read(buffer, 0, 1024)) > 0;)
		{
			baos.write(buffer, 0, length);
		}
		is.close();
		baos.close();
		return baos.toByteArray();
	}
}
