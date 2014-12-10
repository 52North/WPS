/**
 * ﻿Copyright (C) 2014 - 2014 52°North Initiative for Geospatial Open Source
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
package de.tudresden.gis.fusion.data.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

import de.tudresden.gis.fusion.data.IDataResource;
import de.tudresden.gis.fusion.data.IFeatureRelationCollection;
import de.tudresden.gis.fusion.data.binding.IFeatureRelationBinding;
import de.tudresden.gis.fusion.data.simple.StringLiteral;
import de.tudresden.gis.fusion.operation.provision.RDFTurtleGenerator;

public class RelationGenerator extends AbstractGenerator {
		
	public RelationGenerator(){
		super();
		supportedIDataTypes.add(IFeatureRelationBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
		if(data instanceof IFeatureRelationBinding){
			//get relation
			IFeatureRelationCollection relations = ((IFeatureRelationBinding) data).getPayload();
			//write relations to file
			File tempFile = writeToFile(relations);
			//init temp file
			finalizeFiles.add(tempFile); // mark for final delete
			//get and return InputStream
			InputStream is = new FileInputStream(tempFile);			
			return is;
		}
		
		return null;
	}

	/**
	 * write relations to RDF encoded as Turtles
	 * @param relations input relations
	 * @param file output RDF file
	 * @throws FileNotFoundException  
	 * @throws MalformedURLException 
	 */
	private File writeToFile(IFeatureRelationCollection relations) throws FileNotFoundException, MalformedURLException {
		
		Map<String,de.tudresden.gis.fusion.data.IData> input = new HashMap<String,de.tudresden.gis.fusion.data.IData>();
		RDFTurtleGenerator generator = new RDFTurtleGenerator();
		input.put("IN_DATA", relations);
		input.put("URI_PREFIXES", new StringLiteral(""
				+ "http://tu-dresden.de/uw/geo/gis/fusion#;fusion;"
				+ "http://www.w3.org/1999/02/22-rdf-syntax-ns#;rdf;"
				+ "http://www.w3.org/2001/XMLSchema#;xsd;"
				+ "http://tu-dresden.de/uw/geo/gis/fusion/process/demo#;demo;"
				+ "http://tu-dresden.de/uw/geo/gis/fusion/confidence/statisticalConfidence#;statisticalConfidence;"
				+ "http://tu-dresden.de/uw/geo/gis/fusion/similarity/spatial#;spatialRelation;"
				+ "http://tu-dresden.de/uw/geo/gis/fusion/similarity/topology#;topologyRelation;"
				+ "http://tu-dresden.de/uw/geo/gis/fusion/similarity/string#;stringRelation"));
		Map<String,de.tudresden.gis.fusion.data.IData> output = generator.execute(input);	
		IDataResource file = (IDataResource) output.get("OUT_FILE");
		
		return new File(file.getIdentifier().asURI().toURL().getFile());
		
	}
	
}
