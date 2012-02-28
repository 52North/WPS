package org.n52.wps.io.data.binding.bbox;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.n52.wps.io.data.IComplexData;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Coordinate;

public class GTReferenceEnvelope implements IComplexData{

	private Envelope envelope;
	
	public GTReferenceEnvelope(Object llx, Object lly, Object upx, Object upy, String crs) {
		
		try{
			double llx_double = new Double(""+llx);
		
			double lly_double = new Double(""+lly);
			double upx_double = new Double(""+upx);
			double upy_double = new Double(""+upy);
			
			Coordinate ll = new Coordinate(llx_double,lly_double);
			Coordinate ur = new Coordinate(upx_double,upy_double);
			com.vividsolutions.jts.geom.Envelope internalEnvelope = new com.vividsolutions.jts.geom.Envelope(ll,ur);
			
			if(crs==null){
				envelope = new ReferencedEnvelope(internalEnvelope, null);
			}
			else{
				envelope = new ReferencedEnvelope(internalEnvelope,CRS.decode(crs));
			}
		
		}catch(Exception e){
			throw new RuntimeException("Error while creating BoundingBox");
		}
	}

	public GTReferenceEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	@Override
	public Envelope getPayload() {
		return envelope;
	}

	@Override
	public Class getSupportedClass() {
		return ReferencedEnvelope.class;
	}
	
}
