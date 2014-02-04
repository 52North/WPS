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
package org.n52.wps.server.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.server.ExceptionReport;
import org.opengis.geometry.Envelope;

/*
 * @author foerster
 *
 */
public class RawData extends ResponseData {

	static Logger LOGGER = LoggerFactory.getLogger(RawData.class);
	/**
	 * @param obj
	 * @param id
	 * @param schema
	 * @param encoding
	 * @param mimeType
	 */
	public RawData(IData obj, String id, String schema, String encoding, String mimeType, String algorithmIdentifier, ProcessDescriptionType description) throws ExceptionReport{
		super(obj, id, schema, encoding, mimeType, algorithmIdentifier, description);
		if(obj instanceof IComplexData){
			prepareGenerator();
		}
		
	}
	
	public InputStream getAsStream() throws ExceptionReport {
		try {
			if(obj instanceof ILiteralData){
				String result = ""+obj.getPayload();
				 InputStream is = new ByteArrayInputStream(result.getBytes());
				 return is;
			}
			if(obj instanceof IBBOXData){
				Envelope result = (Envelope) obj.getPayload();
				String resultString  = "";
				resultString = resultString + "<wps:BoundingBoxData xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" ";
				if(result.getCoordinateReferenceSystem()!=null && result.getCoordinateReferenceSystem().getIdentifiers().size()>0){
					String crs = result.getCoordinateReferenceSystem().getIdentifiers().iterator().next().toString();
					resultString = resultString + "crs=\""+crs+"\"";
					resultString = resultString + " dimensions=\""+result.getDimension()+"\"";					
				}
				resultString = resultString + ">";
				double[] lowerCorner = result.getLowerCorner().getCoordinate();
				double[] upperCorner = result.getUpperCorner().getCoordinate();
				resultString = resultString +"<ows:LowerCorner xmlns:ows=\"http://www.opengis.net/ows/1.1\">"+lowerCorner[0]+" "+lowerCorner[1]+"</ows:LowerCorner>";
				resultString = resultString +"<ows:UpperCorner xmlns:ows=\"http://www.opengis.net/ows/1.1\">"+upperCorner[0]+" "+upperCorner[1]+"</ows:UpperCorner>";
				resultString = resultString+ "</wps:BoundingBoxData>";
				InputStream is = new ByteArrayInputStream(resultString.getBytes());
				return is;
			}
			//complexdata
			if(encoding == null || encoding == "" || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
				return generator.generateStream(obj, mimeType, schema);
			}
			else if(encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
				return generator.generateBase64Stream(obj, mimeType, schema);
				
			}
		} catch (IOException e) {
			throw new ExceptionReport("Error while generating Complex Data out of the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		throw new ExceptionReport("Could not determine encoding. Use default (=not set) or base64", ExceptionReport.NO_APPLICABLE_CODE);
	}

	
}
