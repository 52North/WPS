/***************************************************************
This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

Copyright (C) 2009 by con terra GmbH

Authors: 
	Bastian Schäffer, University of Muenster
	Theodor Foerster, ITC


Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
48155 Muenster, Germany, 52n@conterra.de

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program (see gnu-gpl v2.txt); if not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA or visit the web page of the Free
Software Foundation, http://www.fsf.org.

***************************************************************/

package org.n52.wps.server;


import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

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
