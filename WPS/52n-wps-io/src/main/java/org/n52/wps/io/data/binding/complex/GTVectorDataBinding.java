package org.n52.wps.io.data.binding.complex;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IData;

public class GTVectorDataBinding implements IData{
	private FeatureCollection featureCollection;  
		
	
	
	
	public GTVectorDataBinding(FeatureCollection payload) {
		this.featureCollection = payload;
	}

	public Class<FeatureCollection> getSupportedClass() {
		return FeatureCollection.class;
	}

	public FeatureCollection getPayload() {
			return featureCollection;
	}

	

}
