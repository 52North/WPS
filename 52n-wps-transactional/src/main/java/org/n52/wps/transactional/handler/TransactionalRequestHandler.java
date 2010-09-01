/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.transactional.handler;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.n52.wps.transactional.service.TransactionalHelper;

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
	public static TransactionalResponse handle(ITransactionalRequest request)
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

	private static TransactionalResponse handleDeploy(
			DeployProcessRequest request) throws ExceptionReport {
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
}
