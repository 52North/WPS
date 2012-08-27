package org.n52.wps.io.data.binding.complex;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class RasterPlaylistBinding implements IComplexData {

	protected String playlistItem;
	protected String mimeType;
	
	public RasterPlaylistBinding(String item){
		this.playlistItem = item;
		mimeType = "application/x-ogc-playlist";
	}
	
	@Override
	public Object getPayload() {
		return this.playlistItem;
	}

	@Override
	public Class<File> getSupportedClass() {
		return File.class;
	}

	public String getMimeType() {
		return mimeType;
	}

}
