package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralShortBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2767793107821138509L;
	private transient Short payload;

	public LiteralShortBinding(Short payload) {
		this.payload = payload;
	}

	public Short getPayload() {
		return payload;
	}

	public Class<Short> getSupportedClass() {
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
