package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public abstract class AbstractGeotiffParser extends AbstractBinaryParser {
	protected static String[] SUPPORTED_FORMAT = {"image/tiff","image/geotiff"};
	protected static Logger LOGGER = Logger.getLogger(GeotiffParser.class);
	
	protected IData parseTiff(InputStream input) {
		String fileName = "tempfile" + System.currentTimeMillis();
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
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e1) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e1);
			throw new RuntimeException(e1);
		}

		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		GeoTiffReader reader;
		try {
			reader = new GeoTiffReader(tempFile, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);

			System.gc();
			tempFile.delete();
			return new GTRasterDataBinding(coverage);
		} catch (DataSourceException e) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.gc();
			tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		}

	}

	public String[] getSupportedFormats() {
		return SUPPORTED_FORMAT;
	
	}

	public boolean isSupportedFormat(String format) {
		for(String supportedFormat : SUPPORTED_FORMAT){
			if (supportedFormat.equalsIgnoreCase(format)) {
				return true;
			}
		}
		return false;
	}

	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = { GTRasterDataBinding.class };
		return supportedClasses;
	}
}
