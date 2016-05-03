/**
 * ﻿Copyright (C) 2009 - 2016 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.ags;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public final class AGSProperties {
	
	private static final AtomicBoolean FIRST_RUN = new AtomicBoolean(true);
	private static String workspaceBase;
	private static String domain;
	private static String user;
	private static String pass;
	private static String ip;
	private static String arcObjectsJar;
	private static String processDescriptionDir;
	private static boolean nativeDCOM;
	
	private static Logger LOGGER = LoggerFactory.getLogger(AGSProperties.class);
	private static AGSProperties theProperties;
	
	private AGSProperties (){
		
		LOGGER.info("Reading AGS configuration ...");
		readAGSProperties();

		if (nativeDCOM){
			// switch to JINTEGRA native mode
			LOGGER.info("Switching to JINTEGRA_NATIVE_MODE");
			System.setProperty("JINTEGRA_NATIVE_MODE", "");
		}
		
	}
	
	public static synchronized AGSProperties getInstance(){
		if(theProperties==null){
			theProperties = new AGSProperties();
		}
		return theProperties;
	}
	
	public String getWorkspaceBase (){
		return workspaceBase;
	}
	
	public String getDomain (){
		return domain;
	}
	
	public String getUser (){
		return user;
	}
	
	public String getPass (){
		return pass;
	}
	
	public String getIP (){
		return ip;
	}
	
	public String getProcessDescriptionDir(){
		return processDescriptionDir;
	}
	
	private void readAGSProperties(){
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(AGSProcessRepository.class.getName());
		
		for(Property property : propertyArray){
			// get IP adress of ArcGIS Server instance
			if(property.getName().equalsIgnoreCase("IP")){
				ip=property.getStringValue();
			}
			// get Domain of ArcGIS Server instance; without a DC: Worstation Name (Windows)
			else if(property.getName().equalsIgnoreCase("DOMAIN")){
				domain=property.getStringValue();
			}
			// get user with access permissions to ArcGIS Server
			else if(property.getName().equalsIgnoreCase("USER")){
				user=property.getStringValue();
			}
			// get the corresponding pass
			else if(property.getName().equalsIgnoreCase("PASS")){
				pass=property.getStringValue();
			}
			// get property for DCOM Native Mode
			else if(property.getName().equalsIgnoreCase("DCOM_NATIVE")){
				String value = property.getStringValue();
				if (value.equalsIgnoreCase("TRUE")){
					nativeDCOM = true;
				}
			}
			// get Workspace base
			else if(property.getName().equalsIgnoreCase("WORKSPACEBASE")){
				workspaceBase=property.getStringValue();
			}
			// get path to arcobjects.jar
			else if(property.getName().equalsIgnoreCase("ARCOBJECTSJAR")){
				arcObjectsJar=property.getStringValue();
			}
			// get path to process description directory
			else if(property.getName().equalsIgnoreCase("DESCRIBE_PROCESS_DIR")){
				processDescriptionDir=property.getStringValue();
			}
			
			
		}
		
		//log the access data
		LOGGER.info("  IP: " + ip);
		LOGGER.info("  DOMAIN: " + domain);
		
		if (user != null) LOGGER.info("  USER: ***");
		else LOGGER.info("  USER: missing!");
		
		if (pass != null) LOGGER.info("  PASS: ***");
		else LOGGER.info("  PASS: missing!");
		
		LOGGER.info("  WORKSPACEBASE: " + workspaceBase);
	}
	
	public void bootstrapArcobjectsJar() {
		
		/*
		 * only bootstrap one time
		 */
		if (!FIRST_RUN.getAndSet(false)) return;
		
		//bootstrap arcobjects.jar
		LOGGER.info("Bootstrapping ArcObjects: " + arcObjectsJar);
		
		File aoFile = new File(arcObjectsJar);
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;

		try {

			Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] {aoFile.toURI().toURL()});
			
		}
		catch (Throwable t) {
			t.printStackTrace();
			System.err.println("Could not add arcobjects.jar to system classloader");
					
		}
		
//		Thread.currentThread().setContextClassLoader(sysloader);
	}

}
