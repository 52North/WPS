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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.install.framework.InstallWizard;
import org.n52.wps.test.checknodetree.CheckNode;
import org.n52.wps.test.checknodetree.CheckNodeTree;


public class ProcessLocationPanel extends JPanel {
 
    
    private javax.swing.JLabel welcomeTitle;
  
    private JPanel jPanel1;
   
    
    
    private JPanel contentPanel;
    private JLabel iconLabel;
    private JSeparator separator;
    private JLabel textLabel;
    private JPanel titlePanel;
    
    private CheckNodeTree tree;
    private InstallWizard wizard;
        
    public ProcessLocationPanel(InstallWizard wizard) {
     
        super();
        this.wizard = wizard;
        contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        ImageIcon icon = getImageIcon();
        
        titlePanel = new javax.swing.JPanel();
        textLabel = new javax.swing.JLabel();
        iconLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();

        setLayout(new java.awt.BorderLayout());


        titlePanel.setLayout(new java.awt.BorderLayout());
        titlePanel.setBackground(Color.gray);
        
        textLabel.setBackground(Color.gray);
        textLabel.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
        textLabel.setText("Process");
        textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        textLabel.setOpaque(true);

        iconLabel.setBackground(Color.gray);
        if (icon != null)
            iconLabel.setIcon(icon);
        
        titlePanel.add(textLabel, BorderLayout.CENTER);
        titlePanel.add(iconLabel, BorderLayout.EAST);
        titlePanel.add(separator, BorderLayout.SOUTH);
        
        add(titlePanel, BorderLayout.NORTH);
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.WEST);

    }  
    
   
    
   
    
    private JPanel getContentPanel() {     
        
        JPanel contentPanel1 = new JPanel();
        
        
        welcomeTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        String[] localProcesses = null;
        String[] repositories = null;
        try {
        	repositories = getRepositories();
        	localProcesses = getLocalProcesses();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        CheckNode[] nodes = getCheckNodes(repositories, localProcesses, "Repositories");
        tree = new CheckNodeTree(nodes);
        jPanel1.add(tree);
        
        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setText("Please select the parsers you want to install");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);
       
        
        
        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

        
        return contentPanel1;
    }
    
    private CheckNode[] getCheckNodes(String[] repositories, String[] localProcesses, String root){
    		CheckNode[] nodes = new CheckNode[repositories.length+1];
    		CheckNode rootNode = new CheckNode(root);
    		nodes[0] = rootNode;
    		for (int i=1;i<repositories.length+1;i++) {
    			String repository = repositories[i-1];
    			nodes[i] = new CheckNode(repository);
    			rootNode.add(nodes[i]);	
    			nodes[i].setSelected(true);
    			if(repository.equals("LocalAlgorithmRepository")){
    				for(String process : localProcesses){
    					CheckNode processNode = new CheckNode(process);
    					processNode.setSelected(true);
    					nodes[i].add(processNode);
    					
    				}
    			
    			}
    		}
    		return nodes;
	}





	private String[] getRepositories() throws XmlException, IOException {
		WPSConfigurationDocument doc = (WPSConfigurationDocument) wizard.getProperties().get(InstallLocationPanelDescriptor.WPS_CCONFIG);
    	Repository[] repositoryList = doc.getWPSConfiguration().getAlgorithmRepositoryList().getRepositoryArray();
    	String[] repositoryStringsList = new String[repositoryList.length];
    	for(int i = 0; i< repositoryStringsList.length; i++){
    		String repositoryName = repositoryList[i].getName();
    		repositoryStringsList[i]=repositoryName;
    	}
    	return repositoryStringsList;

	}





	private String[] getLocalProcesses() throws XmlException, IOException {
		WPSConfigurationDocument doc = (WPSConfigurationDocument) wizard.getProperties().get(InstallLocationPanelDescriptor.WPS_CCONFIG);
    	Repository[] repositoryList = doc.getWPSConfiguration().getAlgorithmRepositoryList().getRepositoryArray();
    	ArrayList processList = new ArrayList();
    	for(int i = 0; i< repositoryList.length; i++){
    		String repositoryName = repositoryList[i].getName();
    		if(repositoryName.equals("LocalAlgorithmRepository") && repositoryList[i].getClassName().equalsIgnoreCase("org.n52.wps.server.LocalAlgorithmRepository")){
    			Repository repository = repositoryList[i];
    			Property[] properties = repository.getPropertyArray();
    			for(Property property : properties){
    				if(property.getName().equals("Algorithm")){
    					processList.add(property.getStringValue());
    				}
    			}
    			
    		}
    	}
    	String[] result = new String[processList.size()];
    	for(int i = 0; i< result.length; i++){
    		result[i] = (String) processList.get(i);
    	}
    	
    	return result;
	}


	CheckNodeTree getTree(){
    	return tree;
    }


	private ImageIcon getImageIcon() {
        
        //  Icon to be placed in the upper right corner.
        
        return null;
    }
    
    

}
