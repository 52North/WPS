package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralFloatBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = 617194666437653721L;
	private transient Float payload;

	public LiteralFloatBinding(Float payload) {
		this.payload = payload;
	}

	public Float getPayload() {
		return payload;
	}

	public Class<Float> getSupportedClass() {
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
