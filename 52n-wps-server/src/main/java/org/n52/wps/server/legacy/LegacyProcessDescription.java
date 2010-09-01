/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, Christin Henzen (TU Dresden)
 
 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.server.legacy;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

public class LegacyProcessDescription {//implements ILegacyDescription{
	
	private static Logger LOGGER = Logger.getLogger(LegacyProcessDescription.class);
	
	private URI algorithmLocation;  //name of toolbox - fe: arctoolbox
	private URI algorithmContainerLocation;//location of tb - fe: ./buffer.tbx
	private URI algorithmContainerURN;  //unique path of toolbox - fe: urn:n52:wps:algorithmcontainer:arctoolbox:9.3
	private URL algorithmWorkspaceLocation;//unique path to ws-location - fe: urn:n52:wps:algorithmcontainer:arctoolbox:9.3
	private ArrayList<URI> legacyBackends;   //array of all required GPSSystems - fe: urn:n52:wps:gpsystem:arcgis:9.3
 
	private HashMap<String, LegacyParameter> algorithmParameters; //map of input and output parameters 
	private boolean sequentialParameters;
	
	public LegacyProcessDescription() {
		algorithmParameters = new HashMap<String, LegacyParameter>();
	}
	
	protected LegacyProcessDescription(File algorithmDescriptionXML){
		algorithmParameters = new HashMap<String, LegacyParameter>();
		legacyBackends = new ArrayList<URI>();
		Document legacyAlgoDoc = generateAlgorithmDescription(algorithmDescriptionXML);
		loadDescription(legacyAlgoDoc);
	}
 
	public final void addNamedParameter(LegacyParameter parameter, String parameterID) {
		algorithmParameters.put(parameterID, parameter);
	}

	public final LegacyParameter getParameterByUniqueID(String id) {
		LegacyParameter parameter = algorithmParameters.get(id);
		return parameter;
	}
 
	public final void loadDescription (Document algorithmDescriptionXML){
		try {
			analyzeAlgoDescription(algorithmDescriptionXML);
		} catch (Exception e) {e.printStackTrace();}
	}

	//--parsing and loading operations ---------------------------------

