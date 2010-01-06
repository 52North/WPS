package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.apache.axis.utils.ByteArray;
import org.n52.wps.io.data.IData;

public class LiteralBase64BinaryBinding implements IData {
	private transient byte[] binary;

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
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(new String(binary));
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		binary = ((String) oos.readObject()).getBytes();
	}
}
