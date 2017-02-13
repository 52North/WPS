package org.n52.wps.io.data;

import net.opengis.ows.x11.BoundingBoxType;

/** Simplified class for demo purpose 
 * */
public class SimpleBBoxBinding implements IData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BoundingBoxType bbox;
	
	public SimpleBBoxBinding(BoundingBoxType bbox) {
		this.bbox = bbox;
	}
	
	@Override
	public Object getPayload() {
		// TODO Auto-generated method stub
		return this.bbox;
	}

	@Override
	public Class getSupportedClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public BoundingBoxType getBbox() {
		return bbox;
	}

	public void setBbox(BoundingBoxType bbox) {
		this.bbox = bbox;
	}

}
