package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralStringBinding implements IData {
	private String payload;
	
	public LiteralStringBinding(String payload){
		this.payload = payload;
	}
	
	public String getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return String.class;
	}

}
