/***************************************************************
Copyright © 2010 52°North Initiative for Geospatial Open Source Software GmbH

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

package org.n52.wps.server.legacy;

import java.io.File;
import java.util.ArrayList;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;

@Deprecated
public class LegacyDelegatorFactory {

	public static final String PROPERTY_NAME_REGISTERED_DELEGATOR = "DELEGATOR";
	private static LegacyDelegatorFactory factory;
	private static Logger LOGGER = Logger.getLogger(LegacyDelegatorFactory.class);
	
	private ILegacyProcessDelegator[] registeredDelegators;
	
	public static void initialize() {
		if (factory == null) {
			factory = new LegacyDelegatorFactory();
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private LegacyDelegatorFactory() {
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass("org.n52.wps.server.legacy.LegacyProcessRepository");
		
		ArrayList<String> delegatorClassNames = new ArrayList<String>();
		
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase(PROPERTY_NAME_REGISTERED_DELEGATOR)){
				delegatorClassNames.add(property.getStringValue());
			}
		}
		
		ArrayList<IAlgorithm> delegatorClassList = new ArrayList<IAlgorithm>(); 
		
		// load delegators
		for(String delegatorName : delegatorClassNames){
			delegatorClassList.add(this.loadDelegator(delegatorName));
		}
		
		registeredDelegators = delegatorClassList.toArray(new ILegacyProcessDelegator[delegatorClassList.size()]);
		
	}
	
	public static LegacyDelegatorFactory getInstance() {
		return factory;
	}
	
	public final ILegacyProcessDelegator getDelegator(String processID, LegacyProcessDescription legacyDescription, ProcessDescriptionType wpsDescribeProcess, File templateWorkspace){
		
		ILegacyProcessDelegator properDelegator = null;
		
		for(ILegacyProcessDelegator currentDelegator : registeredDelegators) {
			if(currentDelegator.isSupportedProcess(legacyDescription)){
				
				//create a new instance of proper delegator to return
				String delegatorName = currentDelegator.getClass().getCanonicalName();
				LOGGER.debug("Creating new instance of " + delegatorName);
				properDelegator = this.loadDelegator(delegatorName);
				properDelegator.initialize(processID, legacyDescription, wpsDescribeProcess, templateWorkspace);
			}
		}
		return properDelegator;
	}
    
	private final ILegacyProcessDelegator loadDelegator(String className){
		
		ILegacyProcessDelegator delegator = null;
		
		try {
			delegator = (ILegacyProcessDelegator)LegacyProcessRepository.class.getClassLoader().loadClass(className).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return delegator;
	}
	
}
