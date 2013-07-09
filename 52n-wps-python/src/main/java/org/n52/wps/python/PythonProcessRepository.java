/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

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

package org.n52.wps.python;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.feed.FeedRepository;
import org.n52.wps.server.feed.movingcode.MovingCodeObject;

public class PythonProcessRepository implements IAlgorithmRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(PythonProcessRepository.class);
	
	private static final String PROPERTY_PROCESS_INVENTORY_DIR = "PROCESS_INVENTORY_DIR";
	private static final String PROPERTY_CONTAINER_URN = "CONTAINER_URN";
	private static final String PROPERTY_BACKEND_URN = "BACKEND_URN";
	private static final String PROPERTY_WORKSPACEBASE = "WORKSPACEBASE";
	
	
	private HashMap<String, MovingCodeObject> registeredAlgorithms;
	private URI[] supportedContainers;
	private URI[] supportedBackends;
	private File workspaceBase = null;
	private File inventoryDir = null;
	
	
	public PythonProcessRepository (){
		LOGGER.info("Initializing Python Process Repository ...");
		
		//initialize local variables
		registeredAlgorithms = new HashMap<String, MovingCodeObject>();
		try{
			loadConfiguration();
			loadLocalProcesses();
			loadFeedProcesses();
		} catch (Exception e){
			LOGGER.error("Could not initialize PythonProcessRepository.");
			LOGGER.error(e.getMessage());
		}
		
		// check if workspaceBase is specified
		if (workspaceBase == null){
			LOGGER.error("Workspace base is missing: Clearing my Process Inventory");
			registeredAlgorithms = new HashMap<String, MovingCodeObject>();
		}
		
		// log active Processes ...
		LOGGER.info("Algorithms loaded by Python Process Repository:");
		for (String currentKey : registeredAlgorithms.keySet()){
			LOGGER.info(currentKey);
		}
		
		// ... or state that there arent't any
		if (registeredAlgorithms.size()==0){
			LOGGER.info("No applicable algorithms fond");
		}
		
	}
	
	private void loadConfiguration() throws Exception{
		Property[] props = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		ArrayList<URI> containerList = new ArrayList<URI>();
		ArrayList<URI> backendList = new ArrayList<URI>();
		
		for(Property currentProp : props){
			try{
				if (currentProp.getActive()){
					String propName = currentProp.getName();
					if (propName.equalsIgnoreCase(PROPERTY_PROCESS_INVENTORY_DIR)){
						inventoryDir = new File (currentProp.getStringValue());
					}
					if (propName.equalsIgnoreCase(PROPERTY_WORKSPACEBASE)){
						workspaceBase = new File (currentProp.getStringValue());
					}
					if (propName.equalsIgnoreCase(PROPERTY_BACKEND_URN)){
						backendList.add(new URI (currentProp.getStringValue()));
					}
					if (propName.equalsIgnoreCase(PROPERTY_CONTAINER_URN)){
						containerList.add(new URI (currentProp.getStringValue()));
					}
				}
			} catch (URISyntaxException e){
				LOGGER.error("Invalid container or backend URN - offending item is " + currentProp.getStringValue());
			}
		}
		
		supportedContainers = containerList.toArray(new URI[0]);
		supportedBackends = backendList.toArray(new URI[0]);
		containerList = null;
		backendList = null;
		
	}
	
	private void loadLocalProcesses(){
		
		// abort loading if inventoryDir is empty
		if (inventoryDir == null){
			return;
		}
		
		String[] describeProcessFiles = retrieveProcessDescriptions(inventoryDir);
		
		// create new MovingCodeObjects
		for (String currentFileName : describeProcessFiles){
			File currentFile = new File (inventoryDir.getAbsolutePath() + File.separator + currentFileName);
			MovingCodeObject currentMCO = new MovingCodeObject(currentFile, inventoryDir);
			if (isSupportedScript(currentMCO)){
				registeredAlgorithms.put(currentMCO.getProcessID(), currentMCO);
			} else {
				LOGGER.info(currentMCO.getProcessID() + " is not supported by this repository. - Dropping algorithm.");
				currentMCO = null;
			}
		}
	}
	
	private void loadFeedProcesses(){
		// retrieve supported MCOs from the feed
		MovingCodeObject[] feedMCOs = FeedRepository.getInstance().getMovingCodeObjects(supportedContainers, supportedBackends);
		for (MovingCodeObject currentMCO : feedMCOs){
			// add those algorithms to this Repository
			registeredAlgorithms.put(currentMCO.getProcessID(), currentMCO);
		}
	}
	
	private boolean isSupportedScript(MovingCodeObject mco){
		boolean rightContainer = false;
		boolean rightBackends = false;
		for (URI currentContainer : supportedContainers){
			if (mco.isContainer(currentContainer)){
				rightContainer = true;
			}
		}
		
		if (mco.isSufficientRuntimeEnvironment(supportedBackends)){
			rightBackends = true;
		}
		
		return (rightContainer && rightBackends);
	}
	
	private static String[] retrieveProcessDescriptions(File directory){
		String[] describeProcessFiles = directory.list(new FilenameFilter() {
		    public boolean accept(File d, String name) {
		       return name.endsWith(".xml");
		    }
		});
		return describeProcessFiles;
	}
	
	/**
	 * Checks if a given processID exists 
	 * @param processID
	 * @return
	*/
	public boolean containsAlgorithm(String processID) {
		if(registeredAlgorithms.containsKey(processID)){
			return true;
		}
		return false;
	}
	
	/**
	 * Returns an IAlgorithm through GenericAGSProcessDelegator
	 * @param processID
	 * @return
	*/
	public IAlgorithm getAlgorithm(String processID) {
		if(!containsAlgorithm(processID)){
			throw new RuntimeException("Could not allocate Process " + processID);
		}
		try {
			// create a unique directory for each instance
			String randomDirName = workspaceBase + File.separator + UUID.randomUUID();
			return new PythonScriptDelegator(registeredAlgorithms.get(processID), new File (randomDirName));
		} catch (IOException e) {
			LOGGER.error(processID + ": Instantiation failed!");
			e.printStackTrace();
			return null;
		}
	}
	
	public Collection<String> getAlgorithmNames() {
		return registeredAlgorithms.keySet();
	}
	
	public ProcessDescriptionType getProcessDescription(String processID) {
		return registeredAlgorithms.get(processID).getProcessDescription();
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
}
