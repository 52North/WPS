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

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.n52.wps.install.framework.InstallWizard;

public class TomcatLocationListener implements DocumentListener{

	private InstallWizard wizard;
	private InstallLocationPanel textField;
	
	public TomcatLocationListener(InstallWizard wizard, InstallLocationPanel installLocationPanel){
		this.wizard = wizard;
		this.textField = installLocationPanel;
	}
	
	

	public void changedUpdate(DocumentEvent e) {
		check();
	}

	public void insertUpdate(DocumentEvent e) {
		check();
		
	}

	public void removeUpdate(DocumentEvent e) {
		check();
		
	}
	
	private void check(){
		if (textField.isLocationEntered() && !textField.getHostName().equals("") && !textField.getHostPort().equals(""))
            wizard.setNextFinishButtonEnabled(true);
         else
            wizard.setNextFinishButtonEnabled(false); 
	}

}
