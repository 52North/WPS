/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.transactional.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class TransactionalRequestHandler {
	/**
	 * Handles the request and returns a transactional response (if succeeded)
	 * or throws an exception (otherwise)
	 * 
	 * @param request
	 *            the request to handle
	 * @return a response if the process has succeeded. <code>null</code> is
	 *         never returned
	 * @throws Exception
	 *             if an error occurs handling the request
	 */
	public TransactionalResponse handle(ITransactionalRequest request)
			throws ExceptionReport {
		if (request instanceof DeployProcessRequest) {
			return handleDeploy((DeployProcessRequest) request);
		} else if (request instanceof UndeployProcessRequest) {
			return handleUnDeploy((UndeployProcessRequest) request);
		} else {
			throw new ExceptionReport("Error. Could not handle request",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}

	private TransactionalResponse handleDeploy(
			DeployProcessRequest request) throws ExceptionReport {
		
		//storeDescribeProcess
		storeDescribeProcess(request);
		
		try {
			ITransactionalAlgorithmRepository repository = TransactionalHelper
					.getMatchingTransactionalRepository(request.getSchema());

			if (repository == null) {
				throw new ExceptionReport("Could not find matching repository",
						ExceptionReport.NO_APPLICABLE_CODE);
			}

			if (!repository.addAlgorithm(request)) {
				throw new ExceptionReport("Could not deploy process",
						ExceptionReport.NO_APPLICABLE_CODE);
			} else {
				return new TransactionalResponse(
						"Process successfully deployed");
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not deploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
	}

	private void storeDescribeProcess(DeployProcessRequest request) {
		 String processName ="";
          try{
             processName = XPathAPI.selectSingleNode(request.getProcessDescription(), "/DeployProcess/ProcessDescriptions/ProcessDescription/Identifier/text()").getNodeValue().trim();
         }catch(DOMException de){
             de.printStackTrace();
         }catch(Exception e){
             e.printStackTrace();
         }
		
		
		 Node describeProcess = null;
		try {
			describeProcess = XPathAPI.selectSingleNode(request.getProcessDescription(), "/DeployProcess/ProcessDescriptions");
		} catch (TransformerException e) {
			e.printStackTrace();
		}
			
			String fullPath =  GenericTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
			int searchIndex= fullPath.indexOf("WEB-INF");
			String subPath = fullPath.substring(0, searchIndex);
			subPath = subPath.replaceFirst("file:", "");
			if(subPath.startsWith("/")){
				subPath = subPath.substring(1);
			}
			
			File directory = new File(subPath+"WEB-INF/ProcessDescriptions/");
			if(!directory.exists()){
				directory.mkdirs();
			}

			String path = subPath+"WEB-INF/ProcessDescriptions/"+processName+".xml";
			
		
			try {
				writeXmlFile(describeProcess, path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
	}

	private static TransactionalResponse handleUnDeploy(
			UndeployProcessRequest request) throws ExceptionReport {
		try {
			if (RepositoryManager.getInstance().getAlgorithm(
					request.getProcessID()) == null) {
				throw new ExceptionReport("The process does not exist",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
			IAlgorithmRepository repository = RepositoryManager.getInstance()
					.getRepositoryForAlgorithm(request.getProcessID());
			if (repository instanceof ITransactionalAlgorithmRepository) {
				ITransactionalAlgorithmRepository transactionalRepository = (ITransactionalAlgorithmRepository) repository;
				if (!transactionalRepository.removeAlgorithm(request)) {
					throw new ExceptionReport("Could not undeploy process",
							ExceptionReport.NO_APPLICABLE_CODE);
				} else {
					return new TransactionalResponse(
							"Process successfully undeployed");
				}
			} else {
				throw new ExceptionReport(
						"The process is not in a transactional "
								+ "repository and cannot be undeployed",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not undeploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}

	}
	
	protected void writeXmlFile(Node node, String filename) throws IOException, TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document tempDocument = documentBuilder.newDocument();
		Node importedNode = tempDocument.importNode(node, true);
		tempDocument.appendChild(importedNode);
        // Prepare the DOM document for writing
        Source source = new DOMSource(tempDocument);

        // Prepare the output file
        File file = new File(filename);
        String parent = file.getParent();
        File directory = new File(parent);
        directory.mkdirs();
        //file.createNewFile();
        OutputStream fileOutput = new FileOutputStream(file);
        Result result = new StreamResult(fileOutput);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    
	
	
	}
}
