package org.n52.wps.io.data.binding.literal;

import org.n52.wps.io.data.ILiteralData;

public abstract class AbstractLiteralDataBinding implements ILiteralData {

	private String unitOfMeasurement;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088293056427203440L;

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}	
	
}
