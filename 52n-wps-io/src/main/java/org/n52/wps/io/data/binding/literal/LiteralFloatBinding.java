package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralFloatBinding implements IData {
	private transient Float payload;

	public LiteralFloatBinding(Float payload) {
		this.payload = payload;
	}

	public Float getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Float.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Float((String) oos.readObject());
	}
}
