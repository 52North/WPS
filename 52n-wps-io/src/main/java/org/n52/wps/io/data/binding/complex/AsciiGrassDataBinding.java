package org.n52.wps.io.data.binding.complex;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IComplexData;


public class AsciiGrassDataBinding implements IComplexData{

	public AsciiGrassDataBinding(GridCoverage2D grid) {
		// TODO Auto-generated constructor stub
	}

	public Object getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getSupportedClass() {
		// TODO Auto-generated method stub
		return null;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		throw new RuntimeException("Serialization of 'AsciiGrassDataBinding' data type not implemented yet.");
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		throw new RuntimeException("Deserialization of 'AsciiGrassDataBinding' data type not implemented yet.");
	}
}
