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

package org.n52.wps.server;

import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

/**
 * @author Bastian Schaeffer, University of Muenster,	Theodor Foerster, ITC
 *
 */
public interface IAlgorithm  {
		
	Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport;
	
	List<String> getErrors();
	
	ProcessDescriptionType getDescription();
	
	/** Returns some well-known name for the process.
	 *  
	 *  @return Returns some well-known name for the process or algorithm
	 *  if that exists, else returns an empty String, never null.
	 *  @note The fully-qualified class name is gotten via getName();
	 */ 
	String getWellKnownName();
	
	/**
	 * Checks if the processDescription complies to the process itself and fits any schema or other dependencies.
	 */
	boolean processDescriptionIsValid();
	
	Class< ? > getInputDataType(String id);
	
	Class< ? > getOutputDataType(String id);
	
}
