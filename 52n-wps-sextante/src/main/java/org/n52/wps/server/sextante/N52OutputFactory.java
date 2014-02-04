/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.sextante;

import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

public class N52OutputFactory extends GTOutputFactory{
	
	public N52OutputFactory(){
		super();
	}

	@Override
	public IVectorLayer getNewVectorLayer(String sName,
			  int iShapeType,
			  Class[] types,
			  String[] sFields,
			  IOutputChannel channel,
			  Object crs) throws UnsupportedOutputChannelException {

		if (channel instanceof FileOutputChannel){
			String sFilename = ((FileOutputChannel)channel).getFilename();
			GTVectorLayer vectorLayer;
			try {
				vectorLayer = new GTVectorLayer();
				vectorLayer.create(sName, iShapeType, types, sFields, sFilename, crs);
			} catch (Exception e) {
				throw new RuntimeException("Error while creating output layer");
			}
			return (IVectorLayer) vectorLayer;
		}
		else{
			throw new UnsupportedOutputChannelException();
		}

	}

}
