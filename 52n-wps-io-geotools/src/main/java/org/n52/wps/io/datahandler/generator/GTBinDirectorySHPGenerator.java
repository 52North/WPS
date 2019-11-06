/*
 * Copyright (C) 2007 - 2018 52°North Initiative for Geospatial Open Source
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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.AttributeImpl;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTBinDirectorySHPGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(GTBinDirectorySHPGenerator.class);

    private Map<String, AttributeDescriptor> attributeNameDescriptorMap = new HashMap<>();

    private Map<String, String> attributeNameMap = new HashMap<>();

    public File writeFeatureCollectionToDirectory(IData data)
            throws IOException {
        return writeFeatureCollectionToDirectory(data, null);
    }

    public File writeFeatureCollectionToDirectory(IData data, File parent) throws IOException {
        GTVectorDataBinding binding = (GTVectorDataBinding) data;
        SimpleFeatureCollection originalCollection = (SimpleFeatureCollection) binding.getPayload();

        if(checkIfAttributeNameIsLongerThan10Chars(originalCollection.getSchema())){
            originalCollection = GTHelper.createCorrectFeatureCollection(originalCollection);
        }

        return createShapefileDirectory(originalCollection, parent);
    }

    private boolean checkIfAttributeNameIsLongerThan10Chars(SimpleFeatureType simpleFeatureType){

        List<AttributeDescriptor> attributeDescriptors = simpleFeatureType.getAttributeDescriptors();

        for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
            String attributeName = attributeDescriptor.getName().getLocalPart();
            if(attributeName.length() > 10){
                return true;
            }

        }
        return false;
    }

    public Collection<Property> truncatePropertyNames(Collection<Property> properties){

        Collection<Property> newProperties = new ArrayList<>();

        for (Property property : properties) {

            Property newProperty = property;

            String propertyName = property.getName().getLocalPart();

            if(propertyName.length() > 10){
                //truncate
//                String newPropertyName = attributeNameMap.get(propertyName);

                newProperty = new AttributeImpl(property.getValue(), attributeNameDescriptorMap.get(propertyName), null);
            }

            newProperties.add(newProperty);
        }

        return newProperties;

    }

    public SimpleFeatureType truncateAttributeNames(SimpleFeatureType simpleFeatureType){

        SimpleFeatureType newType = simpleFeatureType;

        List<AttributeDescriptor> attributeDescriptors = simpleFeatureType.getAttributeDescriptors();

        List<AttributeDescriptor> newAttributeDescriptors = new ArrayList<>();

        for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
            String attributeName = attributeDescriptor.getName().getLocalPart();
            AttributeDescriptor newAttributeDescriptor = attributeDescriptor;
            if(attributeName.length() > 10){
                //truncate
                String newAttributeName = attributeName.substring(0,10);

                LOGGER.info(String.format("Attribute name: %s  was longer than 10 chars, truncating to %s", attributeName, newAttributeName));

                checkNames(attributeName, newAttributeName, attributeNameMap);

                attributeNameMap.put(attributeName, newAttributeName);

                //create new attribute
                Name newName = new NameImpl(attributeDescriptor.getName().getNamespaceURI(), newAttributeName);

                AttributeType attributeType = attributeDescriptor.getType();

                AttributeType newAttributeType = new AttributeTypeImpl(newName, attributeType.getBinding(), attributeType.isIdentified(), attributeType.isAbstract(), attributeType.getRestrictions(), attributeType.getSuper(), attributeType.getDescription());

                newAttributeDescriptor = new AttributeDescriptorImpl(newAttributeType, newName, attributeDescriptor.getMinOccurs(), attributeDescriptor.getMaxOccurs(), attributeDescriptor.isNillable(), attributeDescriptor.getDefaultValue());

                attributeNameDescriptorMap.put(attributeName, newAttributeDescriptor);
            }
            newAttributeDescriptors.add(newAttributeDescriptor);
        }

        newType = new SimpleFeatureTypeImpl(simpleFeatureType.getName(), newAttributeDescriptors, simpleFeatureType.getGeometryDescriptor(), simpleFeatureType.isAbstract(), simpleFeatureType.getRestrictions(), simpleFeatureType.getSuper(), simpleFeatureType.getDescription());

        return newType;
    }

    public String checkNames(String originalName, String truncatedName, Map<String, String> attributeNameMap){

        //check if truncated attribute name already exists
        //it can happen that two truncated attribute name are equal
        //e.g. population_min and population_max, which would be truncated both to population
        if(attributeNameMap.containsValue(truncatedName)){

            LOGGER.info("Found duplicate truncated name: " + truncatedName);
            // create new truncatedName, substring 0,9 and add increasing number
            truncatedName = createNewTruncatedName(truncatedName);

            truncatedName = checkNames(originalName, truncatedName, attributeNameMap);
        }

        return truncatedName;

    }

    private String createNewTruncatedName(String truncatedName){

        //we'll go for 1 digit
        String shortenedTruncatedName = truncatedName.substring(0,9);
        String possibleNumber = truncatedName.substring(9);

        if(!StringUtils.isNumeric(possibleNumber)){
            truncatedName = shortenedTruncatedName + 1;
        }else{
            truncatedName = shortenedTruncatedName + (Integer.parseInt(possibleNumber) + 1);
        }

        return truncatedName;
    }

    /**
     * Transforms the given {@link FeatureCollection} into a zipped SHP file
     * (.shp, .shx, .dbf, .prj) and returs its Base64 encoding
     *
     * @param collection
     *            the collection to transform
     * @return the zipped shapefile
     * @throws IOException
     *             If an error occurs while creating the SHP file or encoding
     *             the shapefile
     * @throws IllegalAttributeException
     *             If an error occurs while writing the features into the the
     *             shapefile
     */
    private File createShapefileDirectory(SimpleFeatureCollection collection, File parent)
            throws IOException, IllegalAttributeException {
        if (parent == null) {
            File tempBaseFile = File.createTempFile("resolveDir", ".tmp");
            tempBaseFile.deleteOnExit();
            parent = tempBaseFile.getParentFile();
        }

        if (parent == null || !parent.isDirectory()) {
            throw new IllegalStateException("Could not find temporary file directory.");
        }

        File shpBaseDirectory = new File(parent, UUID.randomUUID().toString());

        if (!shpBaseDirectory.mkdir()) {
            throw new IllegalStateException("Could not create temporary shp directory.");
        }

        File tempSHPfile = GTHelper.createShapeFile(collection, shpBaseDirectory);

        // Zip the shapefile
        String path = tempSHPfile.getAbsolutePath();
        String baseName = path.substring(0, path.length() - ".shp".length());
        File shx = new File(baseName + ".shx");
        File dbf = new File(baseName + ".dbf");
        File prj = new File(baseName + ".prj");

        // mark created files for delete
        tempSHPfile.deleteOnExit();
        shx.deleteOnExit();
        dbf.deleteOnExit();
        prj.deleteOnExit();
        shpBaseDirectory.deleteOnExit();

        return shpBaseDirectory;
    }

}
