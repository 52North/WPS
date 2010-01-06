package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralShortBinding implements IData {
	private transient Short payload;

	public LiteralShortBinding(Short payload) {
		this.payload = payload;
	}

	public Short getPayload() {
		return payload;
	}

	public Class<?> getSupportedClass() {
		return Short.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Short((String) oos.readObject());
	}
}
