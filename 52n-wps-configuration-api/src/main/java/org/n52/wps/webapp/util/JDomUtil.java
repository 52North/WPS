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
package org.n52.wps.webapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
/**
 * Parse and write to and from XML files.
 */
@Component
public class JDomUtil {

	private static Logger LOGGER = LoggerFactory.getLogger(JDomUtil.class);

	/**
	 * Parse a file to a {@code Document}
	 * 
	 * @param filePath
	 *            file path of the file to be parsed
	 * @return Parsed {@code Document} object
	 * @throws RuntimeException
	 *             if the path or the format of the file are invalid
	 */
	public Document parse(String filePath) {
		SAXBuilder sb = new SAXBuilder();
		Document document = null;
		
		try (FileInputStream inputStream = new FileInputStream(new File(filePath))) {
			document = sb.build(inputStream);
			LOGGER.info("{} is parsed and a Document is returned.", filePath);
		} catch (JDOMException | IOException e) {
			throw new RuntimeException("Unable to parse '" + filePath +"': ", e);
		}
		return document;
	}

	/**
	 * Write a {@code Document} to a file
	 * 
	 * @param document
	 *            the document to be written
	 * @param filePath
	 *            the path to write to
	 * @throws RuntimeException
	 *             if the path is invalid or the document is null
	 */
	public void write(Document document, String filePath) {
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		try (FileOutputStream outputStream = new FileOutputStream(new File(filePath))) {
			xmlOutputter.output(document, outputStream);
			LOGGER.info("{} is written successfully.", filePath);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write Document to '" + filePath +"': ");
		}
	}
}
