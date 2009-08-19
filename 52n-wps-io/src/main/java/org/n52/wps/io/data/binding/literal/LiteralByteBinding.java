package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralByteBinding implements IData {
	private Byte payload;

	public LiteralByteBinding(Byte payload) {
		this.payload = payload;
	}

	public Byte getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Byte.class;
	}
}
