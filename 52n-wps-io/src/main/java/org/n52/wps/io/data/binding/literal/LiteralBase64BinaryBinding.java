package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.IData;

public class LiteralBase64BinaryBinding implements IData {
	private byte[] binary;

	public LiteralBase64BinaryBinding(byte[] binary) {
		this.binary = binary;
	}

	public byte[] getBinary() {
		return binary;
	}

	@Override
	public byte[] getPayload() {
		return binary;
	}

	@Override
	public Class<byte[]> getSupportedClass() {
		return byte[].class;
	}
}
