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

public class GTBinZippedBase64SHPParser extends AbstractGTBinZippedSHPParser {
	

	public GTBinZippedBase64SHPParser() {
		super();
	}

	/**
	 * @throws RuntimeException
	 *             if an error occurs while writing the stream to disk or
	 *             unzipping the written file
	 * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
	 */
	@Override
	public IData parse(InputStream input, String mimeType) throws RuntimeException {
		try {
			File zipped = IOUtils.writeBase64ToFile(input, "zip");
			File shp = IOUtils.unzip(zipped, "shp");

			if (shp == null) {
				throw new RuntimeException(
						"Cannot find a shapefile inside the zipped file.");
			}

			DataStore store = new ShapefileDataStore(shp.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			zipped.delete();
			shp.delete();
			
			return new GTVectorDataBinding(features);
		} catch (IOException e) {
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		}
	}

}
