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


public class ParserLocationPanelDescriptor extends WizardPanelDescriptor implements ActionListener {
    
    public static final String IDENTIFIER = "ParserLocation_PANEL";
    public static final String PARSERS = "ParserLocation_PANEL";
    
    ParserLocationPanel panel2;
    
    public ParserLocationPanelDescriptor(InstallWizard wizard) {
        
        panel2 = new ParserLocationPanel(wizard);
       // panel2.addCheckBoxActionListener(this);
        
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel2);
        
    }
    
    public Object getNextPanelDescriptor() {
    	WizardPanelDescriptor generatorDescriptor = new GeneratorLocationPanelDescriptor(getWizard());
        getWizard().registerWizardPanel(GeneratorLocationPanelDescriptor.IDENTIFIER, generatorDescriptor);
       
        return GeneratorLocationPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return InstallLocationPanelDescriptor.IDENTIFIER;
    }
    
    
    public void aboutToDisplayPanel() {
        setNextButtonAccordingToCheckBox();
    }    

    public void actionPerformed(ActionEvent e) {
        setNextButtonAccordingToCheckBox();
    }
            
    
    private void setNextButtonAccordingToCheckBox() {
        /* if (panel2.isCheckBoxSelected())
            getWizard().setNextFinishButtonEnabled(true);
         else
            getWizard().setNextFinishButtonEnabled(false);           
    */
    }
    
    @Override
    public void finishPage(){
    	getWizard().addProperties(PARSERS, panel2.getTree());
    }
}
