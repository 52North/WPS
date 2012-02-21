/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: 

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

package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GeotiffBinding;
import org.n52.wps.io.data.binding.complex.ShapefileBinding;
import org.n52.wps.io.datahandler.generator.GeoServerUploader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class GeoserverWMSGenerator extends AbstractGenerator {
	
	private String username;
	private String password;
	private String host;
	private String port;
	
	public GeoserverWMSGenerator() {
		
		super();
		this.supportedIDataTypes.add(GTRasterDataBinding.class);
		this.supportedIDataTypes.add(ShapefileBinding.class);
		this.supportedIDataTypes.add(GeotiffBinding.class);
		this.supportedIDataTypes.add(GTVectorDataBinding.class);
		
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
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
//		// check for correct request before returning the stream
//		if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//			throw new IOException("I don't support the incoming datatype");
//		}

		InputStream stream = null;	
		try {
			Document doc = storeLayer(data);
			DOMSource domSource = new DOMSource(doc);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			sw.flush();
			sw.close();
			
			stream = new ByteArrayInputStream(sw.toString().getBytes("UTF-8"));
			
	    }
	    catch(TransformerException ex){
	    	ex.printStackTrace();
	    	throw new RuntimeException("Error generating WCS output. Reason: " + ex);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	throw new RuntimeException("Error generating WCS output. Reason: " + e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error generating WCS output. Reason: " + e);
		}	
		return stream;
	}
	
	private Document storeLayer(IData coll) throws HttpException, IOException, ParserConfigurationException{
		File file = null;
		String storeName = "";
		String wmsLayerName = "";
		if(coll instanceof GTVectorDataBinding){
			GTVectorDataBinding gtData = (GTVectorDataBinding) coll;
			
			try {
				GenericFileData fileData = new GenericFileData(gtData.getPayload());
				file = fileData.getBaseFile(true);
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
			file = fileData.getBaseFile(true);
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
	
		storeName = storeName +"_" + UUID.randomUUID();
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
				
		String capabilitiesLink = "http://"+host+":"+port+"/geoserver/wms?Service=WMS&Request=GetCapabilities&Version=1.1.1";
		//String directLink = geoserverBaseURL + "?Service=WMS&Request=GetMap&Version=1.1.0&Layers=N52:"+wmsLayerName+"&WIDTH=300&HEIGHT=300";;
		
		Document doc = createXML("N52:"+storeName, capabilitiesLink);
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
	
}
