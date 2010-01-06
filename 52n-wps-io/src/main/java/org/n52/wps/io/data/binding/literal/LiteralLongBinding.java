package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralLongBinding implements IData {
	private transient Long payload;

	public LiteralLongBinding(Long payload) {
		this.payload = payload;
	}

	public Long getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Long.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Long((String) oos.readObject());
	}
}
