package org.n52.wps.io.datahandler.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.w3c.dom.Node;

public class GRASSXMLGenerator extends AbstractXMLGenerator {

	
	private static Logger LOGGER = Logger.getLogger(GML2BasicGenerator.class);
	private static String[] SUPPORTED_SCHEMAS = new String[]{
//		"http://schemas.opengis.net/gml/2.1.1/feature.xsd",
		"http://schemas.opengis.net/gml/2.1.2/feature.xsd",
//		"http://schemas.opengis.net/gml/2.1.2.1/feature.xsd",
//		"http://schemas.opengis.net/gml/3.0.0/base/feature.xsd",
//		"http://schemas.opengis.net/gml/3.0.1/base/feature.xsd",
//		"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd"
		};
	
	@Override
	public Node generateXML(IData coll, String schema) {
		if(coll instanceof GenericFileDataBinding){
			
			GenericFileData fileData = ((GenericFileDataBinding)coll).getPayload();
			
			try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Node node = builder.parse(fileData.getDataStream());
//			if (f != null) f.delete();
			return node;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	public OutputStream generate(IData coll) {
		LargeBufferStream baos = new LargeBufferStream();
//		this.writeToStream(coll, baos);		
		return baos;
	}

	/**
	 * Returns an array having the supported schemas.
	 */
	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	/**
	 * Returns true if the given schema is supported, else false.
	 */
	public boolean isSupportedSchema(String schema) {
		for(String supportedSchema : SUPPORTED_SCHEMAS) {
			if(supportedSchema.equals(schema))
				return true;
		}
		return false;
	}

	@Override
	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	
	}
	
}
