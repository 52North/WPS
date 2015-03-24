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
import org.n52.wps.ags.algorithmpackage.AlgorithmPackage;
import org.n52.wps.ags.algorithmpackage.AlgorithmParameterType;
import org.n52.wps.ags.algorithmpackage.CommandLineParameter;
import org.n52.wps.ags.algorithmpackage.PackageUtils;
import org.n52.wps.ags.workspace.AGSWorkspace;
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.server.IAlgorithm;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class ArcToolboxProcessDelegator implements IAlgorithm{

	private static final Logger LOGGER = LoggerFactory.getLogger(ArcToolboxProcessDelegator.class);
	private AGSWorkspace agsWorkspace;

	private final File toolWorkspace;
	private final File workspaceBase;

	private AlgorithmPackage mco;
	protected List<String> errors;

	private String[] toolParameters;


	protected ArcToolboxProcessDelegator(AlgorithmPackage templateMCO, File workspaceBase) throws IOException{
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
						cmdParam.addValue((PackageUtils.loadSingleDataItem(currentItem, toolWorkspace)));
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
			GenericFileDataWithGT outputFileData;
			try {
				// create the GenericFileData object
				outputFileData = new GenericFileDataWithGT(currentFile, mco.getDefaultMimeType(wpsOutputID));
				// put result on output map
				result.put(wpsOutputID, new GenericFileDataWithGTBinding(outputFileData));
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
		return PackageUtils.getInputDataType(mco, id);
	}

	public Class<?> getOutputDataType(String id) {
		return PackageUtils.getOutputDataType(mco, id);
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
