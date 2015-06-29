/**
 * ﻿Copyright (C) 2007 - 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
