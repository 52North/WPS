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
import java.io.FileNotFoundException;
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
	 * Parse a resource to a {@code Document}
	 * 
	 * @param Absolute file path of the resource to be parsed
	 * @return Populated {@code Document} object
	 * @throws JDOMException
	 * @throws IOException
	 */
	public Document load(String absoluteFilePath) throws JDOMException, IOException {
		SAXBuilder sb = new SAXBuilder();
		Document document = null;
		try (FileInputStream inputStream = new FileInputStream(new File(absoluteFilePath))) {
			document = sb.build(inputStream);
			LOGGER.info(absoluteFilePath + " is loaded and a Document is returned.");
		} catch (JDOMException e) {
			throw new JDOMException(e.getMessage());
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(absoluteFilePath + " is not found.");
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}

		return document;
	}

	/**
	 * Write {@code Document} to a file
	 * 
	 * @param Document to be written
	 * @param Absolute file path to write to
	 * @throws IOException
	 */
	public void write(Document document, String absoluteFilePath) throws IOException {
		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getRawFormat());
		try (FileOutputStream outputStream = new FileOutputStream(new File(absoluteFilePath))) {
			xmlOutputter.output(document, outputStream);
			LOGGER.info(absoluteFilePath + " is written.");
		} catch (IOException e) {
			throw new IOException("Unable to write to: " + absoluteFilePath + ": " + e.getMessage());
		}
	}
}
