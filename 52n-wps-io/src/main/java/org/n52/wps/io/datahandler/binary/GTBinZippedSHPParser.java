package org.n52.wps.io.datahandler.binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.axis.encoding.Base64;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class GTBinZippedSHPParser extends AbstractXMLParser {
	/**
	 * @throws RuntimeException
	 *             If an error occurs while parsing the sent XML, unzipping the
	 *             zipped shapefile or the feature collection cannot be obtained
	 *             from the shapefile
	 * @see org.n52.wps.io.datahandler.xml.AbstractXMLParser#parseXML(java.lang.String)
	 */
	@Override
	public IData parseXML(String xml) throws RuntimeException {
		return parseXML(new ByteArrayInputStream(xml.getBytes()));
	}

	/**
	 * @throws RuntimeException
	 *             If an error occurs while parsing the sent XML, unzipping the
	 *             zipped shapefile or the feature collection cannot be obtained
	 *             from the shapefile
	 * @see org.n52.wps.io.datahandler.xml.AbstractXMLParser#parseXML(java.io.InputStream)
	 */
	@Override
	public IData parseXML(InputStream stream) throws RuntimeException {
		try {
			// Create XML document
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(stream);

			String binaryContent = XPathAPI.selectSingleNode(
					document.getFirstChild(), "text()").getTextContent();

			// Flush binary data to temporary file
			File tmp = File.createTempFile("shapefile", ".zip", new File(System
					.getProperty("java.io.tmpdir")));
			FileOutputStream output = new FileOutputStream(tmp);
			Base64.decode(binaryContent, output);
			output.close();

			// Unzip and obtain the data binding
			File shp = org.n52.wps.io.IOUtils.unzip(tmp, "shp");

			if (shp == null) {
				throw new RuntimeException(
						"Cannot find a shapefile inside the zipped file.");
			}

			DataStore store = new ShapefileDataStore(shp.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			// TODO should we delete the shp file here?
			return new GTVectorDataBinding(features);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(
					"Cannot initialize parser for XML input", e);
		} catch (SAXException e) {
			throw new RuntimeException("Cannot parse XML input", e);
		} catch (IOException e) {
			throw new RuntimeException("An error has occurred while obtaining "
					+ "the feature collection from the sent file", e);
		} catch (DOMException e) {
			throw new RuntimeException("Cannot get data from the XML input", e);
		} catch (TransformerException e) {
			throw new RuntimeException("Cannot get data from the XML input", e);
		}
	}

	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		return new Class<?>[] { GTVectorDataBinding.class };
	}

	/**
	 * @throws RuntimeException
	 *             if an error occurs while writing the stream to disk or
	 *             unzipping the written file
	 * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
	 */
	@Override
	public IData parse(InputStream input) throws RuntimeException {
		try {
			File zipped = File.createTempFile("zip", ".zip", new File(System
					.getProperty("java.io.tmpdir")));
			FileOutputStream output = new FileOutputStream(zipped);
			IOUtils.copy(input, output);
			input.close();
			output.close();

			File shp = org.n52.wps.io.IOUtils.unzip(zipped, "shp");

			if (shp == null) {
				throw new RuntimeException(
						"Cannot find a shapefile inside the zipped file.");
			}

			DataStore store = new ShapefileDataStore(shp.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			// TODO should we delete the shp file here?
			return new GTVectorDataBinding(features);
		} catch (IOException e) {
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		}
	}

	@Override
	public String[] getSupportedFormats() {
		return new String[] { IOHandler.MIME_TYPE_ZIPPED_SHP };
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
}
