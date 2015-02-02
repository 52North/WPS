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
package de.tudresden.gis.fusion.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudresden.gis.fusion.data.IData;
import de.tudresden.gis.fusion.data.IFeatureRelation;
import de.tudresden.gis.fusion.data.IFeatureRelationCollection;
import de.tudresden.gis.fusion.data.binding.IFeatureRelationBinding;
import de.tudresden.gis.fusion.data.geotools.GTFeatureCollection;
import de.tudresden.gis.fusion.data.rdf.IRI;
import de.tudresden.gis.fusion.data.simple.DecimalLiteral;

@Algorithm(abstrakt="Determines distance relation between input features", version="1.0")
public class GeometryDistance extends AbstractAnnotatedAlgorithm {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeometryDistance.class);
	
	//input identifier
	private final String IN_REFERENCE = "IN_REFERENCE";
	private final String IN_TARGET = "IN_TARGET";
	private final String IN_THRESHOLD = "IN_THRESHOLD";
	
	//input data
	private SimpleFeatureCollection inReference;
	private SimpleFeatureCollection inTarget;
	private SimpleFeatureCollection outTarget;
	private double inBuffer;
	
	//output identifier
	private final String OUT_RELATIONS = "OUT_RELATIONS";
	private final String OUT_TARGET = "OUT_TARGET";
	
	//output data
	private IFeatureRelationCollection relations;
	private final String TARGET_ATT = "relation";
	private final String TARGET_NO_RELATION = "no_relation";

	//constructor
    public GeometryDistance() {
        super();
    }

    @ComplexDataInput(identifier=IN_REFERENCE, title="reference features", binding=GTVectorDataBinding.class, minOccurs=1, maxOccurs=1)
    public void setReference(FeatureCollection<?,?> inReference) {
        this.inReference = (SimpleFeatureCollection) inReference;
    }
    
    @ComplexDataInput(identifier=IN_TARGET, title="target features", binding=GTVectorDataBinding.class, minOccurs=1, maxOccurs=1)
    public void setTarget(FeatureCollection<?,?> inTarget) {
        this.inTarget = (SimpleFeatureCollection) inTarget;
    }
    
    @LiteralDataInput(identifier=IN_THRESHOLD, title="threshold distance for relations" , binding=LiteralDoubleBinding.class, maxOccurs=1)
    public void setBuffer(double inBuffer) {
    	this.inBuffer = inBuffer;
    }
    
//    @ComplexDataOutput(identifier=OUT_RELATIONS, title="relations between reference and target features", binding=IFeatureRelationBinding.class)
//    public IFeatureRelationCollection getRelations() {
//        return relations;
//    }
    
    @SuppressWarnings("rawtypes")
	@ComplexDataOutput(identifier=OUT_TARGET, title="target features with relations", binding=GTVectorDataBinding.class)
    public FeatureCollection getTarget() {
        return outTarget;
    }
    
    @Execute
    public void execute() {
    	
    	LOGGER.info("Number of reference features: " + inReference.size());
    	LOGGER.info("Number of target features: " + inTarget.size());
    	LOGGER.info("Distance threshold: " + inBuffer);
    	
    	//get relations
    	Map<String,IData> input = new HashMap<String,IData>();
    	input.put(IN_REFERENCE, new GTFeatureCollection(new IRI(IN_REFERENCE), inReference));
    	input.put(IN_TARGET, new GTFeatureCollection(new IRI(IN_TARGET), inTarget));
    	input.put(IN_THRESHOLD, new DecimalLiteral(inBuffer));
		
		Map<String,IData> output = new de.tudresden.gis.fusion.operation.similarity.geometry.GeometryDistance().execute(input);
		
		relations = (IFeatureRelationCollection) output.get("OUT_RELATIONS");
		outTarget = addRelations(inTarget, relations);
		
    	LOGGER.info("Relation measurement returned " + relations.size() + " results");
    }
   
    /**
     * add feature relation to collection of features
     * @param inTarget target features
     * @param relations relations
     * @return target features with relations
     */
    private SimpleFeatureCollection addRelations(SimpleFeatureCollection inTarget, IFeatureRelationCollection relations){
    	//init feature list
    	List<SimpleFeature> outTargetList = new ArrayList<SimpleFeature>();
    	//get new feature type
    	SimpleFeatureType fType = addAttribute(inTarget.getSchema(), TARGET_ATT, String.class);
    	//iterate collection and add relation property
    	SimpleFeatureIterator iterator = inTarget.features();
    	while (iterator.hasNext()) {
    		SimpleFeature feature = iterator.next();
        	//get relation string
        	String sRelation = getRelationString(feature, relations);
    		//build new feature
    		SimpleFeature newFeature = buildFeature(feature, fType, sRelation);
    		//add feature to new collection
    		outTargetList.add(newFeature);
		}
    	//return
    	return DataUtilities.collection(outTargetList);
    }
    
    /**
     * get relation attribute string
     * @param feature input feature
     * @param relations input relations
     * @return relation string for feature
     */
    private String getRelationString(SimpleFeature feature, IFeatureRelationCollection relations){
    	//get feature id
    	String sID = feature.getID();
    	//get relations with corresponding target id
    	List<IFeatureRelation> featureRelations = new ArrayList<IFeatureRelation>();
    	for(IFeatureRelation relation : relations){
    		if(relation.getTarget().getIdentifier().asString().endsWith(sID))
    			featureRelations.add(relation);
    	}
    	//identify suitable relation (if array > 1)
    	if(featureRelations.size() == 0)
    		return TARGET_NO_RELATION;
    	else {
    		StringBuffer sRelation = new StringBuffer();
    		for(IFeatureRelation relation : featureRelations){
    			sRelation.append(getRelationString(relation) + "&&");
    		}
    		return sRelation.substring(0, sRelation.length()-2);
    	}
    }
    
    /**
     * build new feature
     * @param feature input feature
     * @param fType new feature type
     * @param relation input relation
     * @return new feature with attached relation attribute
     */
    private SimpleFeature buildFeature(SimpleFeature feature, SimpleFeatureType fType, String sRelation){
    	//get feature builder
    	SimpleFeatureBuilder fBuilder= new SimpleFeatureBuilder(fType);
    	//copy feature
    	fBuilder.init(feature);
    	//add relation
    	fBuilder.set(TARGET_ATT, sRelation);
		//return new feature
		return fBuilder.buildFeature(feature.getID());
    }
    
    /**
     * get string representation of relation
     * @param relation input relation
     * @return string representation of relation
     */
    private String getRelationString(IFeatureRelation relation){
    	if(relation == null)
    		return TARGET_NO_RELATION;
    	return relation.getReference().getIdentifier().asString() + ";" +
    			relation.getMeasurements().iterator().next().getRelationType().getIdentifier().asString() + ";" +
    			relation.getMeasurements().iterator().next().getMeasurementValue().getIdentifier();
    }
    
    /**
     * build new feature type with additional attribute
     * @param inputType input feature type
     * @param newAtt attribute to be added
     * @return new feature type
     */
    private SimpleFeatureType addAttribute(SimpleFeatureType inputType, String name, Class<?> clazz){
    	//get ft builder
		SimpleFeatureTypeBuilder ftBuilder= new SimpleFeatureTypeBuilder();
		//copy input type
		ftBuilder.init(inputType);
		//set name and default geometry name
		ftBuilder.setName(inputType.getName());
		ftBuilder.setDefaultGeometry(inputType.getGeometryDescriptor().getLocalName());
		//add new attribute property
		ftBuilder.add(name, clazz);
		//return
		return ftBuilder.buildFeatureType();
    }

}
