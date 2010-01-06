package org.n52.wps.io.data.binding.complex;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IData;

public class GTRasterDataBinding implements IData {
	private GridCoverage2D payload; 
	
	public GTRasterDataBinding(GridCoverage2D coverage){
		this.payload = coverage;
	}
	
	public GridCoverage2D getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return GridCoverage2D.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		throw new RuntimeException("Serialization of 'GTRasterDataBinding' data type not implemented yet.");
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		throw new RuntimeException("Deserialization of 'GTRasterDataBinding' data type not implemented yet.");
	}
}
