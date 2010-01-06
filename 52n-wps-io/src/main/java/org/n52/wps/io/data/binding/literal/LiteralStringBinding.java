package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralStringBinding implements IData {
	private transient String payload;
	
	public LiteralStringBinding(String payload){
		this.payload = payload;
	}
	
	public String getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return String.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload);
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = (String) oos.readObject();
	}
}
