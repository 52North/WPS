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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import noNamespace.WPSConfigurationDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.install.framework.InstallWizard;



public class InstallLocationPanel extends JPanel {
 
    
    private javax.swing.JLabel welcomeTitle;
  
    private JPanel jPanel1;
    private JTextField tomcatLocation;
    private JTextField hostNameTextField;
    private JTextField hostPortTextField;
    private JFileChooser fileChooser;
    
    
    private JPanel contentPanel;
    private JLabel iconLabel;
    private JSeparator separator;
    private JLabel textLabel;
    private JPanel titlePanel;
    
    private InstallWizard wizard;
        
    public InstallLocationPanel(InstallWizard installWizard) {
     
        super();
        this.wizard = installWizard;
                
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
        textLabel.setText("Tomcat Location");
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
        
       /* 
        String wpsConfigPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    	wpsConfigPath = URLDecoder.decode(wpsConfigPath);
    	wpsConfigPath = wpsConfigPath.replace("\\", "/");
    	wpsConfigPath = wpsConfigPath.replace("/bin", "/conf/wps_config.xml");
    	wpsConfigPath = wpsConfigPath.replace("file:/", "");*/
        
        String wpsConfigPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
    	wpsConfigPath = URLDecoder.decode(wpsConfigPath);
    	wpsConfigPath = wpsConfigPath.replace("\\", "/");
    	wpsConfigPath = wpsConfigPath.replace("file:/", "");
        int index = wpsConfigPath.indexOf("WEB-INF");
    	wpsConfigPath = wpsConfigPath.substring(0,index);
    	wpsConfigPath = wpsConfigPath + "/WEB-INF/classes/org/n52/wps/server/wps_config.xml";
    	
    	
        try {
			WPSConfigurationDocument doc = WPSConfigurationDocument.Factory.parse(new File(wpsConfigPath));
			installWizard.addProperties(InstallLocationPanelDescriptor.WPS_CCONFIG, doc);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }  
    
   
    
   
    
    private JPanel getContentPanel() {     
        
        JPanel contentPanel1 = new JPanel();
        
        
        welcomeTitle = new javax.swing.JLabel();
        JPanel panel0 = new JPanel(new GridLayout(2,1));
        jPanel1 = new javax.swing.JPanel();
       
        
        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setText("Please enter your tomcat base directory");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

        //jPanel1.setLayout(new java.awt.GridLayout(0, 2));

        tomcatLocation = new JTextField();
        tomcatLocation.setPreferredSize(new Dimension(300,20));
        DocumentListener listener = new TomcatLocationListener(wizard, this);
        tomcatLocation.getDocument().addDocumentListener(listener);
        jPanel1.add(tomcatLocation);
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JFrame frame = new JFrame();
        Action openAction = new OpenFileAction(frame, fileChooser, tomcatLocation);
        JButton openButton = new JButton(openAction);
        openButton.setPreferredSize(new Dimension(100,20));
       
        
        JPanel panel2 = new JPanel(new GridLayout(2,2));
        jPanel1.add(openButton);
        
        JLabel space = new JLabel();
        jPanel1.add(space);
        JLabel hostName = new JLabel("hostname");
        panel2.add(hostName);
        hostNameTextField = new JTextField();
        hostNameTextField.getDocument().addDocumentListener(listener);
        panel2.add(hostNameTextField);
        JLabel hostPort = new JLabel("hostport");
        panel2.add(hostPort);
        hostPortTextField = new JTextField();
        hostPortTextField.getDocument().addDocumentListener(listener);
        panel2.add(hostPortTextField);
        
        panel0.add(jPanel1);
        panel0.add(panel2);
        contentPanel1.add(panel0, java.awt.BorderLayout.CENTER);

        
        return contentPanel1;
    }
    
    private ImageIcon getImageIcon() {
        
        //  Icon to be placed in the upper right corner.
        
        return null;
    }

    public String getTomcatLocation(){
    	return tomcatLocation.getText();
    }
    
    public String getHostName(){
    	return hostNameTextField.getText();
    }

    public String getHostPort(){
    	return hostPortTextField.getText();
    }


	public boolean isLocationEntered() {
		if(tomcatLocation.getText().length()>0){
			return true;
		}else{
			return false;
		}
	}
    
    

}
