/***************************************************************
Copyright © 2010 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

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

package org.n52.wps.ags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.legacy.AbstractLegacyProcessDelegator;
import org.n52.wps.server.legacy.LegacyParameter;
import org.n52.wps.server.legacy.LegacyProcessDescription;

@Deprecated
public class LegacyAGSProcessDelegator extends AbstractLegacyProcessDelegator{
	
	private static Logger LOGGER = Logger.getLogger(LegacyAGSProcessDelegator.class);
	private AGSWorkspace agsWorkspace;
	private LegacyParameter[] parameterDescriptions;
	private String[] toolParameters;
	
	private static final String[] BACKEND_NAMES = {"urn:n52:esri:arcgis:9.3.1", "urn:n52:esri:arcgis:9.3"};
	private static final String[] CONTAINER_NAMES = {"urn:n52:esri:arctoolbox:9.3"};
	
	
	// empty constructor for custom ArcGIS Tools
	public LegacyAGSProcessDelegator(){
		
		// overwrite the abstract parent's variables
		
		try{
			ArrayList<URI> uriList = new ArrayList<URI>();
			for(String currentBackend : BACKEND_NAMES){
				uriList.add(new URI(currentBackend));
			}
			supportedBackends = uriList.toArray(new URI[uriList.size()]);
			
			uriList = new ArrayList<URI>();
			for(String currentContainer : CONTAINER_NAMES){
				uriList.add(new URI(currentContainer));
			}
			supportedContainers = uriList.toArray(new URI[uriList.size()]);
		}
		catch (URISyntaxException e) {
			LOGGER.error("Invalid Backend or Container URIs.");
			e.printStackTrace();
		}
		
	}
		
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//initialize arcObjects
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		
		//create the workspace
		agsWorkspace = new AGSWorkspace();
		this.workspaceDir = agsWorkspace.getWorkspace();
		
		//copy template workspace content
		File sourceDir = this.templateWorkspace;
		File targetDir = new File(agsWorkspace.getWorkspace().getAbsolutePath());
		try {
			this.copyDirectory(sourceDir, targetDir);
		}
		catch (IOException e1) {
			errors.add("Could not copy template workspace.");
		}
		
		//set toolbox and tool name
		String absWSName = this.getLegacyDescription().getAlgorithmWorkspaceLocation().toString();
		String absTBXLocation = this.getLegacyDescription().getAlgorithmContainerLocation().toString();
		String relTBXLocation = absTBXLocation.substring(absWSName.length() + 1, absTBXLocation.length()); // +1 for the leading slash
		String toolbox = this.getLegacyDescription().getAlgorithmContainerLocation().toString();
		toolbox = targetDir + File.separator + relTBXLocation;
		
		String tool = this.getLegacyDescription().getAlgorithmLocation().getQuery();
		
		this.parameterDescriptions = new LegacyParameter[this.getLegacyDescription().getParamsAsArray().length];
		this.parameterDescriptions = this.getLegacyDescription().getParamsAsArray();
		this.toolParameters = new String[this.parameterDescriptions.length];
		
		
		//Assign the parameters
		for (int i=0; i < this.parameterDescriptions.length; i++){
			
			LegacyParameter currentParam = this.parameterDescriptions[i];
			
			// input parameters
			if(currentParam.isInput){
				if(inputData.containsKey(currentParam.wpsInputID)){
					//open the IData list and iterate through it
					List<IData> dataItemList = inputData.get(currentParam.wpsInputID);
					Iterator<IData> it = dataItemList.iterator();
					ArrayList<String> valueList = new ArrayList<String>();
					while (it.hasNext()){
						IData currentItem = it.next();
						valueList.add(this.loadSingleDataItem(currentItem));
					}
					
					String[] valueArray = valueList.toArray(new String[0]);
					
					this.toolParameters[i] = this.calcToolParameterString(" ", valueArray);
				}
				else{
					throw new RuntimeException("Error while allocating input parameter " + currentParam.wpsInputID);
				}
			}
			
			//output only parameters
			if(currentParam.isOutput && !currentParam.isInput){
				if(currentParam.isComplex){
					
					String extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(currentParam.mimeType);
					String fileName = System.currentTimeMillis() + "." + extension;
					
					fileName = this.addOutputFile(fileName);
					this.toolParameters[i] = fileName;
				}
			}
		}
		
		
		//execute
		LOGGER.info("Executing ArcGIS tool " + toolbox + File.pathSeparator + tool + " . Parameter array contains " + this.parameterDescriptions.length + " parameters.");
		agsWorkspace.executeGPTool(tool, toolbox, this.toolParameters);
		
		//create the output
		HashMap<String, IData> result = new HashMap<String, IData>();
		
		for (int i=0; i < this.parameterDescriptions.length; i++){
			LegacyParameter currentParam = this.parameterDescriptions[i];
			
			if(currentParam.isOutput){
				
				if(currentParam.isComplex){
					String fileName = this.toolParameters[i];
					//GenericFileData outputFileData = new GenericFileData(this.workspace.getFileAsStream(fileName), currentParam.mimeType);
					
					File currentFile = new File (fileName);
					GenericFileData outputFileData;
					try {
						outputFileData = new GenericFileData(currentFile, currentParam.mimeType);
						result.put(currentParam.wpsOutputID, new GenericFileDataBinding(outputFileData));
					} catch (FileNotFoundException e) {
						LOGGER.error("Could not read output file: " + fileName);
						e.printStackTrace();
					} catch (IOException e) {
						LOGGER.error("Could not create output file from: " + fileName);
						e.printStackTrace();
					}
				}
				
				if(currentParam.isLiteral){
					// not implemented
				}
				if(currentParam.isCRS){
					// not implemented
				}
			}
		}
		
		return result;
	}
	
	
	private final String calcToolParameterString(String valueSeparator, String[] valueArray){
		
		String returnValue = null;
		boolean firstrun = true;
		
		for(String currentValue : valueArray){
			if (firstrun){
				firstrun = false;
				returnValue = currentValue;
			}
			else {
				returnValue = returnValue + valueSeparator + currentValue;
			}
		}
		
		return returnValue;
	}
	
	
    // If targetLocation does not exist, it will be created.
    public void copyDirectory(File sourceLocation , File targetLocation)
    throws IOException {
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
    
	public final boolean isSupportedProcess(LegacyProcessDescription legacyDescription) {
		
		boolean success = true;
		
		success = legacyDescription.isSequential();
			
		// check container
		if (!(this.isSupportedContainer(legacyDescription.getAlgorithmContainerURN()) && success)){
			success = false;
		}
			
		// check legacy backends
		if (legacyDescription.getLegacyBackends() != null){
			for(URI currentBackend : legacyDescription.getLegacyBackends()){
				if (!(this.isSupportedBackend(currentBackend) && success)){
					success = false;
				}
			}
		}
		else{
			LOGGER.warn("Legacy Description is NULL!");
		}
		return success;
	}
	
	private final String addOutputFile (String fileName){
		String newFileName = agsWorkspace.getWorkspace().getAbsolutePath() + "\\" + fileName;
		return newFileName;
	}
	
}
