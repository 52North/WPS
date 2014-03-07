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

package org.n52.wps.io.datahandler.parser;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class GenericRasterFileParser extends AbstractParser{
	
	private static Logger LOGGER = LoggerFactory.getLogger(GenericRasterFileParser.class);
	
	public GenericRasterFileParser() {
		super();
		supportedIDataTypes.add(GenericRasterFileParser.class);
	}
	
	@Override
	public GenericFileDataBinding parse(InputStream input, String mimeType, String schema) {
		
		GenericFileData theData = new GenericFileData(input, mimeType);
		LOGGER.info("Found File Input " + mimeType);
		
		return new GenericFileDataBinding(theData);
	}

}
