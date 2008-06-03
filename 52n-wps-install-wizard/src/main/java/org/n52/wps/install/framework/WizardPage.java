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

package org.n52.wps.install.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

public abstract class WizardPage extends JPanel{
	protected InstallWizard wizard;
	protected String heading;
	
	protected javax.swing.JLabel welcomeTitle;
	  
	protected JPanel jPanel1;
   
    
    
	protected JPanel contentPanel;
	protected JLabel iconLabel;
	protected JSeparator separator;
	protected JLabel textLabel;
	protected JPanel titlePanel;
	
	public WizardPage(InstallWizard wizard, String heading){
		super();
		this.wizard = wizard;
		init(heading);
	/*	this.heading = heading;
		
		
		contentPanel = new JPanel();
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
        textLabel.setText(heading);
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
        add(secondaryPanel, BorderLayout.WEST);*/
	}
	
	protected void init(String heading){
		
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
        textLabel.setText(heading);
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
	
	protected abstract JPanel getContentPanel();
	
	protected abstract ImageIcon getImageIcon();
}
