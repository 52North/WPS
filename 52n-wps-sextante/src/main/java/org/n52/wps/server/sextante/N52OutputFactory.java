/**
 * ﻿Copyright (C) 2008
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
