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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.n52.wps.install.framework.InstallWizard;
import org.n52.wps.install.framework.WizardPanelDescriptor;


public class InstallLocationPanelDescriptor extends WizardPanelDescriptor implements ActionListener {
    
    public static final String IDENTIFIER = "InstallLocation_Panel";
    public static final String TOMCAT_LOCATION = "Tomcat Location";
	public static final String HOSTNAME = "Hostname";
	public static final String HOSTPORT = "Hostport";
	public static final String WPS_CCONFIG = "WPS Config";
    
    
    InstallLocationPanel panel2;
    
    public InstallLocationPanelDescriptor(InstallWizard wizard) {
        
    	super(wizard);
    	
        panel2 = new InstallLocationPanel(getWizard());
       // panel2.addCheckBoxActionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel2);
        
    }
    
    public Object getNextPanelDescriptor() {
    	  
        WizardPanelDescriptor parserDescriptor = new ParserLocationPanelDescriptor(getWizard());
        getWizard().registerWizardPanel(ParserLocationPanelDescriptor.IDENTIFIER, parserDescriptor);

        return ParserLocationPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return WelcomePanelDescriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        setNextButtonAccordingToCheckBox();
    }    

    public void actionPerformed(ActionEvent e) {
        setNextButtonAccordingToCheckBox();
    }
            
    
    private void setNextButtonAccordingToCheckBox() {
        if (panel2.isLocationEntered() && !panel2.getHostName().equals("") && !panel2.getHostPort().equals(""))
            getWizard().setNextFinishButtonEnabled(true);
         else
            getWizard().setNextFinishButtonEnabled(false);           
    
    }
    
    @Override
    public void finishPage(){
    	if(!panel2.getTomcatLocation().endsWith("webapps")){
    		if(panel2.getTomcatLocation().endsWith("/")||panel2.getTomcatLocation().endsWith("\\")){
    			getWizard().addProperties(TOMCAT_LOCATION, panel2.getTomcatLocation()+"webapps");
    		}else{
    			getWizard().addProperties(TOMCAT_LOCATION, panel2.getTomcatLocation()+"/webapps");
    		}
    	}
    	
    	getWizard().addProperties(HOSTNAME, panel2.getHostName());
    	getWizard().addProperties(HOSTPORT, panel2.getHostPort());
    }
}

