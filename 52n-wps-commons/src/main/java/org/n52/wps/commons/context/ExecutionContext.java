package org.n52.wps.commons.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.opengis.wps.x100.OutputDefinitionType;

public class ExecutionContext {
    
	private String tempFolderName;
	private List<OutputDefinitionType> outputDefinitionTypes;
    
    public ExecutionContext(){
		this(Arrays.asList(new OutputDefinitionType[0]));
	}
    
    public ExecutionContext(OutputDefinitionType output){
		this(Arrays.asList(output != null ? new OutputDefinitionType[] { output } : new OutputDefinitionType[0]));
	}
    
	public ExecutionContext(List<? extends OutputDefinitionType> outputs){
		tempFolderName = UUID.randomUUID().toString();
        this.outputDefinitionTypes = Collections.unmodifiableList(
                outputs != null ? outputs : Arrays.asList(new OutputDefinitionType[0]));
	}
	
    public String getTempDirectoryPath() {
    	
        return System.getProperty("java.io.tmpdir")+tempFolderName;
    }
    
    public List<OutputDefinitionType> getOutputs() {
        return outputDefinitionTypes;
    }
}
