package org.n52.wps.io.datahandler.ows;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpException;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class GeoserverWFSGenerator extends AbstractXMLGenerator{
	
	private String username;
	private String password;
	private String host;
	private String port;
	
	public GeoserverWFSGenerator() {
		
		properties = WPSConfig.getInstance().getPropertiesForGeneratorClass(this.getClass().getName());
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("Geoserver_username")){
				username = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_password")){
				password = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_host")){
				host = property.getStringValue();
			}
			if(property.getName().equalsIgnoreCase("Geoserver_port")){
				port = property.getStringValue();
			}
		}
		if(port == null){
			port = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		}
		for(String supportedFormat : supportedFormats){
			if(supportedFormat.equals("text/xml")){
				supportedFormats.remove(supportedFormat);
			}
		}
		
	}
	

	@Override
	public Node generateXML(IData coll, String schema) {
		Document doc;
		try {
			doc = storeLayer(coll);
		} catch (HttpException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WFS output. Reason: " + e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WFS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WFS output. Reason: " + e);
		}	
		return doc.getFirstChild();
		
	}

	@Override
	public OutputStream generate(IData coll) {
		LargeBufferStream baos = new LargeBufferStream();
			
		try
	    {
			Document doc = storeLayer(coll);
			DOMSource domSource = new DOMSource(doc);
	       OutputStreamWriter writer = new OutputStreamWriter(baos);
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       writer.flush();
	       writer.close();
	    }
	    catch(TransformerException ex){
	    	ex.printStackTrace();
	    	throw new RuntimeException("Error generating WFS output. Reason: " + ex);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error generating WFS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WFS output. Reason: " + e);
		}
		
		return baos;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
		File file = null;
		try {
			GenericFileData fileData = new GenericFileData(gtData.getPayload());
			file = fileData.getBaseFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Error generating shp file for storage in WFS. Reason: " + e1);
		}
		
		//zip shp file
		String path = file.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		File zipped =org.n52.wps.io.IOUtils.zip(file, shx, dbf, prj);

		
		String layerName = zipped.getName();
		layerName = layerName +"_" + UUID.randomUUID();
		GeoServerUploader geoserverUploader = new GeoServerUploader(username, password, port);
		
		String result = geoserverUploader.createWorkspace();
		System.out.println(result);
		System.out.println("");
		result = geoserverUploader.uploadShp(zipped, layerName);
		System.out.println(result);
		
		
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/wfs?Service=WFS&Request=GetCapabilities&Version=1.1.0";
		//String directLink = geoserverBaseURL + "?Service=WFS&Request=GetFeature&Version=1.1.0&typeName=N52:"+file.getName().subSequence(0, file.getName().length()-4);
		
		//delete shp files
		zipped.delete();
		file.delete();
		shx.delete();
		dbf.delete();
		prj.delete();
		Document doc = createXML("N52:"+file.getName().subSequence(0, file.getName().length()-4), capabilitiesLink);
		return doc;
	
	}
	
	private Document createXML(String layerName, String getCapabilitiesLink) throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().newDocument();
		
		Element root = doc.createElement("OWSResponse");
		root.setAttribute("type", "WFS");
		
		Element resourceIDElement = doc.createElement("ResourceID");
		resourceIDElement.appendChild(doc.createTextNode(layerName));
		root.appendChild(resourceIDElement);
		
		Element getCapabilitiesLinkElement = doc.createElement("GetCapabilitiesLink");
		getCapabilitiesLinkElement.appendChild(doc.createTextNode(getCapabilitiesLink));
		root.appendChild(getCapabilitiesLinkElement);
		/*
		Element directResourceLinkElement = doc.createElement("DirectResourceLink");
		directResourceLinkElement.appendChild(doc.createTextNode(getMapRequest));
		root.appendChild(directResourceLinkElement);
		*/
		doc.appendChild(root);
		
		return doc;
	}

	@Override
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTVectorDataBinding.class};
		return supportedClasses;
	}
	
	public boolean isSupportedSchema(String schema) {
		return true;
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

}
