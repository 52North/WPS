package org.n52.wps.transactional.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.handler.TransactionalExceptionHandler;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TransactionalWebProcessingService extends HttpServlet{
	private static Logger LOGGER = LoggerFactory.getLogger(TransactionalWebProcessingService.class);
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		LOGGER.info("Inbound HTTP-POST DeployProcess Request. " + new Date());
		TransactionalResponse response = null;
		try {
			InputStream is = req.getInputStream();
			if (req.getParameterMap().containsKey("request")){
				is = new ByteArrayInputStream(req.getParameter("request").getBytes("UTF-8"));
			}

//			 WORKAROUND	cut the parameter name "request" of the stream		
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
    	    StringWriter sw=new StringWriter();
    	    int k;
    	    while((k=br.read())!=-1){
    	    	sw.write(k);
    	    }
    	    LOGGER.debug(sw.toString());
    	    String s;
    	    String reqContentType = req.getContentType();
    	    if (sw.toString().startsWith("request=")){
    	    	if(reqContentType.equalsIgnoreCase("text/plain")) {
    	    		s = sw.toString().substring(8);
    	    	}
    	    	else {
    	    		s = URLDecoder.decode(sw.toString().substring(8), "UTF-8");
    	    	}
    	    	LOGGER.debug(s);
    	    } else{
    	    	s = sw.toString();
    	    }

    	   
    	    is = new ByteArrayInputStream(s.getBytes("UTF-8"));
    	    
    	    
			//System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			boolean changeMe = false;
			if(changeMe){
			documentBuilderFactory.setNamespaceAware(true);//this prevents "xmlns="""
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
			}
			DocumentBuilder documentBuilder= documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);
					
			String requestType = document.getFirstChild().getNodeName();
			ITransactionalRequest request = null;
			if (requestType == null) {
				throw new ExceptionReport("Request not valid",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			} else if (requestType.equals("DeployProcess")) {
				request = new DeployProcessRequest(document);
			} else if (requestType.equals("UnDeployProcess")) {
				request = new UndeployProcessRequest(document);
			} else {
				throw new ExceptionReport("Request type unknown ("
						+ requestType
						+ ") Must be DeployProcess or UnDeployProcess",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			}

			LOGGER.info("Request type: " + requestType);
			TransactionalRequestHandler handler = new TransactionalRequestHandler();
			response = handler.handle(request);
			if (response == null) {
				throw new ExceptionReport("bug! An error has occurred while "
						+ "processing the request: " + requestType,
						ExceptionReport.NO_APPLICABLE_CODE);
			} else {
				String rootTag = (request instanceof DeployProcessRequest) ? "DeployProcessResponse"
						: "UnDeployProcessResponse";
				PrintWriter writer = res.getWriter();
				writer.write("<" + rootTag + ">");
				writer.write("<Result success=\"true\">");
				writer.write(response.getMessage());
				writer.write("</Result>");
				writer.write("</" + rootTag + ">");
				writer.flush();
				writer.close();
				LOGGER.info("Request handled successfully: " + requestType);
			}
		} catch (ParserConfigurationException e) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("An error has occurred while "
							+ "building the XML parser",
							ExceptionReport.NO_APPLICABLE_CODE));
		} catch (SAXException e) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("An error has occurred while "
							+ "parsing the XML request",
							ExceptionReport.NO_APPLICABLE_CODE));
		} catch (ExceptionReport exception) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					exception);
		} catch (Throwable t) {
			TransactionalExceptionHandler.handleException(res.getWriter(),
					new ExceptionReport("Unexpected error",
							ExceptionReport.NO_APPLICABLE_CODE));
		}
	}
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
}
