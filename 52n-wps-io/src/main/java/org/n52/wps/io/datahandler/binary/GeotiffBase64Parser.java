package org.n52.wps.io.datahandler.binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class GeotiffBase64Parser extends AbstractXMLParser {
	private GeotiffParser parser = new GeotiffParser();

	@Override
	public IData parse(InputStream input) {
		try {
			File tiff = IOUtils.writeBase64ToFile(input, "tiff");
			IData data = parser.parse(new FileInputStream(tiff));
			tiff.delete();
			return data;
		} catch (IOException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		}
	}

	@Override
	public boolean isSupportedEncoding(String encoding) {
		return encoding.equals(IOHandler.ENCODING_BASE64);
	}

	@Override
	public IData parseXML(String xml) {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	@Override
	public IData parseXML(InputStream stream) {
		try {
			File tiff = IOUtils.writeBase64XMLToFile(stream, "tif");
			IData data = parser.parse(new FileInputStream(tiff));
			tiff.delete();
			return data;
		} catch (DOMException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		} catch (SAXException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		} catch (TransformerException e) {
			throw new RuntimeException("Cannot parse base64 tiff image", e);
		}
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return parser.getSupportedInternalOutputDataType();
	}

	@Override
	public String[] getSupportedSchemas() {
		return new String[] {};
	}

	@Override
	public boolean isSupportedSchema(String schema) {
		return schema == null;
	}

	@Override
	public String[] getSupportedFormats() {
		return parser.getSupportedFormats();
	}
}
