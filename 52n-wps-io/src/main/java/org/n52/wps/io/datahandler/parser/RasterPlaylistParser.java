package org.n52.wps.io.datahandler.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.binding.complex.RasterPlaylistBinding;

public class RasterPlaylistParser extends AbstractParser {
		
	public RasterPlaylistParser() {
		super();
		supportedIDataTypes.add(RasterPlaylistBinding.class);
	}
	
	public RasterPlaylistBinding parse(InputStream stream, String mimeType, String schema) {
	    ByteArrayOutputStream into = new ByteArrayOutputStream();
	    byte[] buf = new byte[4096];
	    try {
			for (int n; 0 < (n = stream.read(buf));) {
			    into.write(buf, 0, n);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			into.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    String url = null;
		try {
			url = new String(into.toByteArray(), IOHandler.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		
		RasterPlaylistBinding data = new RasterPlaylistBinding(url);
		
		return data;
	}
	
}
