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
package org.n52.wps.io.test.datahandler.parser;

import java.io.InputStream;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.datahandler.parser.GML2BasicParser4Files;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

/**
 * This class is for testing the GML2BasicParser4Files.
 *
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class GML2BasicParser4FilesTest extends
		AbstractTestCase<GML2BasicParser4Files> {

	public void testParser() {

		if (!isDataHandlerActive()) {
			return;
		}


		Format[] formats = dataHandler.getSupportedFullFormats();

		Format format = formats[0];

		String mimeType = format.getMimetype();
		String schema = format.getSchema();

        InputStream input = getClass().getResourceAsStream("/tasmania_roads_gml2.xml");

		GenericFileDataBinding theBinding = dataHandler.parse(input, mimeType,
				schema);

		assertNotNull(theBinding.getPayload());
		assertNotNull(theBinding.getPayload().getBaseFile(true).exists());

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GML2BasicParser4Files();
	}

}
