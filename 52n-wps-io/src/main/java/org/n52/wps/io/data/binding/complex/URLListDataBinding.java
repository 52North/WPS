package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;

import xint.esa.ese.wps.format.urlList.URLListDocument;



public class URLListDataBinding implements IComplexData{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected transient URLListDocument urlDom;	
	
	public URLListDataBinding(URLListDocument payload) {
		this.urlDom = payload;
	}

	public Class<URLListDocument> getSupportedClass() {
		return URLListDocument.class;
	}

	public URLListDocument getPayload() {
			return urlDom;
	}


	
	

}
