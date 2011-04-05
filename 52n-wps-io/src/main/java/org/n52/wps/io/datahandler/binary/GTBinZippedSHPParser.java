package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

public class GTBinZippedSHPParser extends AbstractGTBinZippedSHPParser {
		/**
	 * @throws RuntimeException
	 *             if an error occurs while writing the stream to disk or
	 *             unzipping the written file
	 * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
	 */
	@Override
	public IData parse(InputStream input, String mimeType) throws RuntimeException {
		try {
			String fileName = "tempfile" + UUID.randomUUID() + ".zip";
			File tempFile = new File(fileName);
			try {
				FileOutputStream outputStream = new FileOutputStream(tempFile);
				byte buf[] = new byte[4096];
				int len;
				while ((len = input.read(buf)) > 0) {
					outputStream.write(buf, 0, len);
				}
				outputStream.close();
				input.close();
			} catch (FileNotFoundException e) {
				System.gc();
				tempFile.delete();
				throw new RuntimeException(e);
			} catch (IOException e1) {
				System.gc();
				tempFile.delete();
				throw new RuntimeException(e1);
			}
			File shp = IOUtils.unzip(tempFile, "shp").get(0);
			DataStore store = new ShapefileDataStore(shp.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(
					store.getTypeNames()[0]).getFeatures();
			System.gc();
			tempFile.delete();
			
			
			
			return new GTVectorDataBinding(features);
		} catch (IOException e) {
			throw new RuntimeException(
					"An error has occurred while accessing provided data", e);
		}
	}


	
}
