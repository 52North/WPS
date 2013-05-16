package org.n52.wps.io.data.binding.complex;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IComplexData;


public class AsciiGrassDataBinding implements IComplexData{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5824397265167649044L;
	private GridCoverage2D grid;
	
	public AsciiGrassDataBinding(GridCoverage2D grid) {
		this.grid = grid;
	}

	public GridCoverage2D getPayload() {
		return this.grid;
	}

	public Class<GridCoverage2D> getSupportedClass() {
		return GridCoverage2D.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		throw new RuntimeException("Serialization of 'AsciiGrassDataBinding' data type not implemented yet.");
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		throw new RuntimeException("Deserialization of 'AsciiGrassDataBinding' data type not implemented yet.");
	}

    @Override
    public void dispose() {
        
    }
    
}
