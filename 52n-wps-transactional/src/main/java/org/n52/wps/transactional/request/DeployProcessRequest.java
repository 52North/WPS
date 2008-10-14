/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

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


package org.n52.wps.transactional.request;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DeployProcessRequest implements ITransactionalRequest{

	protected ProcessDescriptionType processDescription;
	protected DeploymentProfile deploymentProfile;
	
	
	public DeployProcessRequest(Document doc) {
		//TODO this can be achieved in a better way using xml beans
		//quick and dirty hack made on a plane....
		NodeList childs = doc.getFirstChild().getChildNodes();
		if(childs.getLength()==2){
			//have to do it twice, because we want the process id first
			for(int i = 0; i <2; i++){
				Node child = childs.item(i);
				if(child.getNodeName().contains("ProcessDescription")){
					try {
						processDescription = ProcessDescriptionType.Factory.parse(child);
					} catch (XmlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for(int j = 0; j <2; j++){
				Node child = childs.item(j);
				if(child.getNodeName().contains("ProcessDescription")){
					//do nothing, we have it already
				}else{
					String processID = processDescription.getIdentifier().getStringValue();
					
					//extract request profile schema
					String schema = null;;
					//TODO choose a proper way to do that, xpath or so, again quick airport hack...
					NodeList nodeList = child.getChildNodes();
					for(int k = 0; k<nodeList.getLength(); k++){
						Node node = nodeList.item(k);
						if(node!= null && node.getChildNodes().getLength()>0){
							if(node.getFirstChild().getNodeName().equals("wps:Schema")){
								schema = node.getFirstChild().getNodeValue();
							}
						}
						
					}
					if(schema == null){
						throw new RuntimeException("Error. Could not find supported deployment profile");
					}
					String deployManagerClass = TransactionalHelper.getDeploymentManagerForSchema(schema);
					Constructor<?> constructor;
					try {
						constructor = Class.forName(deployManagerClass).getConstructor(String.class, Node.class);
						this.deploymentProfile = (DeploymentProfile) constructor.newInstance(processID, child);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("Error. Could not find supported deployment profile");
					}
				}
			}
			
		}
			
			
		
		
	}


	public ProcessDescriptionType getProcessDescription() {
		return processDescription;
	}


	public DeploymentProfile getDeploymentProfile() {
		return deploymentProfile;
	}
	
	 
	
	

}
