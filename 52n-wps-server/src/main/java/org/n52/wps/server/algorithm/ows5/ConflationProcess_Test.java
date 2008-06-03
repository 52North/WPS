package org.n52.wps.server.algorithm.ows5;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPEnvelope;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConflationProcess_Test extends AbstractAlgorithm{
	
	
	public static void main(String[] args){
		ConflationProcess_Test t = new ConflationProcess_Test();
		Map layers = new HashMap();
				
		Map parameters = new HashMap();
		parameters.put("WFS_Baseline_Address", "XXX");
		parameters.put("WFS_Update_Address", "YYY");
		parameters.put("Rules_Source_Address", "ZZZ");
		
		Map result = t.run(layers, parameters);
		String s = (String) result.get("Result_WFS_Address");
		System.out.println(s);
		
	}
	

	public Map run(Map layers, Map parameters) {
		String result = "";
		try {
			String endpoint = "http://65.123.203.154:8099/bpelasync/ExecuteBPELProcess?name=process&idNS=http://xmlns.ows5.org/ConflationProcess_Test&idName=ConflationProcess_Test&item=testing&value=null";
				
			PostClient client = new PostClient();
			String value = client.buildRequest(createRequestDocument(parameters));
			result = client.sendRequest(endpoint, value);
			
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		HashMap<String,Object> resulthash = new HashMap<String,Object>();
		resulthash.put("Result_WFS_Address", result);
		return resulthash;
		
	}

	

	

	private String createRequestDocument(Map parameters) throws ParserConfigurationException {
		
		String wfs1URL = (String) parameters.get("WFS_Baseline_Address");
		String wfs2URL = (String) parameters.get("WFS_Update_Address");
		String ruleSourceURL = (String) parameters.get("Rules_Source_Address");
		
		
		return "<ConflationProcessProcessRequest xmlns=\"http://xmlns.ows5.com/TestProcess\"><WFS_Baseline_Address>"+wfs1URL+"</WFS_Baseline_Address><WFS_Update_Address>"+wfs2URL+"</WFS_Update_Address><Rules_Source_Address>"+ruleSourceURL+"</Rules_Source_Address></ConflationProcessProcessRequest>";	
	}

	public String getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

}
