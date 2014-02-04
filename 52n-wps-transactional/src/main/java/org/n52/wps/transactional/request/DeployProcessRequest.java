/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.transactional.request;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xpath.XPathAPI;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DeployProcessRequest implements ITransactionalRequest {
	protected Node processDescription;
	protected DeploymentProfile deploymentProfile;
	protected String schema;

	public DeployProcessRequest(Document doc) throws ExceptionReport {
		try {
			String processID = XPathAPI.selectSingleNode(
					doc,
					"/DeployProcess/ProcessDescriptions/"
							+ "ProcessDescription/Identifier/text()")
					.getNodeValue().trim();
			
			processDescription = XPathAPI.selectSingleNode(doc,
					"/DeployProcess/ProcessDescriptions");
			schema = XPathAPI
					.selectSingleNode(doc,
							"/DeployProcess/DeploymentProfile/Schema/attribute::href")
					.getNodeValue();
			if (schema == null) {
				throw new ExceptionReport(
						"Error. Could not find schema in the deployment profile",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
			LoggerFactory.getLogger(DeployProcessRequest.class).info(
					"process ID: " + processID);
			String deployManagerClass = TransactionalHelper
					.getDeploymentProfileForSchema(schema);
			Constructor<?> constructor;
			constructor = Class.forName(deployManagerClass).getConstructor(
					Node.class, String.class);
                        //NH 17-12-09 we're asking for the deployment profile but also need the process request info
			//deploymentProfile = (DeploymentProfile) constructor.newInstance(
			//		XPathAPI.selectSingleNode(doc,
			//				"/DeployProcessRequest/DeploymentProfile"),
			//		processID);
                        deploymentProfile = (DeploymentProfile) constructor.newInstance(
					XPathAPI.selectSingleNode(doc,
							"/DeployProcess"),
					processID);
		} catch (TransformerException e) {
			throw new ExceptionReport("Error. Malformed DeployProcess request",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (NoSuchMethodException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ClassNotFoundException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (InstantiationException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IllegalAccessException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (InvocationTargetException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}
			
		
		
		
	}


	public Node getProcessDescription() {
		return processDescription;
	}


	public DeploymentProfile getDeploymentProfile() {
		return deploymentProfile;
	}
	
	public String getSchema() {
		return schema;
	}
	
	 

	
	

}
