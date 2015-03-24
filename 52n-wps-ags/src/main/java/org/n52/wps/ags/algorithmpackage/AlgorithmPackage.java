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
package org.n52.wps.ags.algorithmpackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class AlgorithmPackage {
	private final File algorithmWorkspace;

	private ProcessDescriptionType processDescription;
	private AlgorithmDescription algorithmDescription;

	static Logger LOGGER = LoggerFactory.getLogger(AlgorithmPackage.class);

	public AlgorithmPackage (ProcessDescriptionType pd, AlgorithmDescription ad, File algorithmWorkspace){

		// create descriptions
		algorithmDescription = ad;
		processDescription = pd;

		// assign workspace
		this.algorithmWorkspace = algorithmWorkspace;

	}

	public AlgorithmPackage (File processDescriptionXML, File workspaceTemplateRoot){

		// create descriptions
		processDescription = createProcessDescription(processDescriptionXML);
		algorithmDescription = createAlgorithmDescription(processDescriptionXML);

		// assemble template workspace
		String wsDirString = workspaceTemplateRoot.getAbsolutePath() + File.separator + getWorkspacePathFragment();
		try {
			wsDirString = new File(wsDirString).getCanonicalPath();
		} catch (IOException e) {
			LOGGER.warn("Could not determine the workspace's canonical path. Possibly a malformed AlgorithmDescription.\n"
					+ "rootDirectory is: " + workspaceTemplateRoot.getAbsolutePath() + "\n"
					+ "workspacePathFragment is: " + getWorkspacePathFragment());
		}
		algorithmWorkspace = new File(wsDirString);
	}

	public AlgorithmPackage createChild (File destinationRoot) throws IOException{
		FileUtils.copyDirectoryToDirectory(algorithmWorkspace, destinationRoot);
		File wsDir = new File(destinationRoot.getAbsolutePath() + File.separator + getWorkspacePathFragment());
		AlgorithmPackage child = new AlgorithmPackage(processDescription, algorithmDescription, wsDir);
		return child;
	}

	public String getProcessID(){
		return processDescription.getIdentifier().getStringValue();
	}

	public boolean isContainer(URI containerURN){
		String str = containerURN.toString();
		if (str.equalsIgnoreCase(algorithmDescription.getContainerType())){
			return true;
		} else {
			return false;
		}
	}

	public boolean isContainer(URI[] containerURNs){
		for(URI currentContainer : containerURNs){
			if (isContainer(currentContainer)){
				return true;
			}
		}
		return false;
	}

	public boolean isSufficientRuntimeEnvironment (URI[] runtimeURNs){
		boolean doublecheck = true;
		for (String myCurrentRuntime : algorithmDescription.getRequiredRuntimeComponent()){
			boolean check = false;
			for (URI yourCurrentRuntime : runtimeURNs){
				if (yourCurrentRuntime.toString().equalsIgnoreCase(myCurrentRuntime)){
					check = true;
				}
			}
			doublecheck = check && doublecheck;
		}

		return doublecheck;
	}

	public ProcessDescriptionType getProcessDescription(){
		return processDescription;
	}

	public File getInstanceWorkspace(){
		return algorithmWorkspace;
	}

	public AlgorithmURL getAlgorithmURL(){
		return new AlgorithmURL(algorithmDescription.getAlgorithmLocation());
	}

	public  List<AlgorithmParameterType> getParameters(){
		return algorithmDescription.getAlgorithmParameters().getParameter();
	}

	public String getDefaultMimeType(String paramID){
		String mimeType = null;

		// check inputs for a match
		for (InputDescriptionType currentInput : processDescription.getDataInputs().getInputArray()){
			if (currentInput.getIdentifier().getStringValue().equalsIgnoreCase(paramID)){
				mimeType = currentInput.getComplexData().getDefault().getFormat().getMimeType();
			}
		}

		for (OutputDescriptionType currentOutput : processDescription.getProcessOutputs().getOutputArray()){
			if (currentOutput.getIdentifier().getStringValue().equalsIgnoreCase(paramID)){
				mimeType = currentOutput.getComplexOutput().getDefault().getFormat().getMimeType();
			}
		}

		return mimeType;
	}

	private String getWorkspacePathFragment(){
		return algorithmDescription.getWorkspaceLocation();
	}

	private static AlgorithmDescription createAlgorithmDescription (File xmlFile) {

		// create the AlgorithmDescription document from DescribeProcess file
		StringReader adReader = generateAlgorithmDescription(xmlFile);

		try {
			JAXBContext jc = JAXBContext.newInstance(AlgorithmDescription.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			AlgorithmDescription ad = (AlgorithmDescription) unmarshaller.unmarshal(adReader);
			return ad;
		} catch (JAXBException e) {
			LOGGER.error("Unable to create AlgorithmDescription from xmlFile: " + xmlFile.getAbsolutePath());
			e.printStackTrace();
			return null;
		}

	}

	private static ProcessDescriptionType createProcessDescription (File describeProcessFile){

		try {
			InputStream xmlDesc = new FileInputStream(describeProcessFile);
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription is empty!");
				return null;
			}
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! ", e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error! ", e);
		}
		return null;
	}


	/**
     * Generates an XML file. Uses a ProcessDescription file and a XSLT file to generate an
     * AlgorithmDescription.
     *
     * @param xmlFile - the ProcessDescription in XML
     * @param xsltFile - the transformation rules in XSLT
     * @throws Exception - the exceptions
     */
    private static StringReader generateAlgorithmDescription(File xmlFile) {

    	Source xmlSource = new StreamSource(xmlFile);
    	TransformerFactory transFact = TransformerFactory.newInstance();
    	StringWriter sw = new StringWriter();
        StreamResult transformResult = new StreamResult(sw);

    	try {
    		Source xsltSource = transFact.getAssociatedStylesheet(xmlSource, null, null, null);
			Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, transformResult);
			StringReader sr = new StringReader(sw.toString());
			return sr;

		} catch (TransformerException e) {
			LOGGER.error("Error evaluating ProcessDescription XML.");
			e.printStackTrace();
		}
		return null;
    }

}
