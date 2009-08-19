package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralFloatBinding implements IData {
	private Float payload;

	public LiteralFloatBinding(Float payload) {
		this.payload = payload;
	}

	public Float getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Float.class;
	}
}
