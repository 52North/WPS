package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IData;

public class PlainStringBinding implements IData{
	private String payload;
	
	public PlainStringBinding(String string) {
		payload = string;
	}

	public Object getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return String.class;
	}

}
