package org.n52.wps.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.message.SOAPBody;
import org.n52.wps.server.handler.RequestHandler;
import org.w3c.dom.DOMException;
// import org.w3c.dom.Document;
// import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Pay attention: This was a very quick hack. Still a lot of work todo.
// General TODO: ExceptionHandling, enable to check for two processes with DescribeProcess, REFACTOR redundant code, JAVADOC comments, enable logging etc.
// @author: Johannes Brauner, brauner@52north.org

public class WebProcessingServiceSoap {

	public void DescribeProcess(SOAPEnvelope requestEnvelope, SOAPEnvelope responseEnvelope) throws ExceptionReport {

		// Create Output Stream.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Node requestDocument = null;
		try {
			requestDocument = ((SOAPBody)requestEnvelope.getBody()).getFirstChild();
		} catch (SOAPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();

		if (requestDocument.getLocalName().equalsIgnoreCase("describeProcess")) {
			parameterMap.put("request", new String[] { "describeProcess" });
		}
		// Get all identifier.
		NodeList identifierList = requestDocument.getChildNodes();
		String[] identifier = new String[identifierList.getLength()];
		for (int i = 0; i < identifierList.getLength(); i++) {
			if (identifierList.item(i).getLocalName().equalsIgnoreCase("Identifier")) {
				identifier[i] = identifierList.item(i).getFirstChild().getNodeValue();
			}
		}
		parameterMap.put("identifier", identifier);

		// Make other request parameters static. TODO Make them dynamic.
		parameterMap.put("service", new String[]{"WPS"});
		parameterMap.put("version", new String[]{"1.0.0"});

		RequestHandler requestHandler = new RequestHandler(parameterMap, outputStream);

		// Parse output stream to SOAP message.
		try {
			
			String filename = "temp"+parameterMap.hashCode()+".xml";
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(outputStream.toByteArray());
			DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder  = factory.newDocumentBuilder();
			File tempFile = new File(filename);
			org.w3c.dom.Document document = builder.parse(tempFile);
			responseEnvelope.getBody().addDocument(document);
			tempFile.delete();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void GetCapabilities(SOAPEnvelope requestEnvelope, SOAPEnvelope responseEnvelope) throws ExceptionReport {

		// Create Output Stream.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Node requestDocument = null;
		try {
			requestDocument = ((SOAPBody)requestEnvelope.getBody()).getFirstChild();
		} catch (SOAPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();

		// Check request parameter "request".
		if (requestDocument.getLocalName().equalsIgnoreCase("getcapabilities")) {
			parameterMap.put("request", new String[] { "getCapabilities" });
		}

		// Make other request parameters static. TODO Make them dynamic.
		parameterMap.put("service", new String[]{"WPS"});
		parameterMap.put("version", new String[]{"1.0.0"});

		RequestHandler requestHandler = new RequestHandler(parameterMap, outputStream);

		// Parse output stream to SOAP message.
		try {
			outputStream.flush();
			// String teststring = outputStream.toString("UTF-8");
			String filename = "temp"+parameterMap.hashCode()+".xml";
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(outputStream.toByteArray());
			DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder  = factory.newDocumentBuilder();
			File tempFile = new File(filename);
			org.w3c.dom.Document document = builder.parse(tempFile);
			responseEnvelope.getBody().addDocument(document);
			tempFile.delete();
			
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Execute(SOAPEnvelope request, SOAPEnvelope response)  {

		callRequestHandler(request, response);
	}

	public void ExecuteProcess_buffer(SOAPEnvelope request, SOAPEnvelope response)  {

		callRequestHandler(request, response);
	}

	private static void callRequestHandler(SOAPEnvelope requestEnvelope, SOAPEnvelope responseEnvelope) {

		// Create Output Stream.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Create SOAP body document from request.
		Node requestDocument = null;
		try {
			requestDocument = ((SOAPBody)requestEnvelope.getBody()).getFirstChild();
		} catch (SOAPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Get input stream from document.
		String documentString = xmlToString(requestDocument);

		// QUICK AND DIRTY: Change request document's parent node to "Execute" due to problems inside the WPS spec.
		documentString = documentString.replaceAll("wps:ExecuteProcess_buffer", "wps:Execute");

		ByteArrayInputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(documentString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Parse request message to request handler (analogous to HTTP Post)
		
		RequestHandler requestHandler = null;
		try {
			requestHandler = new RequestHandler(inputStream, outputStream);
		} catch (ExceptionReport e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Parse output stream to SOAP message.
		try {
			outputStream.flush();
			// String teststring = outputStream.toString("UTF-8");
			String filename = "temp"+requestHandler.hashCode()+".xml";
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(outputStream.toByteArray());
			DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder  = factory.newDocumentBuilder();
			File tempFile = new File(filename);
			org.w3c.dom.Document document = builder.parse(tempFile);
			responseEnvelope.getBody().addDocument(document);
			tempFile.delete();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}
}