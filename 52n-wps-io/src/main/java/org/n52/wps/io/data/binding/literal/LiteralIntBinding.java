package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralIntBinding implements IData {
	private Integer payload;

	public LiteralIntBinding(Integer payload) {
		this.payload = payload;
	}

	public Integer getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Integer.class;
	}

}
