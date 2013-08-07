package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralByteBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6673960070211065274L;
	private transient Byte payload;

	public LiteralByteBinding(Byte payload) {
		this.payload = payload;
	}

	public Byte getPayload() {
		return payload;
	}

	public Class<Byte> getSupportedClass() {
		return Byte.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(payload.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		payload = new Byte((String) oos.readObject());
	}
}
