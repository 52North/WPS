/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2007 by con terra GmbH

 Authors:
 	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany
	

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.install.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import noNamespace.WPSConfigurationDocument;
import noNamespace.GeneratorDocument.Generator;
import noNamespace.ParserDocument.Parser;
import noNamespace.PropertyDocument.Property;
import noNamespace.RepositoryDocument.Repository;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.install.framework.InstallWizard;
import org.n52.wps.install.framework.WizardPanelDescriptor;
import org.n52.wps.install.wizard.util.DirectoryCopier;
import org.n52.wps.test.checknodetree.CheckNode;
import org.n52.wps.test.checknodetree.CheckNodeTree;
import org.w3c.dom.Document;


public class FinishPanelDescriptor extends WizardPanelDescriptor {
    
    public static final String IDENTIFIER = "FINSIH_PANEL";
    
    private FinishPanel panel3;
    private InstallWizard wizard;
    
    public FinishPanelDescriptor(InstallWizard wizard) {
        
        panel3 = new FinishPanel(wizard);
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel3);
        
    }

    public Object getNextPanelDescriptor() {
    	WizardPanelDescriptor completedPanelDescriptor = new CompletedPanelDescriptor(getWizard());
        getWizard().registerWizardPanel(CompletedPanelDescriptor.IDENTIFIER, completedPanelDescriptor);
    	
        return CompletedPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return ProcessLocationPanelDescriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        
        
        getWizard().setNextFinishButtonEnabled(true);
        getWizard().setBackButtonEnabled(true);
        
    }
    
   
    @Override
    public void finishPage() {
    	
    	//create config
    	Map<String, Object> properties = getWizard().getProperties();
        
        	WPSConfigurationDocument doc = (WPSConfigurationDocument) getWizard().getProperties().get(InstallLocationPanelDescriptor.WPS_CCONFIG); 
			//replace elements
			
			doc.getWPSConfiguration().getServer().setHostname((String)properties.get(InstallLocationPanelDescriptor.HOSTNAME));
			doc.getWPSConfiguration().getServer().setHostport((String)properties.get(InstallLocationPanelDescriptor.HOSTPORT));
			
			//parsers
			CheckNodeTree tree1 = (CheckNodeTree) properties.get(ParserLocationPanelDescriptor.PARSERS);
			CheckNode[] selectedNodes1 = tree1.getCheckedNodes();
			
			
				
				Parser[] parsers = doc.getWPSConfiguration().getDatahandlers().getParserList().getParserArray();
				
				
				
				for(int i = 0; i<parsers.length; i++){
					boolean isIncluded = false;
					
					for(CheckNode node : selectedNodes1){
						String name =  node.toString();
						if(parsers[i].getName().equals(name)){
							isIncluded = true;
							break;
						}
					}
					if(!isIncluded){
						doc.getWPSConfiguration().getDatahandlers().getParserList().removeParser(i);
					}
				}
				
				
				
			
			//generators
			CheckNodeTree tree2 = (CheckNodeTree) properties.get(GeneratorLocationPanelDescriptor.GENERATORS);
			CheckNode[] selectedNodes2 = tree2.getCheckedNodes();
			
				
			Generator[] generators = doc.getWPSConfiguration().getDatahandlers().getGeneratorList().getGeneratorArray();
				
			for(int i = 0; i<generators.length; i++){
				boolean isIncluded = false;
				for(CheckNode node : selectedNodes2){
					String name =  node.toString();
					if(generators[i].getName().equals(name)){
						isIncluded = true;
						break;
					}
				}
				if(!isIncluded){
					doc.getWPSConfiguration().getDatahandlers().getGeneratorList().removeGenerator(i);
				}
				
				
			}
			//processes
			//repositories
			CheckNodeTree tree3 = (CheckNodeTree) properties.get(ProcessLocationPanelDescriptor.PROCESSES);
			CheckNode[] selectedNodes3 = tree3.getCheckedNodes();
			
				Repository[] repositories = doc.getWPSConfiguration().getAlgorithmRepositoryList().getRepositoryArray();
				
				for(int i = 0; i<repositories.length; i++){
					boolean isIncluded = false;
					
					for(CheckNode node : selectedNodes3){
						String name =  node.toString();
						if(repositories[i].getName().equals(name)){
							isIncluded = true;
							//special case LocalAlgorithmRepository
							if(name.equalsIgnoreCase("LocalAlgorithmRepository")){
								if(node.getChildCount()>0){
									Enumeration childs = node.children();
									while(childs.hasMoreElements()){
										CheckNode child = (CheckNode) childs.nextElement();
										if(!child.isSelected()){
											Property[] properties2 =  repositories[i].getPropertyArray();
											for(int k = 0; k<properties2.length; k++){
												if(properties2[k].getStringValue().equalsIgnoreCase(child.toString())){
													repositories[i].removeProperty(k);
												}
											}
										}
									}
								}
							}
							
						}
					
					}
					if(!isIncluded){
						doc.getWPSConfiguration().getAlgorithmRepositoryList().removeRepository(i);
					}
				}
				
			
			//writeXmlFile(doc.getDomNode().getOwnerDocument(),wpsConfigPath);
			writeXmlFile(doc.getWPSConfiguration().getDomNode().getOwnerDocument(),"c:\\test_wps_config.xml");
		
    	
    	
    	
    	
    	//copy project
        String tomcatLocation = (String) properties.get(InstallLocationPanelDescriptor.TOMCAT_LOCATION);
       /* try {
			DirectoryCopier.copyDirectory(new File(wpsConfigPath), new File(tomcatLocation));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        
        
    }    
    
    public void writeXmlFile(Document doc, String filename) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);
    
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
