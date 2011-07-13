package org.n52.wps.io.datahandler.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;

public class WCPSQueryParser extends AbstractXMLParser{

	@Override
	public IData parse(InputStream input, String mimeType) {
		return parseXML(input);
	}

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {PlainStringBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parseXML(String gml) {
		PlainStringBinding result = new PlainStringBinding(gml);
		return result;
	}

	@Override
	public IData parseXML(InputStream stream) {
		BufferedReader br;
		StringWriter sw;
		try {
			br = new BufferedReader(new InputStreamReader(stream,"UTF-8"));
		
		    sw=new StringWriter();
		    int k;
		    while((k=br.read())!=-1){
		    	sw.write(k);
		    }
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported Encoding");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	    PlainStringBinding result = new PlainStringBinding(sw.toString());
	    return result;
	    
	}

}
