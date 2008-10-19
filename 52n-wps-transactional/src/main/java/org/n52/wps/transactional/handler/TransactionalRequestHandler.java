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

import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.n52.wps.transactional.service.TransactionalHelper;

public class TransactionalRequestHandler {

	

	public static  TransactionalResponse handle(ITransactionalRequest request) {
		
		try {
			if(!(request instanceof DeployProcessRequest)){
				throw new Exception("Error. Could not handle request");
			}
			ITransactionalAlgorithmRepository repository = TransactionalHelper.getMatchingTransactionalRepository(((DeployProcessRequest)request).getSchema()); 
			
			if(repository == null){
				throw new Exception("Error. Could not find matching repository");
			}
			//request.execute();
			 if (request instanceof DeployProcessRequest) {
				 boolean success = repository.addAlgorithm(request);
				 if(! success){
					 return new TransactionalResponse("Error. Could not deploy process"); 
				 }
				 return new TransactionalResponse("Process successfully deployed");
			 }
			 if (request instanceof UndeployProcessRequest) {
				 boolean success = repository.removeAlgorithm(request);
				 if(! success){
					 return new TransactionalResponse("Error. Could not undeploy process"); 
				 }
				 return new TransactionalResponse("Process successfully undeployed");	
			 }
			
		} catch (Exception e) {
			e.printStackTrace();
			return new TransactionalResponse("Error = " +e.getMessage());
		}
		return new TransactionalResponse("Error");
		
		
	}

}
