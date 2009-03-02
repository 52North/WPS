package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralBooleanBinding implements IData {
	private boolean payload;
	
	public LiteralBooleanBinding(boolean payload){
		this.payload = payload;
	}
	
	public Boolean getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return Boolean.class;
	}

}
