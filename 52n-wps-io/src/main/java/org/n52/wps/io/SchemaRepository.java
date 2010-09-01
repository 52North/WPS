package org.n52.wps.io;

import java.util.HashMap;
import java.util.Map;

public class SchemaRepository {

	private static Map repository;
	
	public static String getSchemaLocation(String namespaceURI){
		if(repository==null){
			repository= new HashMap();
		}
		return (String) repository.get(namespaceURI);
		
	}
	
	public static void registerSchemaLocation(String namespaceURI, String schemaLocation){
		if(repository==null){
			repository= new HashMap();
		}
		repository.put(namespaceURI,schemaLocation);
		
	}
}
