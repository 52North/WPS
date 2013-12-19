package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralIntBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2565311479077929283L;
	private transient Integer payload;

	public LiteralIntBinding(Integer payload) {
		this.payload = payload;
	}

	public Integer getPayload() {
		return payload;
	}

	public Class<Integer> getSupportedClass() {
		return Integer.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Integer((String) oos.readObject());
	}

}
