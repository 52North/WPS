/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.ags;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;

public final class AGSProperties {
	
	private static String workspaceBase;
	private static String domain;
	private static String user;
	private static String pass;
	private static String ip;
	private static String arcObjectsJar;
	private static String processDescriptionDir;
	
	private static Logger LOGGER = Logger.getLogger(AGSProperties.class);
	private static AGSProperties theProperties;
	
	private AGSProperties (){
		
		LOGGER.info("Reading AGS configuration ...");
		readAGSProperties();
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
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass("org.n52.wps.ags.AGSProcessRepository");
		
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("IP")){
				ip=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("DOMAIN")){
				domain=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("USER")){
				user=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("PASS")){
				pass=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("WORKSPACEBASE")){
				workspaceBase=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("ARCOBJECTSJAR")){
				arcObjectsJar=property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("DESCRIBE_PROCESS_DIR")){
				processDescriptionDir=property.getStringValue();
			}
			
			
		}
		
		//log the access data
		
		LOGGER.info("  IP: " + ip);
		LOGGER.info("  DOMAIN: " + domain);
		
		if (user != null) LOGGER.info("  USER: ***");
		else LOGGER.info("  USER: missing");
		
		if (pass != null) LOGGER.info("  PASS: ***");
		else LOGGER.info("  PASS: missing");
		
		LOGGER.info("  WORKSPACEBASE: " + workspaceBase);
	}
	
	public void bootstrapArcobjectsJar() {
		
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
		
		Thread.currentThread().setContextClassLoader(sysloader);
	}

}
