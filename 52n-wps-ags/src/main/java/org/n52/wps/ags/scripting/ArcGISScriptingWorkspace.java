/***************************************************************
Copyright © 2010 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden; Christin Henzen, TU Dresden
 
 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.ags.scripting;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.n52.wps.ags.AGSProperties;

public class ArcGISScriptingWorkspace{
	
	private static Logger LOGGER = Logger.getLogger(ArcGISScriptingWorkspace.class);
	private static final String WORKSPACE_BASE = AGSProperties.getInstance().getWorkspaceBase();
	private static final String COMMAND = "cmd /c";
	
	protected final File workspaceDir;
	
	public ArcGISScriptingWorkspace(){
		String directory = WORKSPACE_BASE + File.separator + String.valueOf(System.currentTimeMillis());
		workspaceDir = new File(directory);
		workspaceDir.mkdir();
	}
	
	public void executePythonScript(String scriptPath, String[] parameters){
		
		String command = COMMAND + " " + scriptPath;
		
		for (String currentParam : parameters){
			command = command + " " + currentParam;
		}
		
		
		LOGGER.info("Executing " + command);
		LOGGER.info("Workspace is: " + workspaceDir.getAbsolutePath());
		
		try {
			Process p = Runtime.getRuntime().exec(command, null, workspaceDir);
			p.waitFor();
			if (p.exitValue() == 0){
				LOGGER.info("Successfull termination of " + scriptPath);
			}
			else {
				LOGGER.info("Abnormal termination of " + scriptPath);
				LOGGER.info("Errorlevel / Exit Value: " + p.exitValue());
			}
		}
		catch (IOException e) {
			LOGGER.error("Error while executing " + scriptPath);
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOGGER.error(scriptPath + " got interrupted!");
			e.printStackTrace();
		}
	}
	
	
	public File getWorkspace() {
		return this.workspaceDir;
	}
	
	//delete the current workspace
	protected void finalize(){

		if (deleteDirectory(workspaceDir)){
			LOGGER.info("Workspace successfully deleted!");
		}
		else {
			LOGGER.info("Workspace could not be deleted :-(");
		}
		
		//just to be safe - call the destructor from the superior class
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static final boolean deleteDirectory(File dirPath) {
		if( dirPath.exists() ) {
			File[] files = dirPath.listFiles();
		    for(int i = 0; i < files.length; i++) {
		    	if(files[i].isDirectory()) {
		    		deleteDirectory(files[i]);
		        }
		        else files[i].delete();
		    }
		}
		return( dirPath.delete() );
	}
	
}
