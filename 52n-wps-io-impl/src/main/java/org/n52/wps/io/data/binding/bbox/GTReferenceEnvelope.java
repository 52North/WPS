package org.n52.wps.io.data.binding.bbox;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.n52.wps.io.data.IBBOXData;
import org.opengis.geometry.Envelope;

import com.vividsolutions.jts.geom.Coordinate;

public class GTReferenceEnvelope implements IBBOXData{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Envelope envelope;
	
	public GTReferenceEnvelope(Object llx, Object lly, Object upx, Object upy, String crs) {
		
		try {
			double llx_double = Double.parseDouble(llx.toString());
			double lly_double = Double.parseDouble(lly.toString());
			double upx_double = Double.parseDouble(upx.toString());
			double upy_double = Double.parseDouble(upy.toString());
			
			Coordinate ll = new Coordinate(llx_double,lly_double);
			Coordinate ur = new Coordinate(upx_double,upy_double);
			com.vividsolutions.jts.geom.Envelope internalEnvelope = new com.vividsolutions.jts.geom.Envelope(ll,ur);
			
			if (crs == null) {
				this.envelope = new ReferencedEnvelope(internalEnvelope, null);
			}
			else {
				this.envelope = new ReferencedEnvelope(internalEnvelope,CRS.decode(crs));
			}
		
		} catch (Exception e) {
			throw new RuntimeException("Error while creating BoundingBox");
		}
	}

	public GTReferenceEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}


	public Envelope getPayload() {
		return envelope;
	}

	public Class<?> getSupportedClass() {
		return ReferencedEnvelope.class;
	}
	
}
