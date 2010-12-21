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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;

public class LegacyProcessRepository implements IAlgorithmRepository{
	
	private static Logger LOGGER = Logger.getLogger(LegacyProcessRepository.class);
	private static String PROCESS_DESCRIPTION_DIR;
	private static String TEMPLATE_WORKSPACE_DIR;
	
	private HashMap<String, ProcessDescriptionType> registeredProcessDescriptions;
	private HashMap<String, LegacyProcessDescription> registeredLegacyDescriptions;
	private HashMap<String, File> registeredTemplateWorkspaces;
	private static LegacyDelegatorFactory DELEGATOR_FACTORY;
	
	
	public LegacyProcessRepository() {
		
		this.registeredProcessDescriptions = new HashMap<String, ProcessDescriptionType>();
		this.registeredLegacyDescriptions = new HashMap<String, LegacyProcessDescription>();
		this.registeredTemplateWorkspaces = new HashMap<String, File>();
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		ArrayList<String> delegators = new ArrayList<String>();
		
		
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("DESCRIBE_PROCESS_DIR"))
				PROCESS_DESCRIPTION_DIR = property.getStringValue();
			if (property.getName().equalsIgnoreCase("TEMPLATE_WORKSPACE_DIR"))
				TEMPLATE_WORKSPACE_DIR = property.getStringValue();
			if(property.getName().equalsIgnoreCase("DELEGATOR")){
				delegators.add(property.getStringValue());
			}
		}
		
		
		File templateDir = new File(TEMPLATE_WORKSPACE_DIR);
		String[] templateDirectories = templateDir.list();
		for (String currentDirName : templateDirectories){
			File currentDir = new File(TEMPLATE_WORKSPACE_DIR + File.separator + currentDirName);
			registeredTemplateWorkspaces.put(currentDirName, currentDir.getAbsoluteFile());
		}
		
		// initialize delegator factory
		LegacyDelegatorFactory.initialize();
		DELEGATOR_FACTORY = LegacyDelegatorFactory.getInstance();
		
		this.addLocalAlgorithms();
	}
	
	private void addLocalAlgorithms(){
		File pdDirectory = new File(LegacyProcessRepository.PROCESS_DESCRIPTION_DIR);
		
		String[] describeProcessFiles = pdDirectory.list(new FilenameFilter() {
		    public boolean accept(File d, String name) {
		       return name.endsWith(".xml");
		    }
		});
		
		for (String currentFileName : describeProcessFiles){
			currentFileName = pdDirectory.getAbsolutePath() + File.separator + currentFileName;
			ProcessDescriptionType pd = this.loadProcessDescription(new File(currentFileName));
			String processID = pd.getIdentifier().getStringValue();
			LOGGER.info("Registering: " + processID);
			this.registeredProcessDescriptions.put(processID, pd);
			
			LegacyProcessDescription lpd = new LegacyProcessDescription(new File(currentFileName));
			this.registeredLegacyDescriptions.put(processID, lpd);
			
			if (!pd.validate()){
				LOGGER.warn("ProcessDescription is not valid. Removing "  + processID + " from Repository.");
				this.registeredProcessDescriptions.remove(processID);
				this.registeredLegacyDescriptions.remove(processID);
			}
		}
	}
	
	
	public Collection<String> getAlgorithmNames() {
		return this.registeredProcessDescriptions.keySet();
	}
	
	public boolean containsAlgorithm(String processID) {
		if(this.registeredProcessDescriptions.containsKey(processID)){
			return true;
		}
		return false;
	}
	
	private ProcessDescriptionType loadProcessDescription(File describeProcessFile){
		
		try {
			InputStream xmlDesc = new FileInputStream(describeProcessFile);
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription is empty!");
				return null;
			}
			
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! ", e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! ", e);
		}
		return null;
	}
	
	public IAlgorithm getAlgorithm(String processID, ExecuteRequest executeRequest) {
		
		if(!containsAlgorithm(processID)){
			throw new RuntimeException("Could not allocate Process " + processID);
		}
		
		ILegacyProcessDelegator properDelegator = DELEGATOR_FACTORY.getDelegator(processID, registeredLegacyDescriptions.get(processID), registeredProcessDescriptions.get(processID), registeredTemplateWorkspaces.get(processID));
		
		// initialize the delegator
		

		return properDelegator;
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> algorithms = new ArrayList<IAlgorithm>(registeredProcessDescriptions.size());
		for(String processID : registeredProcessDescriptions.keySet()){
			IAlgorithm algorithm = getAlgorithm(processID, null);
			if(algorithm!=null){
				algorithms.add(algorithm);
			}
		}
		return algorithms;
	}
	
	private void updateLocalAlgorithm(String id){
		// not yet implemented
	}
	
}
