package org.n52.wps.commons.context;

import java.io.File;
import java.util.UUID;

public class ExecutionContext {
    
	private String tempFolderName;
	
	public ExecutionContext(){
		tempFolderName = UUID.randomUUID().toString();
	}
	
    public String getTempDirectoryPath() {
    	
        return System.getProperty("java.io.tmpdir")+tempFolderName;
    } 
    
    // add more as needed...
}
