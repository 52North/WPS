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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.install.framework.InstallWizard;
import org.n52.wps.test.checknodetree.CheckNodeTree;


public class ParserLocationPanel extends JPanel {
 
    
    private javax.swing.JLabel welcomeTitle;
  
    private JPanel jPanel1;
   
    
    
    private JPanel contentPanel;
    private JLabel iconLabel;
    private JSeparator separator;
    private JLabel textLabel;
    private JPanel titlePanel;
    private CheckNodeTree tree;
    
    private InstallWizard wizard;
        
    public ParserLocationPanel(InstallWizard wizard) {
     
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
        textLabel.setText("Parsers");
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
        String[] parsers = null;
        try {
			parsers = getParsers();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        tree = new CheckNodeTree(parsers, "Parsers");
        jPanel1.add(tree);
        
        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setText("Please select the parsers you want to install");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);
       
        
        
        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

        
        return contentPanel1;
    }
    
    private String[] getParsers() throws XmlException, IOException {
    	
    	WPSConfigurationDocument doc = (WPSConfigurationDocument) wizard.getProperties().get(InstallLocationPanelDescriptor.WPS_CCONFIG);
    	Parser[] parserList = doc.getWPSConfiguration().getDatahandlers().getParserList().getParserArray();
    	String[] parserStringList = new String[parserList.length];
    	for(int i = 0; i< parserList.length; i++){
    		String parserName = parserList[i].getName();
    		parserStringList[i] = parserName;
    	}
		return parserStringList;
	}


    public CheckNodeTree getTree(){
    	return tree;
    }


	private ImageIcon getImageIcon() {
        
        //  Icon to be placed in the upper right corner.
        
        return null;
    }





	
    

}
