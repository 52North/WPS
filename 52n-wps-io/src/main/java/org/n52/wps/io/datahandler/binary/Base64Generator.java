package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.encoding.Base64;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.FileDataBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Base64Generator extends AbstractXMLGenerator implements
		IStreamableGenerator {

	@Override
	public Node generateXML(IData coll, String schema) {
		try {
			// Build the text node with the base64 encoding
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.newDocument();
			Node ret = document.createTextNode(getBase64Encoding(coll));

			return ret;
		} catch (DOMException e) {
			throw new RuntimeException("An error has occurred while building "
					+ "the XML response", e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("An error has occurred while building "
					+ "the XML response", e);
		} catch (IOException e) {
			throw new RuntimeException("An error has occurred while accessing "
					+ "the calculated results", e);
		} catch (Throwable e) {
			throw new RuntimeException("An error has occurred while "
					+ "generating the base64 encoding", e);
		}
	}

	@Override
	public OutputStream generate(IData coll) {
		LargeBufferStream stream = new LargeBufferStream();
		this.writeToStream(coll, stream);
		return stream;
	}

	@Override
	public Class<?>[] getSupportedInternalInputDataType() {
		return new Class<?>[] { FileDataBinding.class };
	}

	@Override
	public String[] getSupportedSchemas() {
		return new String[] {};
	}

	@Override
	public boolean isSupportedEncoding(String encoding) {
		return encoding.equals(IOHandler.ENCODING_BASE64);
	}

	@Override
	public boolean isSupportedSchema(String schema) {
		return schema == null;
	}

	@Override
	public void writeToStream(IData data, OutputStream os) {
		try {
			String encoded = getBase64Encoding(data);
			os.write(encoded.getBytes());
		} catch (IOException e) {
			throw new RuntimeException("An error has occurred while accessing "
					+ "the calculated results", e);
		} catch (Throwable e) {
			throw new RuntimeException("An error has occurred while "
					+ "generating the base64 encoding", e);
		}
	}

	private String getBase64Encoding(IData data) throws IOException {
		if (!(data instanceof FileDataBinding)) {
			throw new IllegalArgumentException("Unsupported IData type: "
					+ data.getClass() + ". Expecting: " + FileDataBinding.class);
		}

		// Get the data source from the binding
		FileDataBinding binding = (FileDataBinding) data;
		File file = binding.getPayload();

		// Get base64 encoding for source file
		InputStream is = new FileInputStream(file);
		if (file.length() > Integer.MAX_VALUE) {
			throw new IOException("File is too large to process");
		}
		byte[] bytes = new byte[(int) file.length()];
		is.read(bytes);

		// Build the text node with the base64 encoding
		return Base64.encode(bytes);
	}
	
	@Override
	public boolean isSupportedFormat(String format) {
		return true;
	}
}
