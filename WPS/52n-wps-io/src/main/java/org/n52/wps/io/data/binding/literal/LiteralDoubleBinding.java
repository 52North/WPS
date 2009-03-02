package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralDoubleBinding implements IData {
	private Double payload;
	
	public LiteralDoubleBinding(Double payload){
		this.payload = payload;
	}
	
	public Double getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return Double.class;
	}

}
