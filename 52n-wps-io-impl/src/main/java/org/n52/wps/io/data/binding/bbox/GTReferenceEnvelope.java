/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
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
