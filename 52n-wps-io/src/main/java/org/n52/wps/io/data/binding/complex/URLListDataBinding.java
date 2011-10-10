package org.n52.wps.io.data.binding.complex;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;


import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.GenericFileData;

import org.n52.wps.io.datahandler.xml.SimpleGMLGenerator;
import org.n52.wps.io.datahandler.xml.SimpleGMLParser;
import org.n52.wps.io.data.IComplexData;

import xint.esa.ssegrid.wps.javaSAGAProfile.URLListDocument;

public class URLListDataBinding implements IComplexData{
	
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
