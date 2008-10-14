package org.n52.wps.transactional.service;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.transactional.deploy.IDeployManager;

public class TransactionalHelper {

	public static Repository getMatchingTransactionalRepositoryClassName(String schema){
		WPSConfig config = WPSConfig.getInstance();
		Repository[] repositories = config.getRegisterdAlgorithmRepositories();
		
		for(Repository repository : repositories){
			Property[] properties = repository.getPropertyArray();
			for(Property property : properties){
				if(property.getName().equals("supportedFormat")){
					if(property.getStringValue().equals(schema)){
						return repository;
					}
				}
				
			}
		}
		return null;
	}
	
	public static String getDeploymentManagerForSchema(String schema){
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			if(property.getName().equals("DeployManager")){
				return property.getStringValue();
			}
		}
		return null;
	}
}
