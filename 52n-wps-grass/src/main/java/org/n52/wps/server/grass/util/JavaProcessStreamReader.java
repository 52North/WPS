/**
 * ﻿Copyright (C) 2007
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
