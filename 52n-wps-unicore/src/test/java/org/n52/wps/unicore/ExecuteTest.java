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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ExecuteTest
{
	public static void main(String[] args) throws MalformedURLException, IOException
	{
		String sUrl = "http://localhost:8761/wps/WebProcessingService";
		String sXml = "ExecuteDocument.xml";

		new ExecuteTest().exampleRequest(sUrl, sXml);
	}

	private void exampleRequest(String sUrl, String sXml) throws MalformedURLException, IOException
	{
		URL url = new URL(sUrl);

		URLConnection connection = url.openConnection();

		connection.setDoInput(true);
		connection.setDoOutput(true);

		OutputStream os = connection.getOutputStream();

		InputStream is = ExecuteTest.class.getResourceAsStream(sXml);
		byte[] buffer = new byte[1024];
		int c = is.read(buffer);
		while (c > 0)
		{
			System.out.print(new String(buffer, 0, c));
			os.write(buffer, 0, c);
			c = is.read(buffer);
		}
		os.close();
		System.out.println();

		StringBuffer result = new StringBuffer();
		is = connection.getInputStream();
		buffer = new byte[1];
		c = is.read(buffer);
		while (c > 0)
		{
			result.append(new String(buffer, 0, c));
			c = is.read(buffer);
		}
		System.out.println(result.toString());
	}
}
