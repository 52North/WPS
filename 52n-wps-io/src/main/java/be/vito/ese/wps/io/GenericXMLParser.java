package be.vito.ese.wps.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GenericXMLParser extends AbstractXMLParser{

	@Override
	public Class<?>[] getSupportedInternalOutputDataType(){
		return new Class [] {GenericXMLDataBinding.class};
	}

	@Override
	public IData parseXML(InputStream stream){
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		// documentBuilderFactory.setNamespaceAware(true);

		try{
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();			
			Document document = documentBuilder.parse(stream);
			return new GenericXMLDataBinding(document);
		}
		catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new RuntimeException("Error while parsing XML: " + ex.getMessage(), ex);
		}

	}
	
	@Override
	public IData parse(InputStream input, String mimeType){
		return parseXML(input);
	}
	
	@Override
	public IData parseXML(String xml){

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		FileOutputStream tmpFos = null;
		File tmpFile = null;
		// documentBuilderFactory.setNamespaceAware(true);				

		try{
			
			tmpFile = File.createTempFile("input", "xml");
			tmpFos = new FileOutputStream(tmpFile);
			
			StringReader xmlReader = new StringReader(xml);
			int i = xmlReader.read();
			while(i != -1){
				tmpFos.write(i);
				i = xmlReader.read();
			}
			tmpFos.close();			
			
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(tmpFile);
			return new GenericXMLDataBinding(document);
		}
		catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new RuntimeException("Error while parsing XML: " + ex.getMessage(), ex);
		}
		finally{
			if(tmpFile != null){
				tmpFile.delete();
			}
			if(tmpFos != null){
				try{
					tmpFos.close();
				}
				catch(IOException ex){
					// ignore
				}
			}
		}

	}	

}