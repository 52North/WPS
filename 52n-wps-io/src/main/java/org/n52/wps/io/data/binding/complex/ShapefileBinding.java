package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.IOException;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IComplexData;

public class ShapefileBinding implements IComplexData{

	protected File shpFile;
	protected String mimeType;
	
	public ShapefileBinding(File shapeFile){
		this.shpFile = shapeFile;
		mimeType = IOHandler.MIME_TYPE_ZIPPED_SHP;
	}
	
	@Override
	public Object getPayload() {
		return shpFile;
	}

	@Override
	public Class getSupportedClass() {
		return File.class;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public File getZippedPayload(){
		String path = shpFile.getAbsolutePath();
		String baseName = path.substring(0, path.length() - ".shp".length());
		File shx = new File(baseName + ".shx");
		File dbf = new File(baseName + ".dbf");
		File prj = new File(baseName + ".prj");
		File zipped = null;
		try {
			zipped = org.n52.wps.io.IOUtils.zip(shpFile, shx, dbf, prj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return zipped;

	}
}
