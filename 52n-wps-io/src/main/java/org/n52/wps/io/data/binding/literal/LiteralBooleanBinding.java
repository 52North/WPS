package org.n52.wps.io.data.binding.literal;

import java.io.IOException;

public class LiteralBooleanBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8476435383089241416L;
	private transient boolean payload;
		
	public LiteralBooleanBinding(Boolean payload){
		this.payload = payload;
	}
	
	public Boolean getPayload() {
		return payload;
	}

	public Class<Boolean> getSupportedClass() {
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
