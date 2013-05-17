package org.n52.wps.io.data.binding.complex;

import java.io.IOException;

import org.n52.wps.io.data.IComplexData;

public class PlainStringBinding implements IComplexData{
	protected transient String payload;
	
	public PlainStringBinding(String string) {
		payload = string;
	}

	public Object getPayload() {
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
    
    @Override
	public void dispose(){
		
	}

}
