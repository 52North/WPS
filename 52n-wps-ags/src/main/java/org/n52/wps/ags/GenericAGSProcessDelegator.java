/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

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

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.ags.workspace.AGSWorkspace;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.IAlgorithm;


public class GenericAGSProcessDelegator implements IAlgorithm{
	
	private static Logger LOGGER = LoggerFactory.getLogger(GenericAGSProcessDelegator.class);
	private final String processID;
	private List<String> errors;
	private AGSWorkspace workspace;
	protected final ToolParameter[] parameterDescriptions;
	private String[] toolParameters;
	private final int parameterCount;
	private final ProcessDescriptionType processDescription;
	private final File instanceWorkspace;
	
	
	
	public GenericAGSProcessDelegator(File workspace, String processID, ToolParameter[] legacyParameters, ProcessDescriptionType processDescription) {
		errors = new ArrayList<String>();
		this.processID = processID;
		this.parameterDescriptions = legacyParameters;
		this.parameterCount = legacyParameters.length;
		this.processDescription = processDescription;
		this.toolParameters = new String[this.parameterCount];
		this.instanceWorkspace = workspace;
		
	}
	
	
	public ProcessDescriptionType getDescription() {
		return processDescription;
	}

	public List<String> getErrors() {
		return errors;
	}
	
	
	public Class<?> getInputDataType(String id) {
		InputDescriptionType[] inputs = this.getDescription().getDataInputs().getInputArray();
		
		for(InputDescriptionType input : inputs){
			
			//Literal Input
			if(input.isSetLiteralData()){
				String datatype = input.getLiteralData().getDataType().getStringValue();
				if(datatype.equalsIgnoreCase("string")){
					return LiteralStringBinding.class;
				}
				else if(datatype.equalsIgnoreCase("boolean")){
					return LiteralBooleanBinding.class;
				}
				else if(datatype.equalsIgnoreCase("float")){
					return LiteralFloatBinding.class;
				}
				else if(datatype.equalsIgnoreCase("double")){
					return LiteralDoubleBinding.class;
				}
				else if(datatype.equalsIgnoreCase("int")){
					return LiteralIntBinding.class;
				}
				else if(datatype.equalsIgnoreCase("integer")){
					return LiteralIntBinding.class;
				}
			}
			
			//Complex Output
			else if(input.isSetComplexData()){
				return GenericFileDataBinding.class;
			}
		}
		
		return null;
	}

	public Class<?> getOutputDataType(String id) {
		OutputDescriptionType[] outputs = this.getDescription().getProcessOutputs().getOutputArray();
		
		for(OutputDescriptionType output : outputs){
			
			//Literal Output
			if(output.isSetLiteralOutput()){
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if(datatype.equalsIgnoreCase("string")){
					return LiteralStringBinding.class;
				}
				else if(datatype.equalsIgnoreCase("boolean")){
					return LiteralBooleanBinding.class;
				}
				else if(datatype.equalsIgnoreCase("float")){
					return LiteralFloatBinding.class;
				}
				else if(datatype.equalsIgnoreCase("double")){
					return LiteralDoubleBinding.class;
				}
				else if(datatype.equalsIgnoreCase("int")){
					return LiteralIntBinding.class;
				}
				else if(datatype.equalsIgnoreCase("integer")){
					return LiteralIntBinding.class;
				}
			}
			
			//Complex Output
			else if(output.isSetComplexOutput()){
				return GenericFileDataBinding.class;
			}
		}
		return null;
	}
		
	
	public String getWellKnownName() {
		return processID;
	}

	
	public boolean processDescriptionIsValid() {
		return this.getDescription().validate();
	}

	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		
		//initialize arcObjects
		AGSProperties.getInstance().bootstrapArcobjectsJar();
		
		//create the workspace
		this.workspace = new AGSWorkspace(instanceWorkspace);
		
