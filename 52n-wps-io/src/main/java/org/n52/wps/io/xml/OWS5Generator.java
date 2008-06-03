package org.n52.wps.io.xml;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.examples.packet.GMLPacketDocument;
import noNamespace.PropertyDocument.Property;

import org.geotools.feature.FeatureCollection;
import org.geotools.xml.DocumentWriter;
import org.n52.wps.io.IStreamableGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class OWS5Generator extends AbstractXMLGenerator implements IStreamableGenerator{

	private static String SUPPORTED_SCHEMA = "http://v-ebiz.uni-muenster.de:8080/wps100/wps/1.0.0/algorithms/testAlgorithm/OWS5response.xsd";
	
	
	@Override
	public Node generateXML(Object coll, String schema) {
		String content = (String) coll;
		Writer fw;
		Document doc = null;
		try {
			fw = new FileWriter(new File("test.xml"));
		
		fw.write(content);
		fw.close();
		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("test.xml");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error " + e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException("Error " + e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error " + e.getMessage());
		}
		return doc;
	}

	public OutputStream generate(Object coll) {
		return null;
	}

	

	public String[] getSupportedSchemas() {
		return new String[]{SUPPORTED_SCHEMA};
	}

	
	public String[] getSupportedRootClasses() {
		return new String[]{String.class.getName()};
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	public boolean isSupportedRootClass(String clazzName) {
		if(clazzName.equals(String.class.getName())) {
			return true;
		}
		return false;
	}

	public boolean isSupportedSchema(String schema) {
		return SUPPORTED_SCHEMA.equals(schema);
	}

	public void write(Object coll, Writer writer) {
		String content = (String) coll;
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			bufferedWriter.write(content);
			bufferedWriter.flush();
			bufferedWriter.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	

}