	/**
     * Generates an XML file. Uses a ProcessDescription file and a XSLT file to generate an
     * AlgorithmDescription.
     * 
     * @param xmlFile - the ProcessDescription in XML
     * @param xsltFile - the transformation rules in XSLT
     * @throws Exception - the exceptions
     */
    private final Document generateAlgorithmDescription(File xmlFile) {
    	
    	Source xmlSource = new StreamSource(xmlFile);
    	TransformerFactory transFact = TransformerFactory.newInstance();
    	StringWriter sw = new StringWriter();
        StreamResult transformResult = new StreamResult(sw);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
    	
    	try {
			
    		Source xsltSource = transFact.getAssociatedStylesheet(xmlSource, null, null, null);
			Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, transformResult);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(sw.toString())));
			return document;
			
		} catch (TransformerException e) {
			LOGGER.error("Error while evaluating ProcessDescription XML.");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			LOGGER.error("Error while creating legacy description XML.");
			e.printStackTrace();
		} catch (SAXException e) {
			LOGGER.error("Error while converting ProcessDescription to legacy description.");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Error while writing legacy description.");
			e.printStackTrace();
		}

		return null;
    }
    
    private final void analyzeAlgoDescription (Node node) throws DOMException, URISyntaxException, MalformedURLException{
    	
    	for(int b=0; b<node.getChildNodes().getLength(); b++){
			
			Node actNode = node.getChildNodes().item(b);
			String actName = actNode.getNodeName();
			String actTC = actNode.getTextContent();
			
			if(actNode.hasChildNodes()) analyzeAlgoDescription(actNode);
			
			if(actName.contains("algorithmParameters")){
				NamedNodeMap attributes = actNode.getAttributes();
				String testSequential = attributes.getNamedItem("sequential").getNodeValue();
				if (testSequential.equalsIgnoreCase("TRUE")) sequentialParameters = true;
				else sequentialParameters = false;
			}
			
			if(actName.contains("parameter")){
				analyzeParam(actNode);
			}

			if(actName.contains("algorithmWorkspaceLocation")){
				algorithmWorkspaceLocation = new URL(actTC);
			}
			if(actName.contains("algorithmContainerLocation")){
				algorithmContainerLocation = new URI(actTC);
			}
			if(actName.contains("algorithmLocation")){
				algorithmLocation = new URI(actTC);
			}
			if(actName.contains("algorithmContainerURN")){
				algorithmContainerURN = new URI(actTC);
			}
			if(actName.contains("processingSystemURN")){
				legacyBackends.add(new URI(actTC));
			}
		}// end for
    	
	}// end of analyzeAlgo()
    
    /**
     * Analyzes all incoming parameter nodes and their attributes.
     * 
     * @param node - parameter node
     */
    private final void analyzeParam(Node node){
    	String wpsInputID = null;
    	String wpsOutputID = null;
    	String gpParameterID = null;
		String wpsComplexDataSchema = null;
		String wpsMimeType = null;
		String wpsLiteralDataType = null;
		String wpsDefaultCRS = null;
		String prefixString = null;
		String suffixString = null;
		String separatorString = null; 
    	
    	for(int b=0; b<node.getChildNodes().getLength(); b++){
    		
			String actName = node.getChildNodes().item(b).getNodeName();
			String actTC = node.getChildNodes().item(b).getTextContent();
			
			if(actName.contains("prefixString")) prefixString=actTC;
			if(actName.contains("suffixString")) suffixString=actTC;
			if(actName.contains("separatorString")) separatorString=actTC;
			if(actName.contains("wpsInputID")) wpsInputID=actTC;
			if(actName.contains("wpsOutputID")) wpsOutputID=actTC;
			if(actName.contains("wpsDataSchema")) wpsComplexDataSchema=actTC;
			if(actName.contains("wpsMimeType")) wpsMimeType=actTC;
			if(actName.contains("wpsLiteralDataTye")) wpsLiteralDataType=actTC;
			if(actName.contains("wpsDefaultCRS")) wpsDefaultCRS=actTC;
			
			// maybe rework this section, apply checks ...
			if(actName.contains("legacyIntID")) gpParameterID=actTC;
			if(actName.contains("legacyStringID")) gpParameterID=actTC;
			
    	}// end for	
    	
    	//LegacyParameter-Constructor public gesetzt
    	LegacyParameter lParam = new LegacyParameter(wpsInputID, wpsOutputID, 
    			gpParameterID, wpsComplexDataSchema, wpsMimeType, wpsLiteralDataType, 
    			wpsDefaultCRS, prefixString, suffixString, separatorString, false);
    	
    	algorithmParameters.put(gpParameterID, lParam);
    }// end analyzeParameter()
    
	public final LegacyParameter[] getParamsAsArray(){
		
		LegacyParameter[] paramArray = null;
		
		if((this.algorithmParameters.size() > 0) && this.sequentialParameters){
			Set<String> keys = this.algorithmParameters.keySet();
			SortedSet<Integer> intKeys = new TreeSet<Integer>();
			
			for(String currentKey : keys){
				intKeys.add(Integer.parseInt(currentKey));
			}
			
			int maxElement = intKeys.last().intValue();
			paramArray = new LegacyParameter[maxElement + 1]; //length = maxElement + 1
			
			for(String currentKey : keys){
				int i = Integer.parseInt(currentKey);
				paramArray[i] = this.algorithmParameters.get(currentKey);
			}
		}
		
		return paramArray;
	}
    
    //---------------------------------------------------------------------------------    
    //--getters and setters -------------------------------------------------------------
    //---------------------------------------------------------------------------------     
    

	public final URI getAlgorithmLocation() {
		return algorithmLocation;
	}

	public final void setAlgorithmLocation(URI algorithmLocation) {
		this.algorithmLocation = algorithmLocation;
	}

	public final URI getAlgorithmContainerLocation() {
		return algorithmContainerLocation;
	}

	public final void setAlgorithmContainerLocation(URI algorithmContainerLocation) {
		this.algorithmContainerLocation = algorithmContainerLocation;
	}

	public final URI getAlgorithmContainerURN() {
		return algorithmContainerURN;
	}

	public final void setAlgorithmContainerURN(URI algorithmContainerURN) {
		this.algorithmContainerURN = algorithmContainerURN;
	}

	public final URL getAlgorithmWorkspaceLocation() {
		return algorithmWorkspaceLocation;
	}

	public final void setAlgorithmWorkspaceLocation(URL algorithmWorkspaceLocation) {
		this.algorithmWorkspaceLocation = algorithmWorkspaceLocation;
	}

	public final URI[] getLegacyBackends() {
		URI[] backends = legacyBackends.toArray(new URI[legacyBackends.size()]);
		return backends;
	}

	public final void setLegacyBackends(URI[] legacyBackends) {
		this.legacyBackends = new ArrayList<URI>();
		for (URI currentURI : legacyBackends){
			this.legacyBackends.add(currentURI);
		}
	}

	public final HashMap<String, LegacyParameter> getAlgorithmParameters() {
		return algorithmParameters;
	}

	public final void setAlgorithmParameters(
			HashMap<String, LegacyParameter> algorithmParameters) {
		this.algorithmParameters = algorithmParameters;
	}

	public final boolean isSequential() {
		return this.sequentialParameters;
	}

	public final void setSequential(boolean isSequential) {
		this.sequentialParameters = isSequential;
	}
    
}//end of LegacyAlgorithmDescription