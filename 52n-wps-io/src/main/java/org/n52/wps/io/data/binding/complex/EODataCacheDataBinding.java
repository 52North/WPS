package org.n52.wps.io.data.binding.complex;

import org.n52.wps.io.data.IComplexData;

import xint.esa.ese.wps.format.eoDataCache.EODataCacheDocument;
import xint.esa.ese.wps.format.urlList.URLListDocument;



public class EODataCacheDataBinding implements IComplexData{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected transient EODataCacheDocument urlDom;	
	
	public EODataCacheDataBinding(EODataCacheDocument payload) {
		this.urlDom = payload;
	}

	public Class<EODataCacheDocument> getSupportedClass() {
		return EODataCacheDocument.class;
	}

	public EODataCacheDocument getPayload() {
			return urlDom;
	}


	
	

}
