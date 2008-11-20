package org.n52.wps.transactional.deploy.bpel.apache;

import java.io.BufferedInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.deploy.AbstractDeployManager;
import org.n52.wps.transactional.deploy.IDeployManager;
import org.n52.wps.transactional.deploymentprofiles.BPELDeploymentProfile;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.apache.ode.axis2.service.ServiceClientUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
        

public class ApacheBPELManager extends AbstractDeployManager {

    private ServiceClientUtil _client;
    private OMFactory _factory;
    private String deplyomentEndpoint;
    private String processManagerEndpoint;
    
    public ApacheBPELManager(ITransactionalAlgorithmRepository repository) {
    	super(repository);
    	Property[] properties = WPSConfig.getInstance().getPropertiesForRepositoryClass(repository.getClass().getName());
		//TODO think of multiple instance of this class registered (yet not possible since singleton)
		Property deployEndpointProperty = WPSConfig.getInstance().getPropertyForKey(properties, "ODE-Engine_DeploymentEndpoint");
		if(deployEndpointProperty==null){
			throw new RuntimeException("Error. Could not find ODE-Engine_DeploymentEndpoint");
		}
		deplyomentEndpoint = deployEndpointProperty.getStringValue();
		Property processManagerEndpointProperty = WPSConfig.getInstance().getPropertyForKey(properties, "ODE-Engine_ProcessManagerEndpoint");
		if(deployEndpointProperty==null){
			throw new RuntimeException("Error. Could not find ODE-Engine_ProcessManagerEndpoint");
		}
		processManagerEndpoint = processManagerEndpointProperty.getStringValue();
	}

    
    public boolean deployProcess(DeployProcessRequest request) throws Exception {
        // WPS-T preparation
        DeploymentProfile profile = request.getDeploymentProfile();
        if(! (profile instanceof BPELDeploymentProfile)){
                throw new Exception("Requested Deployment Profile not supported");
        }
        BPELDeploymentProfile deploymentProfile = (BPELDeploymentProfile) profile; 
        String processID = deploymentProfile.getProcessID();
        Node suitcase = deploymentProfile.getSuitCase();
        Node workflow = deploymentProfile.getBPEL();
        Node clientWSDL = deploymentProfile.getClientWSDL();
        Map<Integer, Node> wsdlList = deploymentProfile.getWSDLList();

        //ODE preparation
        OMNamespace pmapi = _factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
        OMElement root = _factory.createOMElement("deploy", pmapi); // qualified operation name
        OMElement namePart = _factory.createOMElement("name", null);
        namePart.setText(processID);
        OMElement zipPart = _factory.createOMElement("package", null);
        OMElement zipElmt = _factory.createOMElement("zip", null);
        
        int res = ZipCreator.makeZIP(processID, suitcase, workflow, clientWSDL, wsdlList, "C:\\temp\\wps-t.zip");
         try{
            InputStream is = new BufferedInputStream(new FileInputStream("C:\\temp\\wps-t.zip"));

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

                // Deploy
                sendToDeployment(root);
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        return true;     
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
        //String serializedXML = writeXMLToStream(new DOMSource(domNode)).toString();
        
//      serializedXML = serializedXML.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
//	serializedXML = serializedXML.replace("<Execute xmlns=\"http://www.opengis.net/wps/1.0.0\">","<Execute xmlns=\"http://www.opengis.net/wps/1.0.0\" version=\"1.0.0\" service=\"WPS\">");
//	serializedXML = serializedXML.replace(" href"," xmlns:xlin=\"http://www.w3.org/1999/xlink\" xlin:href");
        
        ServiceClient client = new ServiceClient();
        OperationClient operationClient = client.createClient(ServiceClient.ANON_OUT_IN_OP);
        //creating message context
        MessageContext outMsgCtx = new MessageContext();
        //assigning message context’s option object into instance variable
        Options opts = outMsgCtx.getOptions();
        //setting properties into option
        
        //TODO is this correct?
        opts.setTo(new EndpointReference("http://shelob:8081/ode/processes/wps-t"));
        opts.setAction("");
        outMsgCtx.setEnvelope(creatSOAPEnvelope(domNode));
        operationClient.addMessageContext(outMsgCtx);
        operationClient.execute(true);
        MessageContext inMsgtCtx = operationClient.getMessageContext("In");
        SOAPEnvelope response = inMsgtCtx.getEnvelope();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder =factory.newDocumentBuilder();

        return builder.parse(response.toString());
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getAllProcesses() throws Exception {
       try{
    	   List<String> allProcesses = new ArrayList<String>();
      
    	   OMElement root = _client.buildMessage("listAllProcesses", new String[] {"filter", "orderKeys"}, new String[] {});
                
    	   OMElement result = sendToPM(root);
    	   Iterator<OMElement> pi = result.getFirstElement().getChildrenWithName(
                       new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/","process-info"));
        
    	   while (pi.hasNext()) {
    		   OMElement omPID = pi.next();
    		   allProcesses.add(omPID.getFirstChildWithName(
                            new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/","pid")).getText());
    	   }
    	   return allProcesses;
       }catch(Exception e){
    	   return new ArrayList<String>();
       }
      
        
    }

    public boolean containsProcess(String processID) throws Exception {
        return getAllProcesses().contains(processID);
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean unDeployProcess(UndeployProcessRequest request) throws Exception {
        return false;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

   /* public static void main(String[] args){
	try{
            _client = new ServiceClientUtil();
            _factory = OMAbstractFactory.getOMFactory();
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/
    
     private OMElement sendToPM(OMElement msg) throws AxisFault {
    	
        return _client.send(msg, this.processManagerEndpoint);
    }

    private OMElement sendToDeployment(OMElement msg) throws AxisFault {
        return _client.send(msg,this.deplyomentEndpoint);
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
    
    public  SOAPEnvelope creatSOAPEnvelope(Node domNode) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
        // creating the payload
        
        //TODO: parse the domNode to a request doc
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("echo", omNs);
        value.setText("Hello");
        method.addChild(value);
        envelope.getBody().addChild(method);
        return envelope;
    }
}
