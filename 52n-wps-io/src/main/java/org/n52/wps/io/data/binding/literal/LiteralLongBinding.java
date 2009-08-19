package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralLongBinding implements IData {
	private Long payload;

	public LiteralLongBinding(Long payload) {
		this.payload = payload;
	}

	public Long getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Long.class;
	}
}
