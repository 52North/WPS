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
package org.n52.wps.server.grass.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles Java process streams. If this would not be done, the thread would not wait
 * for the process to be finished.
 * (See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4)
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class JavaProcessStreamReader extends Thread {

	private static Logger LOGGER = LoggerFactory
			.getLogger(JavaProcessStreamReader.class);

	InputStream inputStream;
	String type;
	OutputStream outputStream;

	public JavaProcessStreamReader(InputStream is, String type) {
		this(is, type, null);
	}

	public JavaProcessStreamReader(InputStream is, String type,
			OutputStream redirect) {
		this.inputStream = is;
		this.type = type;
		this.outputStream = redirect;
	}

	public void run() {

		InputStreamReader inputStreamReader = null;
		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		try {
			if (outputStream != null)
				printWriter = new PrintWriter(outputStream);

			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (printWriter != null) {
					printWriter.println(line);
				} else {
					LOGGER.debug(type + ">" + line);
				}
			}
			if (printWriter != null) {
				printWriter.flush();
			}
		} catch (IOException ioe) {
			LOGGER.error("Something went wrong while parsing the Java process stream.",
					ioe);
		} finally {
			try {
				if (printWriter != null) {
					printWriter.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (inputStreamReader != null) {
					inputStreamReader.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (Exception e) {
				LOGGER.error(
						"Something went wrong while trying to close the streams.",
						e);
			}
		}
	}

}
