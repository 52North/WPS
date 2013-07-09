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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.ags.workspace.AGSWorkspace;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.feed.movingcode.AlgorithmParameterType;
import org.n52.wps.server.feed.movingcode.CommandLineParameter;
import org.n52.wps.server.feed.movingcode.MovingCodeObject;
import org.n52.wps.server.feed.movingcode.MovingCodeUtils;



public class ArcToolboxProcessDelegator implements IAlgorithm{
	
	private static Logger LOGGER = LoggerFactory.getLogger(ArcToolboxProcessDelegator.class);
	private AGSWorkspace agsWorkspace;
	
	private final File toolWorkspace;
	private final File workspaceBase;
	
	private MovingCodeObject mco;
	protected List<String> errors;
	
	private String[] toolParameters;
	
	
	protected ArcToolboxProcessDelegator(MovingCodeObject templateMCO, File workspaceBase) throws IOException{
		this.workspaceBase = workspaceBase;
		errors = new ArrayList<String>();
		mco = templateMCO.createChild(workspaceBase);
		toolParameters = new String[mco.getParameters().size()];
		toolWorkspace = new File(mco.getInstanceWorkspace().getCanonicalPath());
	}
		
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//initialize arcObjects
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		
		//create the workspace
		agsWorkspace = new AGSWorkspace(toolWorkspace);
		
		//retrieve path to the toolbox
		String toolbox = toolWorkspace.getAbsolutePath() + mco.getAlgorithmURL().getPublicPath();
		//retrieve the tool
		String tool = mco.getAlgorithmURL().getPrivatePath();
		
		List<AlgorithmParameterType> params = mco.getParameters();
		HashMap<String, String> outputs = new HashMap<String,String>();
		System.gc();
		
		// assign parameters
		for (AlgorithmParameterType currentParam : params){
			String wpsInputID = currentParam.getWpsInputID();
			String wpsOutputID = currentParam.getWpsOutputID();
			int positionID = currentParam.getPositionID().intValue();
			
			CommandLineParameter cmdParam = null;
			
			// input parameters
			if(!wpsInputID.equalsIgnoreCase("")){
				if(inputData.containsKey(wpsInputID)){
					//open the IData list and iterate through it
					List<IData> dataItemList = inputData.get(wpsInputID);
					Iterator<IData> it = dataItemList.iterator();
					//create CommanLineParameter Object
					cmdParam = new CommandLineParameter(currentParam.getPrefixString(), currentParam.getSuffixString(), currentParam.getSeparatorString());
					while (it.hasNext()){
						IData currentItem = it.next();
						// load as file and add to CommanLineParameter
						cmdParam.addValue((MovingCodeUtils.loadSingleDataItem(currentItem, toolWorkspace)));
					}
				}
			} else if (!wpsOutputID.equalsIgnoreCase("")){ // output only parameters !!ONLY SINGLE OUTPUT ITEMS SUPPORTED BY WPS!!
				// create CommanLineParameter Object
				cmdParam = new CommandLineParameter(currentParam.getPrefixString(), currentParam.getSuffixString(), currentParam.getSeparatorString());
				
				// retrieve the default mimeType
				String mimeType = mco.getDefaultMimeType(wpsOutputID);
				
				// prepare output filename
				String extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(mimeType);
				String fileName = UUID.randomUUID().toString().substring(0,7) + "." + extension; // geoprocessor can't handle points, dashes etc in output file name
//				String fileName = UUID.randomUUID() + "." + extension;
				fileName = toolWorkspace.getAbsolutePath() + File.separator + fileName;
				cmdParam.addValue(fileName);
			}
			
			//prepare the output - files only
			if (!wpsOutputID.equalsIgnoreCase("")){
				String fileName = cmdParam.getAsPlainString();
				outputs.put(wpsOutputID, fileName);
			}
			
			
			// create a new parameter in the tool's Parameter Array
			if (cmdParam == null){
				toolParameters[positionID] = "";
			} else {
				toolParameters[positionID] = cmdParam.getAsCommandString();
			}
			
		}
		
		//execute
		LOGGER.info("Executing ArcGIS tool " + toolbox + File.pathSeparator + tool + " . Parameter array contains " + toolParameters.length + " parameters.");
		try {
			agsWorkspace.executeGPTool(tool, toolbox, toolParameters);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			errors.add(e1.getMessage());
			throw new RuntimeException(e1.getMessage()); // otherwise WPS tries to zip and return non-existing files => null pointer
		}
		
		//create the output - files only
		HashMap<String, IData> result = new HashMap<String, IData>();
		for (String wpsOutputID : outputs.keySet()){
			// create File object
			File currentFile = new File (outputs.get(wpsOutputID));
			GenericFileData outputFileData;
			try {
				// create the GenericFileData object
				outputFileData = new GenericFileData(currentFile, mco.getDefaultMimeType(wpsOutputID));
				// put result on output map
				result.put(wpsOutputID, new GenericFileDataBinding(outputFileData));
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not read output file: " + outputs.get(wpsOutputID));
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error("Could not create output file from: " + outputs.get(wpsOutputID));
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public ProcessDescriptionType getDescription() {
		return mco.getProcessDescription();
	}

	public String getWellKnownName() {
		return mco.getProcessID();
	}

	public boolean processDescriptionIsValid() {
		return mco.getProcessDescription().validate();
	}

	public Class<?> getInputDataType(String id) {
		return MovingCodeUtils.getInputDataType(mco, id);
	}

	public Class<?> getOutputDataType(String id) {
		return MovingCodeUtils.getOutputDataType(mco, id);
	}
	
	//delete the current workspace
	protected void finalize(){
		try {
			FileUtils.deleteDirectory(workspaceBase);
		} catch (IOException e) {
			LOGGER.error("Could not delete dead workspace:\n" + workspaceBase.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
}
