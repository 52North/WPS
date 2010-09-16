package org.n52.wps.io.data.binding.complex;

import java.io.File;

import org.n52.wps.io.data.IComplexRasterData;

public class GeotiffBinding implements IComplexRasterData{

	protected File geotiff;
	protected String mimeType;
	
	public GeotiffBinding(File geotiff){
		this.geotiff = geotiff;
		mimeType = "image/tiff";
	}
	
	@Override
	public File getPayload() {
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
