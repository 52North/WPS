package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.IData;

public class GeotiffBase64Parser extends AbstractGeotiffParser {
	@Override
	public IData parse(InputStream input) {
		try {
			File tiff = IOUtils.writeBase64ToFile(input, "tiff");
			IData data = parseTiffFile(new FileInputStream(tiff));
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
