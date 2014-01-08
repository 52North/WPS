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

import java.util.HashMap;
import java.util.Map;

public class SchemaRepository {

	private static Map<String, String> repository;
	private static Map<String, String> gmlNamespaces;
	
	public static synchronized String getSchemaLocation(String namespaceURI){
		if (repository==null) {
			repository = new HashMap<String, String>();
		}
		return repository.get(namespaceURI);
		
	}
	
	public static synchronized void registerSchemaLocation(String namespaceURI, String schemaLocation){
		if (repository==null) {
			repository = new HashMap<String, String>();
		}
		repository.put(namespaceURI,schemaLocation);
		
	}
	
	public static synchronized void registerGMLVersion(String namespaceURI, String gmlNamespace){
		if (gmlNamespaces==null) {
			gmlNamespaces = new HashMap<String, String>();
		}
		gmlNamespaces.put(namespaceURI, gmlNamespace);
		
	}

	public static synchronized String getGMLNamespaceForSchema(String namespace) {
		if (gmlNamespaces==null) {
			gmlNamespaces = new HashMap<String, String>();
		}
		return gmlNamespaces.get(namespace);
	}
}