		//Assign the parameters
		for (int i=0; i<this.parameterCount; i++){
			
			ToolParameter currentParam = this.parameterDescriptions[i];
			
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
					
					//TODO verify: isnt the input separator acutally an " ; "?
					this.toolParameters[i] = this.calcToolParameterString(" ", valueArray);
				}
				else{
					if(currentParam.isOptional){
						this.toolParameters[i] = null;
					}else{
						errors.add("Error while allocating input parameter " + currentParam.wpsInputID);
						throw new RuntimeException("Error while allocating input parameter " + currentParam.wpsInputID);
						
					}
				}
			}
			
			//output only parameters
			else if(currentParam.isOutput && !currentParam.isInput){
				if(currentParam.isComplex){
					String extension = "";
					if(currentParam.schema != null && currentParam.schema.length()>0){
						//we have vector data. So use a shp file.
						extension = "shp";
					}else{
						extension = GenericFileDataConstants.mimeTypeFileTypeLUT().get(currentParam.mimeType);
					}
					String fileName = UUID.randomUUID().toString().substring(0,7) + "." + extension; // geoprocessor can't handle points, dashes etc in output file name
					
					fileName = this.addOutputFile(fileName);
					this.toolParameters[i] = fileName;
				}
			}
		}
		
		
		//execute
		String toolName = this.processDescription.getTitle().getStringValue();
		LOGGER.info("Executing ArcGIS tool " + toolName + " . Parameter array contains " + this.parameterCount + " parameters.");
		try {
			workspace.executeGPTool(toolName, null, this.toolParameters);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			errors.add(e1.getMessage());
			throw new RuntimeException(e1.getMessage()); // otherwise WPS tries to zip and return non-existing files => null pointer
		}
		
		
		//create the output
		HashMap<String, IData> result = new HashMap<String, IData>();
		
		for (int i=0; i<this.parameterCount; i++){
			ToolParameter currentParam = this.parameterDescriptions[i];
			
			if(currentParam != null // Otherwise, nullpointer exception when trying to process outputs!
					&& currentParam.isOutput){ 
				
				if(currentParam.isComplex){
					String fileName = this.toolParameters[i];
					//GenericFileData outputFileData = new GenericFileData(this.workspace.getFileAsStream(fileName), currentParam.mimeType);
					
					File currentFile = new File (fileName);
					GenericFileData outputFileData;
					try {
						if(currentParam.schema != null && currentParam.schema.length()>0){
							//we have vector data. So use a shp file.
							
							outputFileData = new GenericFileData(currentFile, GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP);
						}else{
							outputFileData = new GenericFileData(currentFile, currentParam.mimeType);
						}
						result.put(currentParam.wpsOutputID, new GenericFileDataBinding(outputFileData));
					} catch (FileNotFoundException e) {
						LOGGER.error("Could not read output file: " + fileName);
						errors.add("Could not read output file: " + fileName);
						throw new RuntimeException("No files found. Probably the process did not create any output files.");
					} catch (IOException e) {
						LOGGER.error("Could not create output file from: " + fileName);
						errors.add("Could not create output file from: " + fileName);
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

		//Handle if no result was created
		if (result.isEmpty()){
			String message = "";
			for (String error : errors){
				message = message.concat(error + " - ");
			}
			message = message.concat("ArcGIS Backend Process " + this.processID + " did not return any output"
					+ " data or the output data could not be processed.");
			throw new RuntimeException(message);
		}
		return result;
	}
	
	private String loadSingleDataItem(IData dataItem){
		
		Object payload = dataItem.getPayload();
		String value = null;
		
		//File
		if (payload instanceof GenericFileData){
			GenericFileData gfd = (GenericFileData)payload;
			value = gfd.writeData(this.workspace.getWorkspace());	
		}
		
		//String
		else if (payload instanceof String)
			value = (String) payload;
		
		//Float
		else if (payload instanceof Float)
			value = ((Float)payload).toString();

		//Integer
		else if (payload instanceof Integer)
			value = ((Integer)payload).toString();
		
		//Double
		else if (payload instanceof Double)
			value = ((Double)payload).toString();
		
		return value;
	}
	
	private String calcToolParameterString(String valueSeparator, String[] valueArray){
		
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
	
	private final String addOutputFile (String fileName){
		String newFileName = this.workspace.getWorkspace().getAbsolutePath() + "\\" + fileName;
		return newFileName;
	}
	
	//delete the current workspace
	protected void finalize(){
		try {
			FileUtils.deleteDirectory(instanceWorkspace);
		} catch (IOException e) {
			LOGGER.error("Could not delete dead workspace:\n" + instanceWorkspace.getAbsolutePath());
			e.printStackTrace();
		}
	}
}
