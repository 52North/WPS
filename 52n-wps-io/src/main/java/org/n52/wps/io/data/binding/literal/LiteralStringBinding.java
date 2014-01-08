package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralStringBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4918615178134884183L;
	private transient String payload;
	
	public LiteralStringBinding(String payload){
		this.payload = payload;
	}
	
	public String getPayload() {
		return payload;
	}

	public Class<String> getSupportedClass() {
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
