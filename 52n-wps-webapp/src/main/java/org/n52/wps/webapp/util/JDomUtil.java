/**
 * Copyright (C) 2013
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
