package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralDoubleBinding extends AbstractLiteralDataBinding {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3355214481832627325L;
	private transient Double payload;
	
	public LiteralDoubleBinding(Double payload){
		this.payload = payload;
	}
	
	public Double getPayload() {
		return payload;
	}

	public Class<Double> getSupportedClass() {
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