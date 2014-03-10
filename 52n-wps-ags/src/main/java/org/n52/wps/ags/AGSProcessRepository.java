/**
 * ﻿Copyright (C) 2009 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.ags;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.ags.workspace.AGSWorkspace;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Matthias Mueller, TU Dresden; Christin Henzen, TU Dresden
 *
 */
public class AGSProcessRepository implements IAlgorithmRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(AGSProcessRepository.class);
	
	private Map<String, ProcessDescriptionType> registeredProcessDescriptions;
	private Map<String, ToolParameter[]> registeredAlgorithmParameters;
	
	
	public AGSProcessRepository() {
		LOGGER.info("Initializing ArcGIS Server Repository ...");
		
		//initialize local variables
		this.registeredProcessDescriptions = new HashMap<String, ProcessDescriptionType>();
		this.registeredAlgorithmParameters = new HashMap<String, ToolParameter[]>();
		
		String describeProcessPathString = AGSProperties.getInstance().getProcessDescriptionDir();
		LOGGER.info("Loading AGS process descriptions from: " + describeProcessPathString);
		
		File describeProcessPath = new File(describeProcessPathString);
		ArrayList<File> describeProcessFiles = listAllFiles(describeProcessPath,false);
		
		for (File currentFile : describeProcessFiles){
			addAlgorithm(currentFile);
		}
		
	}
	
	
	/**
	 * Checks if a given processID exists 
	 * @param processID
	 * @return
	*/
	public boolean containsAlgorithm(String processID) {
		return registeredProcessDescriptions.containsKey(processID);
	}
	
	
	/**
	 * Returns an IAlgorithm through GenericAGSProcessDelegator
	 * @param processID
	 * @return
	*/
	public IAlgorithm getAlgorithm(String processID) {
		if (!containsAlgorithm(processID)){
			//TODO returning null should be better than throwing an unexpectable exception. Test if it breaks other parts
			return null;
		}
		// create a unique directory for each instance
		String randomDirName = AGSProperties.getInstance().getWorkspaceBase() + File.separator + UUID.randomUUID();
		return new GenericAGSProcessDelegator(new File (randomDirName), processID, registeredAlgorithmParameters.get(processID), registeredProcessDescriptions.get(processID));
	}
	
	
	private void addAlgorithm(File processDescriptionFile){
		
		ProcessDescriptionType pd = this.loadProcessDescription(processDescriptionFile);
		String processID = pd.getIdentifier().getStringValue();
		LOGGER.debug("Registering: " + processID);
		this.registeredProcessDescriptions.put(processID, pd);
		ToolParameter[] params = this.loadParameters(pd);
		this.registeredAlgorithmParameters.put(processID, params);
		
		if (!pd.validate()){
			LOGGER.debug("ProcessDescription is not valid. Removing "  + processID + " from Repository.");
			this.registeredProcessDescriptions.remove(processID);
			this.registeredAlgorithmParameters.remove(processID);
		}
	}
	
	public Collection<String> getAlgorithmNames() {
		return registeredProcessDescriptions.keySet();
	}
	
	private ToolParameter[] loadParameters(ProcessDescriptionType pd){
//		if(pd.getIdentifier().getStringValue().contains("buffer")){
//			System.out.println("Buffer"); 
//		}
		InputDescriptionType[] inputArray = pd.getDataInputs().getInputArray();
		OutputDescriptionType[] outputArray = pd.getProcessOutputs().getOutputArray();
		
		int paramCount = inputArray.length + outputArray.length;
		
		
		//create parameter array
		//LegacyParameter[] paramArray = new LegacyParameter[inputArray.length + outputArray.length];
		ToolParameter[] paramArray = new ToolParameter[paramCount];
		
		
		for (InputDescriptionType currentDesc : inputArray){
			
			NodeList nl = currentDesc.getDomNode().getChildNodes();
			String pameterID = null;
			
			for (int i = 0; i < nl.getLength(); i++){
				Node currentNode = nl.item(i);
				if(currentNode instanceof Comment){
					pameterID = ((Comment)currentNode).getNodeValue().replaceAll("\\s+", "");
				}
			}
			
			int index = Integer.parseInt(pameterID);
			
			String mimeType = null;
			String schema = null;
			String literalDataType = null;
			String defaultCRS = null;
			
			String prefixString = null;
			String suffixString = null;
			String separatorString = " ";
			String wpsInputID = currentDesc.getIdentifier().getStringValue();
			String wpsOutputID = null;
			
			//complex data
			if (currentDesc.isSetComplexData()){
				mimeType = currentDesc.getComplexData().getDefault().getFormat().getMimeType();
				schema = currentDesc.getComplexData().getDefault().getFormat().getSchema();
			}
			
			//literal data
			if (currentDesc.isSetLiteralData()){
				if (currentDesc.getLiteralData().isSetDataType()){
					literalDataType = currentDesc.getLiteralData().getDataType().getStringValue();
				}
				if (currentDesc.getLiteralData().isSetValuesReference()){
					literalDataType = currentDesc.getLiteralData().getDataType().getReference();
				}
			}
			
			//BBox data
			if (currentDesc.isSetBoundingBoxData()){
				defaultCRS = currentDesc.getBoundingBoxData().toString();
			}
			
			boolean isOptional = false;
			if(currentDesc.getMinOccurs().equals(new BigInteger("0"))){
				isOptional = true;
			}
			
			paramArray[index] = new ToolParameter(wpsInputID, wpsOutputID, pameterID, schema, mimeType, literalDataType, defaultCRS, prefixString, suffixString, separatorString, isOptional);
		}
		
		for (OutputDescriptionType currentDesc : outputArray){
			
			NodeList nl = currentDesc.getDomNode().getChildNodes();
			String pameterID = null;
			
			for (int i = 0; i < nl.getLength(); i++){
				Node currentNode = nl.item(i);
				if(currentNode instanceof Comment){
					pameterID = ((Comment)currentNode).getNodeValue().replaceAll("\\s+", "");
				}
			}
			
			int index = Integer.parseInt(pameterID);
			
			String mimeType = null;
			String schema = null;
			String literalDataType = null;
			String defaultCRS = null;
			
			String prefixString = null;
			String suffixString = null;
			String separatorString = null;
			String wpsInputID = null;
			
			// if parameter is already present
			if(paramArray[index] != null){
				wpsInputID = paramArray[index].wpsInputID;
			}
			
			String wpsOutputID = currentDesc.getIdentifier().getStringValue();
			
			//complex data
			if (currentDesc.isSetComplexOutput()){
				mimeType = currentDesc.getComplexOutput().getDefault().getFormat().getMimeType();
				schema = currentDesc.getComplexOutput().getDefault().getFormat().getSchema();
			}
			
			//literal data
			if (currentDesc.isSetLiteralOutput()){
				if (currentDesc.getLiteralOutput().isSetDataType()){
					literalDataType = currentDesc.getLiteralOutput().getDataType().getStringValue();
				}
			}
			
			//BBox data
			if (currentDesc.isSetBoundingBoxOutput()){
				defaultCRS = currentDesc.getBoundingBoxOutput().toString();
			}
			boolean isOptional = false;
			
			paramArray[index] = new ToolParameter(wpsInputID, wpsOutputID, pameterID, schema, mimeType, literalDataType, defaultCRS, prefixString, suffixString, separatorString,isOptional);
		}
		
		
		return paramArray;
	}
	
	private ArrayList<File> listAllFiles(File rootDir, boolean includeDirNames) {
		ArrayList<File> result = new ArrayList<File>();
		try {
			File[] fileList = rootDir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory() == true) {
					if (includeDirNames)
						result.add(fileList[i]);
					result.addAll(listAllFiles(fileList[i], includeDirNames));
				} else{
					result.add(fileList[i]);
					LOGGER.info("Found AGS PD: " + fileList[i].getPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	

	private ProcessDescriptionType loadProcessDescription(File describeProcessFile){
		
		try {
			InputStream xmlDesc = new FileInputStream(describeProcessFile);
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription is empty! " + describeProcessFile.getName());
				return null;
			}
			
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! " + describeProcessFile.getName(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! " + describeProcessFile.getName(), e);
		}
		return null;
	}
	
	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if(!registeredProcessDescriptions.containsKey(processID)){
			registeredProcessDescriptions.put(processID, getAlgorithm(processID).getDescription());
		}
		return registeredProcessDescriptions.get(processID);
	}


	@Override
	public void shutdown() {
		AGSWorkspace.shutdown();
	}
	
	
}
