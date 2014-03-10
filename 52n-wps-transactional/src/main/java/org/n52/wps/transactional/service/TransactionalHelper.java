/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.transactional.service;


import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.transactional.deploy.IProcessManager;
import org.w3c.dom.Node;

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
		IAlgorithmRepository algorithmRepository = RepositoryManager.getInstance().getRepositoryForClassName(className);
		if(algorithmRepository!=null){
			if(algorithmRepository instanceof ITransactionalAlgorithmRepository){
				return (ITransactionalAlgorithmRepository) algorithmRepository;
			}
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
	
	public static IProcessManager getProcessManagerForSchema(String schema) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		Repository repository = getMatchingTransactionalRepositoryClassName(schema);
		Property[] properties = repository.getPropertyArray();
		for(Property property : properties){
			if(property.getName().equals("DeployManager")){
				//return (IDeployManager) Class.forName(property.getStringValue()).newInstance();
                            try{

                                Class depManager = Class.forName(property.getStringValue());
                                Constructor con = depManager.getConstructor(ITransactionalAlgorithmRepository.class);
                                Object o = con.newInstance(new Object[]{getMatchingTransactionalRepository(schema)});
                                return (IProcessManager)o;
                            }catch(ClassNotFoundException e){
                                e.printStackTrace();
                            }catch(NoSuchMethodException e){
                                e.printStackTrace();
                            }catch(SecurityException e){
                                e.printStackTrace();
                            }catch(IllegalAccessException e){
                                e.printStackTrace();
                            }catch(InvocationTargetException e){
                                e.printStackTrace();
                            }catch(Exception e){
                                e.printStackTrace();
                            }

			}
		}
		return null;
	}
	
	public static void writeXmlFile(Node node, String filename) {
	    try {
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(node);

	        // Prepare the output file
	        File file = new File(filename);
	        Result result = new StreamResult(file);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.transform(source, result);
	    } catch (TransformerConfigurationException e) {
	    } catch (TransformerException e) {
	    }
	}

}
