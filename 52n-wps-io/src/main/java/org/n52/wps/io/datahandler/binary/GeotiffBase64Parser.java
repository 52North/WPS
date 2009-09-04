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

public class GeotiffBase64Parser extends AbstractGeotiffParser {
	

	@Override
	public IData parse(InputStream input) {
		try {
			File tiff = IOUtils.writeBase64ToFile(input, "tiff");
			IData data = parseTiff(new FileInputStream(tiff));
			System.gc();
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

	
}
