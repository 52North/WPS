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
