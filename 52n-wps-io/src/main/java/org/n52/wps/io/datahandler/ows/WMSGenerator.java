package org.n52.wps.io.datahandler.ows;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GeotiffBinding;
import org.n52.wps.io.data.binding.complex.ShapefileBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class WMSGenerator extends AbstractXMLGenerator{
	
	private String username;
	private String password;
	private String host;
	private String port;
	
	public WMSGenerator() {
		
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
		port = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();
		
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
			throw new RuntimeException("Error generating WMS output. Reason: " + e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WMS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WMS output. Reason: " + e);
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
	    	throw new RuntimeException("Error generating WMS output. Reason: " + ex);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error generating WMS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WMS output. Reason: " + e);
		}
		
		return baos;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		File file = null;
		String storeName = "";
		String wmsLayerName = "";
		if(coll instanceof GTVectorDataBinding){
			GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
			
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

			file = zipped;
			wmsLayerName = new File(path).getName().substring(0, new File(path).getName().length()-4);
			
		}
		if(coll instanceof GTRasterDataBinding){
			GTRasterDataBinding gtData = (GTRasterDataBinding) coll;
			GenericFileData fileData = new GenericFileData(gtData.getPayload(), null);
			file = fileData.getBaseFile();
			int lastIndex = file.getName().lastIndexOf(".");
			wmsLayerName = file.getName().substring(0, lastIndex);
			
		}
		if(coll instanceof ShapefileBinding){
			ShapefileBinding data = (ShapefileBinding) coll;
			file = data.getZippedPayload();
			String path = file.getAbsolutePath();
			wmsLayerName = new File(path).getName().substring(0, new File(path).getName().length()-4);
			
		}
		if(coll instanceof GeotiffBinding){
			GeotiffBinding data = (GeotiffBinding) coll;
			file = (File) data.getPayload();
			String path = file.getAbsolutePath();
			wmsLayerName = new File(path).getName().substring(0, new File(path).getName().length()-4);
		}
		storeName = file.getName();			
	
		storeName = storeName +"_"+ System.currentTimeMillis();
		GeoServerUploader geoserverUploader = new GeoServerUploader(username, password, port);
		
		String result = geoserverUploader.createWorkspace();
		System.out.println(result);
		System.out.println("");
		if(coll instanceof GTVectorDataBinding){
			result = geoserverUploader.uploadShp(file, storeName);			
		}
		if(coll instanceof GTRasterDataBinding){
			result = geoserverUploader.uploadGeotiff(file, storeName);
		}
		
		System.out.println(result);
				
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/oms?Service=WMS&Request=GetCapabilities&Version=1.1.1";
		//String directLink = geoserverBaseURL + "?Service=WMS&Request=GetMap&Version=1.1.0&Layers=N52:"+wmsLayerName+"&WIDTH=300&HEIGHT=300";;
		
		Document doc = createXML("N52:"+wmsLayerName, capabilitiesLink);
		return doc;
	
	}
	
	private Document createXML(String layerName, String getCapabilitiesLink) throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().newDocument();
		
		Element root = doc.createElement("OWSResponse");
		root.setAttribute("type", "WMS");
		
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
		Class[] supportedClasses = {GTRasterDataBinding.class, GTVectorDataBinding.class};
		return supportedClasses;
	}
	
	public boolean isSupportedSchema(String schema) {
		return true;
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}
	
	
	
	
	

}
