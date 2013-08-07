package org.n52.wps.io.data;

import java.io.Serializable;

public interface IData extends Serializable {
	public Object getPayload();
	
	/*
	 * Inheriting classes shall indicate which class they support.
	 * For special cases like Grass, it is recommended to
	 * Create your own wrapper class, for e.g. a File in the filesystem.
	*/
	public abstract Class<?> getSupportedClass();
}
