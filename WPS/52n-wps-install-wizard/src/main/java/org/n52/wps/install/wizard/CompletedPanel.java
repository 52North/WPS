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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.n52.wps.install.framework.InstallWizard;

public class CompletedPanel extends JPanel {
 
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JLabel welcomeTitle;
    private JLabel yetAnotherBlankSpace1;
    
    private JPanel contentPanel;
    private JLabel iconLabel;
    private JSeparator separator;
    private JLabel textLabel;
    private JPanel titlePanel;
    
    private InstallWizard wizard;
        
    public CompletedPanel(InstallWizard wizard) {
        
        super();
        this.wizard = wizard;
        contentPanel = getContentPanel();
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
        textLabel.setText("WPS Installation Successful");
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
        
      
        welcomeTitle = new JLabel();
        jPanel1 = new JPanel();
        
        jLabel1 = new JLabel();

        contentPanel1.setLayout(new java.awt.BorderLayout());

        
        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        yetAnotherBlankSpace1 = new JLabel();

        
        jPanel1.add(yetAnotherBlankSpace1);

       

        jLabel1.setText("WPS Installation Successful");
        
        
        jPanel1.add(yetAnotherBlankSpace1);

        JLabel jLabel2 = new JLabel();
        jLabel2.setText("It will be available under http://" + wizard.getProperties().get(InstallLocationPanelDescriptor.HOSTNAME)+ ":" + wizard.getProperties().get(InstallLocationPanelDescriptor.HOSTPORT) + "/wps/WebProcessingService");
        
        jPanel1.add(new JLabel());
        jPanel1.add(new JLabel());
        jPanel1.add(jLabel1);
        jPanel1.add(yetAnotherBlankSpace1);
        jPanel1.add(jLabel2);
        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);
        return contentPanel1;
    }
    
    private ImageIcon getImageIcon() {        
        return null;
    }
    
}
