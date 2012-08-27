package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.RasterPlaylistBinding;

public class RasterPlaylistGenerator extends AbstractGenerator {

	public RasterPlaylistGenerator(){
		super();
		supportedIDataTypes.add(RasterPlaylistBinding.class);	
	}
	
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		String str;
		if (data == null){
			str = "";
		}
		else{
			str = (String) ((RasterPlaylistBinding)data).getPayload();			
		}
		InputStream is = new ByteArrayInputStream(str.getBytes());
	 
		return is;
	}

}
