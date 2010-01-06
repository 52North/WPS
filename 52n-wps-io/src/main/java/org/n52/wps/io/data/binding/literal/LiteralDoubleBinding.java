package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

import org.n52.wps.io.data.IData;

public class LiteralDoubleBinding implements IData {	
	private transient Double payload;
	
	public LiteralDoubleBinding(Double payload){
		this.payload = payload;
	}
	
	public Double getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return Double.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Double((String) oos.readObject());
	}
}