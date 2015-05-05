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
package org.n52.wps.server.request;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x20.DataInputType;
import net.opengis.wps.x20.ExecuteDocument;
import net.opengis.wps.x20.ExecuteRequestType;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x20.StatusInfoDocument.StatusInfo;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.context.ExecutionContext;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.ExecuteResponseBuilderV200;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Handles an ExecuteRequest
 */
public class ExecuteRequestV200 extends ExecuteRequest implements IObserver {

	private static Logger LOGGER = LoggerFactory
			.getLogger(ExecuteRequestV200.class);
	private ExecuteDocument execDom;
	private Map<String, IData> returnResults;
	private ExecuteResponseBuilderV200 execRespType;
	private boolean rawData;

	/**
	 * Creates an ExecuteRequest based on a Document (HTTP_POST)
	 * 
	 * @param doc
	 *            The clients submission
	 * @throws ExceptionReport
	 */
	public ExecuteRequestV200(Document doc) throws ExceptionReport {
		super(doc);
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.execDom = ExecuteDocument.Factory.parse(doc, option);
			if (this.execDom == null) {
				LOGGER.error("ExecuteDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}

		// validate the client input
		validate();

		// create an initial response
		execRespType = new ExecuteResponseBuilderV200(this);

		storeRequest(execDom);
	}

	/**
	 * Validates the client request
	 * 
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport {
		// Identifier must be specified.
		if (!WPSConfig.SUPPORTED_VERSIONS.contains(execDom.getExecute()
				.getVersion())) {
			throw new ExceptionReport("Specified version is not supported.",
					ExceptionReport.INVALID_PARAMETER_VALUE, "version="
							+ getExecute().getVersion());
		}

		// Fix for bug https://bugzilla.52north.org/show_bug.cgi?id=906
		String identifier = getAlgorithmIdentifier();

		if (identifier == null) {
			throw new ExceptionReport("No process identifier supplied.",
					ExceptionReport.MISSING_PARAMETER_VALUE, "identifier");
		}

		// check if the algorithm is in our repository
		if (!RepositoryManager.getInstance().containsAlgorithm(identifier)) {
			throw new ExceptionReport(
					"Specified process identifier does not exist",
					ExceptionReport.INVALID_PARAMETER_VALUE, "identifier="
							+ identifier);
		}

		// validate if the process can be executed
		ProcessOffering desc = (ProcessOffering) RepositoryManager
				.getInstance().getProcessDescription(getAlgorithmIdentifier())
				.getProcessDescriptionType(WPSConfig.VERSION_200);
		// We need a description of the inputs for the algorithm
		if (desc == null) {
			LOGGER.warn("desc == null");
			return false;
		}

	    //TODO validate in-/outputs
		
		//TODO check for null
		rawData = execDom.getExecute().getResponse().equals(ExecuteRequestType.Response.RAW);
		
		return true;
	}
	
	/**
	 * Gets the Execute that is associated with this Request
	 * 
	 * @return The Execute
	 */
	public ExecuteRequestType getExecute() {
		return execDom.getExecute();
	}

	/**
	 * Actually serves the Request.
	 * 
	 * @throws ExceptionReport
	 */
	public Response call() throws ExceptionReport {
		IAlgorithm algorithm = null;
		Map<String, List<IData>> inputMap = null;
		try {
			//TODO add outputs to execution context
			ExecutionContext context = new ExecutionContext();
			
			// register so that any function that calls
			// ExecuteContextFactory.getContext() gets the instance registered
			// with this thread
			ExecutionContextFactory.registerContext(context);

			LOGGER.debug("started with execution");

			updateStatusStarted();

			// parse the input
			DataInputType[] inputs = new DataInputType[0];
			if (getExecute().getInputArray() != null) {
				inputs = getExecute().getInputArray();
			}
			InputHandler parser = new InputHandler.Builder(new Input(inputs),
					getAlgorithmIdentifier()).build();

			// we got so far:
			// get the algorithm, and run it with the clients input
			
			algorithm = RepositoryManager.getInstance().getAlgorithm(
					getAlgorithmIdentifier());

			if (algorithm instanceof ISubject) {
				ISubject subject = (ISubject) algorithm;
				subject.addObserver(this);
			}

			inputMap = parser.getParsedInputData();
			returnResults = algorithm.run(inputMap);			

			List<String> errorList = algorithm.getErrors();
			if (errorList != null && !errorList.isEmpty()) {
				String errorMessage = errorList.get(0);
				LOGGER.error("Error reported while handling ExecuteRequest for "
						+ getAlgorithmIdentifier() + ": " + errorMessage);
//				updateStatusError(errorMessage);
			} else {
				updateStatusSuccess();
			}
		} catch (Throwable e) {
			String errorMessage = null;
			if (algorithm != null && algorithm.getErrors() != null
					&& !algorithm.getErrors().isEmpty()) {
				errorMessage = algorithm.getErrors().get(0);
			}
			if (errorMessage == null) {
				errorMessage = e.toString();
			}
			if (errorMessage == null) {
				errorMessage = "UNKNOWN ERROR";
			}
			LOGGER.error("Exception/Error while executing ExecuteRequest for "
					+ getAlgorithmIdentifier() + ": " + errorMessage);
//			updateStatusError(errorMessage);
			if (e instanceof Error) {
				// This is required when catching Error
				throw (Error) e;
			}
			if (e instanceof ExceptionReport) {
				throw (ExceptionReport) e;
			} else {
				throw new ExceptionReport(
						"Error while executing the embedded process for: "
								+ getAlgorithmIdentifier(),
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		} finally {
			// you ***MUST*** call this or else you will have a PermGen
			// ClassLoader memory leak due to ThreadLocal use
			ExecutionContextFactory.unregisterContext();
			if (algorithm instanceof ISubject) {
				((ISubject) algorithm).removeObserver(this);
			}
			if (inputMap != null) {
				for (List<IData> l : inputMap.values()) {
					for (IData d : l) {
						if (d instanceof IComplexData) {
							((IComplexData) d).dispose();
						}
					}
				}
			}
			if (returnResults != null) {
				for (IData d : returnResults.values()) {
					if (d instanceof IComplexData) {
						((IComplexData) d).dispose();
					}
				}
			}
		}

		ExecuteResponse response = new ExecuteResponse(this);
		return response;
	}

	/**
	 * Gets the identifier of the algorithm the client requested
	 * 
	 * @return An identifier
	 */
	public String getAlgorithmIdentifier() {
		// Fix for bug https://bugzilla.52north.org/show_bug.cgi?id=906
		if (getExecute().getIdentifier() != null) {
			return getExecute().getIdentifier().getStringValue();
		}
		return null;
	}

	public Map<String, IData> getAttachedResult() {
		return returnResults;
	}

	public ExecuteResponseBuilderV200 getExecuteResponseBuilder() {
		return this.execRespType;
	}

	public boolean isRawData() {
		return rawData;
	}

	public void update(ISubject subject) {
		Object state = subject.getState();
		LOGGER.info("Update received from Subject, state changed to : " + state);
		
		StatusInfo status = StatusInfo.Factory.newInstance();

		int percentage = 0;
		if (state instanceof Integer) {
			percentage = (Integer) state;
			status.setPercentCompleted(percentage);
		}
		status.setStatus(ExecuteResponseBuilderV200.Status.Running.toString());
		updateStatus(status);
	}

	public void updateStatusAccepted() {		
		StatusInfo status = StatusInfo.Factory.newInstance();
		status.setStatus(ExecuteResponseBuilderV200.Status.Accepted.toString());
		updateStatus(status);
	}

	public void updateStatusSuccess() {		
		StatusInfo status = StatusInfo.Factory.newInstance();
		status.setStatus(ExecuteResponseBuilderV200.Status.Succeeded.toString());
		updateStatus(status);
	}

	public void updateStatusFailed() {		
		StatusInfo status = StatusInfo.Factory.newInstance();
		status.setStatus(ExecuteResponseBuilderV200.Status.Failed.toString());
		updateStatus(status);
	}

	public void updateStatusStarted() {		
		StatusInfo status = StatusInfo.Factory.newInstance();
		status.setStatus(ExecuteResponseBuilderV200.Status.Running.toString());
		status.setPercentCompleted(0);
		updateStatus(status);
	}

	private void updateStatus(StatusInfo status) {
		status.setJobID(getUniqueId().toString());
		getExecuteResponseBuilder().setStatus(status);
		try {
			getExecuteResponseBuilder().update();
//			if (isStoreResponse()) {
				ExecuteResponse executeResponse = new ExecuteResponse(this);
				InputStream is = null;
				try {
					is = executeResponse.getAsStream();
					DatabaseFactory.getDatabase().storeResponse(
							getUniqueId().toString(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
//			}
		} catch (ExceptionReport e) {
			LOGGER.error("Update of process status failed.", e);
			throw new RuntimeException(e);
		}
	}

	private void storeRequest(ExecuteDocument executeDocument) {
		InputStream is = null;
		try {
			is = executeDocument.newInputStream();
			DatabaseFactory.getDatabase().insertRequest(
					getUniqueId().toString(), is, true);
		} catch (Exception e) {
			LOGGER.error("Exception storing ExecuteRequest", e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	@Override
	public boolean isStoreResponse() {
		return getExecute().getMode().equals(ExecuteRequestType.Mode.ASYNC);
	}

	@Override
	public void updateStatusError(String errorMessage) {
		// TODO Auto-generated method stub		
	}
}