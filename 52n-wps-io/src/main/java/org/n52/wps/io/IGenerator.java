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

package org.n52.wps.io;

import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.data.IData;

/** 
 * Basic interface for all Generators.
 * 
 * @author Matthias Mueller, TU Dresden
 * 
 */
public interface IGenerator extends IOHandler {
	
	/**
	 * 
	 * @param data
	 * @param mimeType
	 * @param schema
	 * @return
	 * 
	 * generates final output data produced by an IAlgorithm
	 * and returns an InputStream for subsequent access.
	 * 
	 */
	
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException;
	
	
	/**
	 * 
	 * @param data
	 * @param mimeType
	 * @param schema
	 * @return
	 * 
	 * generates final output data produced by an IAlgorithm, encodes it in Base64
	 * and returns an InputStream for subsequent access.
	 * 
	 */
	public InputStream generateBase64Stream(IData data, String mimeType, String schema) throws IOException;
	
}
