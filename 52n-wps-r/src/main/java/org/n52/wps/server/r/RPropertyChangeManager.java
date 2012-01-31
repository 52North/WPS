package org.n52.wps.server.r;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.WPSConfigurationDocument.WPSConfiguration;
import org.n52.wps.commons.WPSConfig;

public class RPropertyChangeManager implements PropertyChangeListener {

	private static Logger LOGGER = Logger.getLogger(RPropertyChangeManager.class);
	
	private static RPropertyChangeManager instance;
	
	private RPropertyChangeManager(){
	}
	
	public static RPropertyChangeManager getInstance(){
		if(instance == null){
			instance = new RPropertyChangeManager();
			WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, instance);
		}
		return instance;
	}
	
	public static RPropertyChangeManager reInitialize(){
		WPSConfig.getInstance().removePropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, instance);
		instance = new RPropertyChangeManager();		
		return instance;
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt){	
		//String repName = LocalRAlgorithmRepository.class.getCanonicalName();
		//RepositoryManager manager = RepositoryManager.getInstance();	
		//LocalRAlgorithmRepository repository = (LocalRAlgorithmRepository) manager.getRepositoryForClassName(repName);
		
		LOGGER.info("received PropertyChangeEvent: "+evt.getPropertyName());
		deleteUnregisteredScripts();
		//TODO: How might processes be renamed?
	
	}
	
	
	/**
	 * Checks R-repository properties
	 * If there is a script without corresponding algorithm property, the property will be added
	 * 
	 * Usage:
	 * properties were changed -> scripts will be changed
	 * scripts were changed -> properties will be changed
	 */
	public void addUnregisteredScripts(){
		checkConfigProperties(false);
	}
	
	
	/**
	 * Checks R-repository properties
	 * If there is a script without corresponding algorithm property, it will be deleted
	 * 
	 * Usage: 
	 * properties were changed -> scripts will be changed
	 * scripts were changed (or lack of properties) -> properties will be changed
	 */
	public void deleteUnregisteredScripts(){
		checkConfigProperties(true);
	}
	
	
	/**
	 * Called during repository initialization or when wps config was changed
	 * @param deleteUnregisteredScripts
	 */
	private void checkConfigProperties(boolean deleteUnregisteredScripts){
		
		//Retrieve repository document and properties:
		String className = LocalRAlgorithmRepository.class.getCanonicalName();
		Repository[] repepositoryDocuments = WPSConfig.getInstance().getRegisterdAlgorithmRepositories();
		Repository repositoryDocument = null;
		
		for (Repository doc : repepositoryDocuments) {
			if(doc.getClassName().equals(className)){
				repositoryDocument = doc;
			}	
		}
		
		if(repositoryDocument == null){
			LOGGER.debug("Local R Algorithm Repository is not registered");
			return;
		}
		
		Property[] oldPropertyArray = repositoryDocument.getPropertyArray();
		HashMap<String, Property> algorithmPropertyHash = new HashMap<String, Property>();
		boolean propertyChanged = false;
		ArrayList<Property> newPropertyList= new ArrayList<Property>();
		
		//indicates if host and port of rserve are available in config properties
		//if not, values from R_Config will be used
		String rhost = "Rserve_Host";
		String rport = "Rserve_Port";
		String ruser = "Rserve_User";
		String rpwd  = "Rserve_Password";
		
		HashSet<String> param = new HashSet<String>();
		param.add(rhost.toLowerCase());
		param.add(rport.toLowerCase());
		param.add(ruser.toLowerCase());
		param.add(rpwd.toLowerCase());

		//repositoryDoc.get
		for(Property property : oldPropertyArray){
			// check the name and active state
			if(property.getName().equalsIgnoreCase("Algorithm"))
			{
				//put id into a dictionary to check and add later:
				algorithmPropertyHash.put(property.getStringValue(), property);
			}
			else{
				String pname = property.getName().toLowerCase();
				if(!param.contains(pname)) continue;
				
				if(pname.equalsIgnoreCase(rhost)){
					R_Config.RSERVE_HOST = property.getStringValue();
				}
				else
				if(pname.equalsIgnoreCase(rport))
				{
					try{
						R_Config.RSERVE_PORT = Integer.parseInt(property.getStringValue());
					}catch(NumberFormatException e){
						LOGGER.error("Non numeric RServe_Port property found - it will be ignored and deleted");
						continue;
					}
				}else
				if(pname.equalsIgnoreCase(ruser)){
					R_Config.RSERVE_USER = property.getStringValue();
				}else
				if(pname.equalsIgnoreCase(rpwd)){
					R_Config.RSERVE_PASSWORD = property.getStringValue();
				}
				//valid properties which are not algorithms will be just passed to the new list:
				param.remove(pname);
				newPropertyList.add(property);
			}
		}
		
		//if there was none of the parameters given by WPSconfig, those will be added
		//RServe_User and RServe_port won't be added
		if(param.contains(rhost.toLowerCase())){
			Property host = repositoryDocument.addNewProperty();
			host.setActive(true);
			host.setName(rhost);
			host.setStringValue(R_Config.RSERVE_HOST);
			newPropertyList.add(host);
			propertyChanged = true;
		}
		
		if(param.contains(rport.toLowerCase())){
			Property port = repositoryDocument.addNewProperty();
			port.setActive(true);
			port.setName(rport);
			port.setStringValue(""+R_Config.RSERVE_PORT);
			newPropertyList.add(port);
			propertyChanged = true;
		}
		

		// look up script dir for R process files
		// adjusts wps config
		File algorithmDir = new File(R_Config.SCRIPT_DIR);
		if(algorithmDir.isDirectory()){
			File[] scripts = algorithmDir.listFiles(new R_Config.ScriptFilter());
			for(File scriptf : scripts){
				String wkn = R_Config.FileToWkn(scriptf);
				Property prop = algorithmPropertyHash.get(wkn);
				
				//case: property is missing in wps config
				if(prop == null){
					//either delete r script or create new property:
					if(deleteUnregisteredScripts){
						deleteProcessFile(wkn);
						continue;
					}else{
						//Change Property if Algorithm is not inside process description:
						prop = repositoryDocument.addNewProperty();
						prop.setActive(true);
						prop.setName("Algorithm");
						prop.setStringValue(wkn);
						newPropertyList.add(prop);
						propertyChanged = true;
					}
				}else {
					newPropertyList.add(algorithmPropertyHash.remove(wkn));
				}
				
				/*if(prop.getActive() && addAlgorithm){
					repository.addAlgorithm(wkn);
				}*/
			}
		}

		//there might be registered algorithms, which don't got a script file any more,
		//those will be deleted here:
		if(!algorithmPropertyHash.isEmpty()) propertyChanged = true;
		
		class PropertyComparator implements Comparator<Property>{
			@Override
			public int compare(Property o1, Property o2) {
				int com1 = o1.getName().compareToIgnoreCase(o2.getName());
				if(com1 != 0)
					return com1;
				else
					return(o1.getStringValue().compareToIgnoreCase(o2.getStringValue()));
			}
		}
		
		//check if properties need to be re-ordered:
		if(!propertyChanged){
			PropertyComparator comp = new PropertyComparator();
			for(int i = 0; i < oldPropertyArray.length-1; i++){
				int order = comp.compare(oldPropertyArray[i],oldPropertyArray[i+1]);
				if(order > 0)
				{
					propertyChanged=true;
					break;
				}
			}
		}
		
		if(propertyChanged){
			Property[] newPropertyArray = newPropertyList.toArray(new Property[0]);
			
			//TODO: sort properties or not? And how?
			//sort list of properties lexicographically:
			
			Arrays.sort(newPropertyArray, new PropertyComparator());
			repositoryDocument.setPropertyArray(newPropertyArray);
			propertyChanged = true;
		
		//write new WPSConfig if property had to be changed
			WPSConfigurationDocument wpsConfigurationDocument = WPSConfigurationDocument.Factory.newInstance();
			WPSConfiguration wpsConfig = WPSConfig.getInstance().getWPSConfig();
			wpsConfigurationDocument.setWPSConfiguration(wpsConfig);
	        
			// writes the new WPSConfig to a file
	        try {
	            String configurationPath = WPSConfig.getConfigPath();
	            File XMLFile = new File(configurationPath);
	            wpsConfigurationDocument.save(XMLFile, new org.apache.xmlbeans.XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
	            WPSConfig.forceInitialization(configurationPath);
	            LOGGER.info("WPS Config was changed");
	        } catch (IOException e) {
	            LOGGER.error("Could not write configuration to file: "+ e.getMessage());
	        } catch (org.apache.xmlbeans.XmlException e){
	            LOGGER.error("Could not generate XML File from Data: " + e.getMessage());
	        }
		}
	}
	
	/**
	 *  Deletes *.R file from repository
	 */
	private boolean deleteProcessFile(String processName){
		boolean deleted = false;
		try {
			File processFile = R_Config.wknToFile(processName);
			deleted = processFile.delete();
			if (!deleted) {
				LOGGER.error("Process file " + processFile.getName()
						+ " could not be deleted, "
						+ "Process just removed temporarly");
			}else
				LOGGER.info("Process "+processName+" and process file "+ processFile.getName()
						+" successfully deleted!");
		} catch (Exception e) {
			LOGGER.error("Process file refering to "+processName+"could not be deleted, this" +
					"error was not expected:\n"+e.getLocalizedMessage());
		}
		return deleted;

	}

}
