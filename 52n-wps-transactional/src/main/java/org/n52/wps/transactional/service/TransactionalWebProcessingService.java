package org.n52.wps.transactional.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.n52.wps.transactional.handler.TransactionalExceptionHandler;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.w3c.dom.Document;

public class TransactionalWebProcessingService extends HttpServlet{
	private static Logger LOGGER = Logger.getLogger(TransactionalWebProcessingService.class);
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		LOGGER.info("Inbound HTTP-POST DeployProcess Request. " + new Date());
		TransactionalResponse response = null;
		try {
			InputStream is = req.getInputStream();
			//System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory documentBuiloderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder= documentBuiloderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);
			
			//look up what kind of Request we are dealing with
			String requestType = document.getFirstChild().getNodeName();
			LOGGER.info("requesttype is: "+requestType);
			if(requestType == null){
				throw new Exception("Request Not Valid");
			}
			
			if(requestType.equals("DeployProcessRequest")){
				response = TransactionalRequestHandler.handle(new DeployProcessRequest(document));
			}
			else{
				if(requestType.equals("UnDeployProcessRequest")){
					response = TransactionalRequestHandler.handle(new UndeployProcessRequest(document));
				}else{
					throw new Exception("Could not process request. Reuqest type unknown. Must be DeployProcess or UndeployProcess");
				}
			}
			if(response == null){
				throw new Exception("Could not process request. Could not handle deploy process request properly.");
			}
		}catch(Exception exception){
			TransactionalExceptionHandler.handleException(res.getWriter(), "Internal Server Error. Could not handle deploy process request properly.");
		}
		
		//TODO change this quick hack made on an airplane....
		Writer writer = res.getWriter();
		writer.write("<Result>");
		writer.write(response.getMessage());
		writer.write("</Result>");
		writer.flush();
		writer.close();
		LOGGER.info(" DeployProcess Request handled successfully. " + new Date());
	}


}
