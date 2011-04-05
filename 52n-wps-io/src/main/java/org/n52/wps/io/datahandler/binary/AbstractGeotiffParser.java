package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public abstract class AbstractGeotiffParser extends AbstractBinaryParser {
	protected static Logger LOGGER = Logger.getLogger(GeotiffParser.class);
	
	protected IData parseTiff(InputStream input) {
		File tempFile;
		try {
            tempFile = File.createTempFile("tempfile" + UUID.randomUUID(),"tmp");
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
			//tempFile.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e1) {
			System.gc();
			//tempFile.delete();
			LOGGER.error(e1);
			throw new RuntimeException(e1);
		}

		return parseTiff(tempFile);

	}
	
	protected IData parseTiff(File file){
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		GeoTiffReader reader;
		try {
			reader = new GeoTiffReader(file, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);

			System.gc();
			file.delete();
			return new GTRasterDataBinding(coverage);
		} catch (DataSourceException e) {
			System.gc();
			file.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.gc();
			file.delete();
			LOGGER.error(e);
			throw new RuntimeException(e);
		}

	}

	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = { GTRasterDataBinding.class };
		return supportedClasses;
	}
}
