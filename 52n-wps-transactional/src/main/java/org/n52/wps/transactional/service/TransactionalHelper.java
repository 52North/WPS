package org.n52.wps.transactional.service;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
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
	
	public static ITransactionalAlgorithmRepository getMatchingTransactionalRepository(String schema){
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		String className = repository.getClassName();
		Object instance = null;
		try {
			//in case of a assumed singleton
			Class<?> clazz = Class.forName(className);
			Method[] methods = clazz.getMethods();
			for(Method method : methods){
				if(method.getName().equals("getInstance")){
					instance = method.invoke(clazz, new Object[0]);
					break;
				}
			}
			//in case it is not a singleton
			if(instance == null){
				instance = Class.forName(className).newInstance();
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(instance != null){
			return (ITransactionalAlgorithmRepository) instance;
		}
		return null;
	}
	
	public static String getDeploymentProfileForSchema(String schema){
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			if(property.getName().equals("DeploymentProfileClass")){
				return property.getStringValue();
			}
		}
		return null;
	}
	
	public static IDeployManager getDeploymentManagerForSchema(String schema) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			if(property.getName().equals("DeployManager")){
				return (IDeployManager) Class.forName(property.getStringValue()).newInstance();
			}
		}
		return null;
	}
}
