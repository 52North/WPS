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
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.n52.wps.install.framework.InstallWizard;


public class WelcomePanel extends JPanel {
 
    private JLabel blankSpace;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;

    private JLabel welcomeTitle;
    private JPanel contentPanel;
    
    private JLabel iconLabel;
    private ImageIcon icon;
    
    public WelcomePanel() {
        
        iconLabel = new JLabel();
        contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        icon = getImageIcon();

        setLayout(new java.awt.BorderLayout());

        if (icon != null)
            iconLabel.setIcon(icon);
        
        iconLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        
        add(iconLabel, BorderLayout.WEST);
        
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);
    }
    
    
    private JPanel getContentPanel() {
        
        JPanel contentPanel1 = new JPanel();
        JPanel jPanel1 = new JPanel();
        
        welcomeTitle = new JLabel();
        blankSpace = new JLabel();
        jLabel1 = new JLabel();
    
    

        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 11));
        welcomeTitle.setText("Welcome to the 52° North WPS Installer!");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        jPanel1.add(blankSpace);
        jLabel1.setText("This is wizard will install the 52°North WPS.");
        jPanel1.add(jLabel1);
      

        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

        return contentPanel1;
        
    }

    private ImageIcon getImageIcon() {
        return new ImageIcon(getResource("clouds.jpg"));
    }
    
    private URL getResource(String key) {

      
        String name = key;
        URL url = null;
        if (name != null) {
        	url = InstallWizard.class.getClassLoader().getResource(name);
            return url;
        } else
            return null;

    }
 
}
