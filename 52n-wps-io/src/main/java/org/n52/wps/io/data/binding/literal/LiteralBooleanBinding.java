package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralBooleanBinding implements IData {
	private transient boolean payload;
	
	public LiteralBooleanBinding(boolean payload){
		this.payload = payload;
	}
	
	public Boolean getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return Boolean.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(Boolean.toString(payload));
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = Boolean.parseBoolean((String) oos.readObject());
	}

}
