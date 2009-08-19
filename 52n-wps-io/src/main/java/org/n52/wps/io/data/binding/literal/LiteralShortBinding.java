package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralShortBinding implements IData {
	private Short payload;

	public LiteralShortBinding(Short payload) {
		this.payload = payload;
	}

	public Short getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Short.class;
	}
}
