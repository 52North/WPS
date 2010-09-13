package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.IOException;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IComplexData;

public class GeotiffBinding implements IComplexData{

	protected File geotiff;
	protected String mimeType;
	
	public GeotiffBinding(File geotiff){
		this.geotiff = geotiff;
		mimeType = "image/tiff";
	}
	
	@Override
	public Object getPayload() {
		return geotiff;
	}

	@Override
	public Class getSupportedClass() {
		return File.class;
	}

	public String getMimeType() {
		return mimeType;
	}
}
