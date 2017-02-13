package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;

import xint.esa.ese.wps.format.dataList.DataListDocument;
import xint.esa.ese.wps.format.urlList.URLListDocument;



public class DataListDataBinding implements IComplexData{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected transient DataListDocument urlDom;	
	
	public DataListDataBinding(DataListDocument payload) {
		this.urlDom = payload;
	}

	public Class<DataListDocument> getSupportedClass() {
		return DataListDocument.class;
	}

	public DataListDocument getPayload() {
			return urlDom;
	}


	
	

}
