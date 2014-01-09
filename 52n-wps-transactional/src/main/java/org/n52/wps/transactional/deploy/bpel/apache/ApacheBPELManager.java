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
package org.n52.wps.transactional.deploy.bpel.apache;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.deploy.AbstractProcessManager;
import org.n52.wps.transactional.deploymentprofiles.BPELDeploymentProfile;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ApacheBPELManager extends AbstractProcessManager {

    private ODEServiceClient _client;
    private OMFactory _factory;
    private String deploymentEndpoint;
    private String processManagerEndpoint;
    
    public ApacheBPELManager(ITransactionalAlgorithmRepository repository) {
        super(repository);

    	Property[] properties = WPSConfig.getInstance().getPropertiesForRepositoryClass(repository.getClass().getName());
        //TODO think of multiple instance of this class registered (yet not possible since singleton)
        Property deployEndpointProperty = WPSConfig.getInstance().getPropertyForKey(properties, "ODE-Engine_DeploymentEndpoint");
        if(deployEndpointProperty==null){
                throw new RuntimeException("Error. Could not find ODE-Engine_DeploymentEndpoint");
        }
        deploymentEndpoint = deployEndpointProperty.getStringValue();
        Property processManagerEndpointProperty = WPSConfig.getInstance().getPropertyForKey(properties, "ODE-Engine_ProcessManagerEndpoint");
        if(processManagerEndpointProperty==null){
                throw new RuntimeException("Error. Could not find ODE-Engine_ProcessManagerEndpoint");
        }
        processManagerEndpoint = processManagerEndpointProperty.getStringValue();

         try{
            _factory = OMAbstractFactory.getOMFactory();
            _client = new ODEServiceClient();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean deployProcess(DeployProcessRequest request) throws Exception {

        boolean blnresult = false;
        // WPS-T preparation
        DeploymentProfile profile = request.getDeploymentProfile();
        if(! (profile instanceof BPELDeploymentProfile)){
                throw new Exception("Requested Deployment Profile not supported");
        }
        BPELDeploymentProfile deploymentProfile = (BPELDeploymentProfile) profile; 
        String processID = deploymentProfile.getProcessID();
        Node suitcase = deploymentProfile.getSuitCase();
        System.out.println(nodeToString(suitcase));
        Node workflow = deploymentProfile.getBPEL();
        System.out.println(nodeToString(workflow));
        Node clientWSDL = deploymentProfile.getClientWSDL();
        System.out.println(nodeToString(clientWSDL));
        Map<Integer, Node> wsdlList = deploymentProfile.getWSDLList();

        //ODE preparation
        OMElement result = null;
        try{
            OMNamespace pmapi = _factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
            OMElement root = _factory.createOMElement("deploy", null); // qualified operation name
            OMElement namePart = _factory.createOMElement("name", null);
            namePart.setNamespace(pmapi);
            namePart.setText(processID);
            //OMElement zipPart = _factory.createOMElement("package", null);
            OMElement zipPart = _factory.createOMElement("package", pmapi);
            //OMElement zipElmt = _factory.createOMElement("zip", null);
            OMElement zipElmt = _factory.createOMElement("zip", pmapi);

            File zipFile = File.createTempFile("wpsbpel", ".zip", null);

            int res = ZipCreator.makeZIP(processID, suitcase, workflow, clientWSDL, wsdlList, zipFile);
             try{
                InputStream is = new BufferedInputStream(new FileInputStream(zipFile));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try{
                    for (int b = is.read(); b >= 0; b = is.read()) {
                        outputStream.write((byte) b);
                    }
                    String base64Enc = Base64.encode(outputStream.toByteArray());
                    OMText zipContent = _factory.createOMText(base64Enc, "application/zip", true);
                    root.addChild(namePart);
                    root.addChild(zipPart);
                    zipPart.addChild(zipElmt);
                    zipElmt.addChild(zipContent);

                    
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }

            // Deploy
            try{
                result = sendToDeployment(root);
            }catch(Exception e){
                e.printStackTrace();
                blnresult = false;
            }
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
        //if (result != null){
            if (result.toString().contains("response"))
                blnresult = true;
        //}
        return blnresult;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean unDeployProcess(String processID) throws Exception {
        // Prepare undeploy message
        OMNamespace pmapi = _factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
        OMElement root = _factory.createOMElement("undeploy", pmapi);  // qualified operation name
        OMElement part = _factory.createOMElement("processName", pmapi);
        part.setText(processID);
        root.addChild(part);

        // Undeploy
        sendToDeployment(root);

        return true;
	}
    
    public Document invoke(ExecuteDocument doc, String algorithmID) throws Exception {
        
        Node domNode = doc.getDomNode();


        //doc.save(new File("d:\\tmp\\execdoc.xml"));
        //String serializedXML = writeXMLToStream(new DOMSource(domNode)).toString();
        
//      serializedXML = serializedXML.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
//	serializedXML = serializedXML.replace("<Execute xmlns=\"http://www.opengis.net/wps/1.0.0\">","<Execute xmlns=\"http://www.opengis.net/wps/1.0.0\" version=\"1.0.0\" service=\"WPS\">");
//	serializedXML = serializedXML.replace(" href"," xmlns:xlin=\"http://www.w3.org/1999/xlink\" xlin:href");
        ServiceClient client = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        SOAPEnvelope response = null;
        try{
            client = new ServiceClient();
            OperationClient operationClient = client.createClient(ServiceClient.ANON_OUT_IN_OP);
            //creating message context
            MessageContext outMsgCtx = new MessageContext();
            //assigning message context's option object into instance variable
            Options opts = outMsgCtx.getOptions();
            //setting properties into option

            //TODO is this correct?

            opts.setTo(new EndpointReference(deploymentEndpoint.replace("DeploymentService", algorithmID)));
            opts.setAction("");
            outMsgCtx.setEnvelope(createSOAPEnvelope(doc));
            operationClient.addMessageContext(outMsgCtx);
            operationClient.execute(true);
            MessageContext inMsgtCtx = operationClient.getMessageContext("In");
            response = inMsgtCtx.getEnvelope();

            
        }catch(AxisFault af){

        }finally{
//            if (client != null){
//                try{
//                    client.cleanupTransport();
//
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//                try{
//                    client.cleanup();
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
        }


        //TODO: Parse SoapEnvelope to DOM Document

       Document result = SAAJUtil.getDocumentFromSOAPEnvelope(response);
           
		if (client != null) {
			try {
				client.cleanupTransport();

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				client.cleanup();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
       
       
        //Document result = builder.parse((InputStream)response.getXMLStreamReader());

        //System.out.print(result.toString());
        return result;
        //throw new UnsupportedOperationException("Not supported yet.");

        
    }

    public Collection<String> getAllProcesses() throws Exception {
       try{
    	   List<String> allProcesses = new ArrayList<String>();

           //ServiceClient sc = new ServiceClient(null, null);


    	   OMElement listRoot = _client.buildMessage("listAllProcesses", new String[] {}, new String[] {});
                
    	   OMElement result = sendToPM(listRoot);
    	   Iterator<OMElement> pi = result.getFirstElement().getChildrenWithName(
                       new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/","process-info"));
        
    	   while (pi.hasNext()) {
    		   OMElement omPID = pi.next();
    		   
    		   String fullName = omPID.getFirstChildWithName(
                       new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/","pid")).getText();
    		   
    		   /*just take the name as defined by the user...
    		    * whats returned originally was something like
    		    * {http://xy.z}ProcessName-XXX (-XXX is attached due to the ODE-versioning)
    		    * this lead to problems with the processdescription, which
    		    * has the name "ProcessName"
    		    */
    		   allProcesses.add(fullName.substring(fullName.indexOf("}") + 1, fullName.indexOf("-")));
    	   }
    	   return allProcesses;
       }catch(Exception e){
    	   return new ArrayList<String>();
       }
      
        
    }

    public boolean containsProcess(String processID) throws Exception {
        boolean containsProcess = false;
        //need to filter out the namespace if it is passed in.
        if (processID.contains("}"))
            processID = processID.split("}")[1];
        try{
            OMElement listRoot = _client.buildMessage("listProcesses", new String[] {"filter", "orderKeys"},
                new String[] {"name="+processID+"", ""});
            OMElement result = sendToPM(listRoot);

            if (result.toString().contains(processID))
                containsProcess = true;
        }catch(AxisFault af){
            containsProcess = false;
            af.printStackTrace();
        }catch(Exception e){
            containsProcess = false;
            e.printStackTrace();
        }
        //return getAllProcesses().contains(processID);
        return containsProcess;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean unDeployProcess(UndeployProcessRequest request) throws Exception {
        //unDeployProcess(String processID) is implemented though...
        return unDeployProcess((String)request.getProcessID());
        //return false;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    
     private OMElement sendToPM(OMElement msg) throws AxisFault {
    	return _client.send(msg,this.processManagerEndpoint);
        //return _PMclient.send(msg, this.processManagerEndpoint,10000);
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg,this.deploymentEndpoint);

        //return _DEPclient.send(msg,this.deploymentEndpoint,10000);
    }

    private ByteArrayOutputStream writeXMLToStream(Source source) throws TransformerException {
//		 Prepare the DOM document for writing
       
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Prepare the output file
       
        Result result = new StreamResult(out);
        //System.etProperty("javax.xml.transform.TransformerFactory","com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        // Write the DOM document to the file
        TransformerFactory x = TransformerFactory.newInstance();
        Transformer xformer = x.newTransformer();
        xformer.transform(source, result);
        
        return out;
	}
    
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		return stringWriter.toString();
	}
    
    
    public  SOAPEnvelope createSOAPEnvelope(Node domNode) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

         NamespaceContext ctx = new NamespaceContext() {

            public String getNamespaceURI(String prefix) {
                String uri;
                          if (prefix.equals("wps"))
                              uri = "http://www.opengis.net/wps/1.0.0";
                          else if (prefix.equals("ows"))
                              uri = "http://www.opengis.net/ows/1.1";
                          else
                              uri = null;
                          return uri;
            }

            public String getPrefix(String namespaceURI) {
                return null;
            }

            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        };

        XPathFactory xpathFact =
                      XPathFactory.newInstance();
        XPath xpath = xpathFact.newXPath();
        xpath.setNamespaceContext(ctx);

        String identifier = null;
        String input = null;
        String xpathidentifier = "//ows:Identifier";
        String xpathinput = "//wps:DataInputs/wps:Input/wps:Data/wps:LiteralData";

        try{
           identifier = xpath.evaluate(xpathidentifier, domNode);
           input =  xpath.evaluate(xpathinput, domNode);
        }catch(Exception e){
            e.printStackTrace();
        }
        //OMNamespace wpsNs = fac.createOMNamespace("http://scenz.lcr.co.nz/wpsHelloWorld", "wps");
        OMNamespace wpsNs = fac.createOMNamespace("http://scenz.lcr.co.nz/"+ identifier, "wps");
        // creating the payload
        
        //TODO: parse the domNode to a request doc
        //OMElement method = fac.createOMElement("wpsHelloWorldRequest", wpsNs);
        OMElement method = fac.createOMElement(identifier + "Request", wpsNs);
        OMElement value = fac.createOMElement("input", wpsNs);
        //value.setText("Niels");
        value.setText(input);
        method.addChild(value);
        envelope.getBody().addChild(method);
        return envelope;
    }
    
	@SuppressWarnings("unchecked")
	private SOAPEnvelope createSOAPEnvelope(ExecuteDocument execDoc) {

		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();

		NamespaceContext ctx = new NamespaceContext() {

			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("wps"))
					uri = "http://www.opengis.net/wps/1.0.0";
				else if (prefix.equals("ows"))
					uri = "http://www.opengis.net/ows/1.1";
				else
					uri = null;
				return uri;
			}

			public String getPrefix(String namespaceURI) {
				return null;
			}

			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}
		};

		_client = new ODEServiceClient();
		HashMap<String, String> allProcesses = new HashMap<String, String>();

		OMElement listRoot = _client.buildMessage("listAllProcesses",
				new String[] {}, new String[] {});

		OMElement result = null;
		try {
			result = sendToPM(listRoot);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<OMElement> pi = result.getFirstElement().getChildrenWithName(
				new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/",
						"process-info"));

		while (pi.hasNext()) {
			OMElement omPID = pi.next();

			String fullName = omPID
					.getFirstChildWithName(
							new QName(
									"http://www.apache.org/ode/pmapi/types/2006/08/02/",
									"pid")).getText();
			allProcesses.put(
					fullName.substring(fullName.indexOf("}") + 1,
							fullName.indexOf("-")),
					fullName.substring(1, fullName.indexOf("}")));

		}

		String identifier = execDoc.getExecute().getIdentifier()
				.getStringValue();

		OMNamespace wpsNs = null;

		for (String string : allProcesses.keySet()) {

			if (string.equals(identifier)) {
				wpsNs = fac.createOMNamespace(allProcesses.get(string), "nas");
				break;
			}

		}
		// creating the payload

		// TODO: parse the domNode to a request doc
		// OMElement method = fac.createOMElement("wpsHelloWorldRequest",
		// wpsNs);
		OMElement method = fac.createOMElement(identifier + "Request", wpsNs);
		envelope.getBody().addChild(method);

		DataInputsType datainputs = execDoc.getExecute().getDataInputs();

		for (InputType input1 : datainputs.getInputArray()) {

			String inputIdentifier = input1.getIdentifier().getStringValue();
			OMElement value = fac.createOMElement(inputIdentifier, "", "");
			if (input1.getData() != null
					&& input1.getData().getLiteralData() != null) {
				value.setText(input1.getData().getLiteralData()
						.getStringValue());
			} else {
				// Node no =
				// input1.getData().getComplexData().getDomNode().getChildNodes().item(1);
				// value.setText("<![CDATA[" + nodeToString(no) + "]>");
				// value.addChild(no);
				OMElement reference = fac.createOMElement("Reference",
						"http://www.opengis.net/wps/1.0.0", "wps");
				OMNamespace xlin = fac.createOMNamespace(
						"http://www.w3.org/1999/xlink", "xlin");

				OMAttribute attr = fac.createOMAttribute("href", xlin, input1
						.getReference().getHref());
				reference.addAttribute(attr);
				reference.addAttribute("schema", input1.getReference()
						.getSchema(), fac.createOMNamespace("", ""));
				value.addChild(reference);
			}
			method.addChild(value);
		}

		return envelope;

	}
}
