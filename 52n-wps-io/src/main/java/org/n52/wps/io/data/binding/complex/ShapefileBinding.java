package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IComplexData;


public class ShapefileBinding implements IComplexData{
	private static Logger LOGGER = Logger.getLogger(ShapefileBinding.class);
	
	
	protected File shpFile;
	protected String mimeType;
	
	public ShapefileBinding(File shapeFile){
		this.shpFile = shapeFile;
		mimeType = IOHandler.MIME_TYPE_ZIPPED_SHP;
	}
	
	@Override
	public File getPayload() {
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
	
	public GTVectorDataBinding getPayloadAsGTVectorDataBinding(){
		String dirName = "tmp" + System.currentTimeMillis();
		File tempDir = null;
		
		try {
			DataStore store = new ShapefileDataStore(shpFile.toURI().toURL());
			FeatureCollection features = store.getFeatureSource(store.getTypeNames()[0]).getFeatures();
			System.gc();
			tempDir.delete();
			return new GTVectorDataBinding(features);
		} catch (MalformedURLException e) {
			LOGGER.error("Something went wrong while creating data store.");
			e.printStackTrace();
			throw new RuntimeException("Something went wrong while creating data store.", e);
		} catch (IOException e) {
			LOGGER.error("Something went wrong while converting shapefile to FeatureCollection");
			e.printStackTrace();
			throw new RuntimeException("Something went wrong while converting shapefile to FeatureCollection", e);
		}
	}
	
	protected void finalize(){
		try{
			shpFile.delete();
		}catch(Exception e){
			
		}
	}
	
	
	
}
