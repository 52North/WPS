package org.n52.wps.io.datahandler.binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public abstract class  AbstractGTBinZippedSHPParser extends AbstractXMLParser {
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
		return parse(stream, null);
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
	public abstract IData parse(InputStream input, String mimeType);

	
	@Override
	public String[] getSupportedSchemas() {
		return new String[] {};
	}

	@Override
	public boolean isSupportedSchema(String schema) {
		return schema == null;
	}
}
