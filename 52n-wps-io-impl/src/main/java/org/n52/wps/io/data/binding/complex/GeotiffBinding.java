package org.n52.wps.io.data.binding.complex;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.n52.wps.io.data.IComplexData;


public class GeotiffBinding implements IComplexData{

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
	
    @Override
	public void dispose(){
		FileUtils.deleteQuietly(geotiff);
	}
}
