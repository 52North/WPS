package org.n52.wps.server.r.syntax;

import org.n52.wps.io.data.IData;

public interface RTypeDefinition {

	/**
	 * @return Unique type-expression used in the WPS4R annotation (type-Attribute)
	 */
	public abstract String getKey();

	/**
	 * 
	 * @return Type-expression used in the processDescription
	 */
	public abstract String getProcessKey();

	public abstract boolean isComplex();
	//TODO to be added:
	
	//public abstract boolean isLitearal();
	//public abstract boolean isLitearal();
	
	/**
	 * 
	 * @return (default) encoding or null if not applicable
	 */
	public abstract String getEncoding();

	/**
	 * 
	 * @return (default) Schema
	 */
	public abstract String getSchema();

	/**
	 * Refers to the Databinding in use
	 * @return IData class
	 */
	public abstract Class<? extends IData> getIDataClass();

}