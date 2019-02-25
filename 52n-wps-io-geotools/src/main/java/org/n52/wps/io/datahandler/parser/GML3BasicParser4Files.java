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
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.wps.io.data.GenericFileDataWithGT;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;

/**
 * This parser handles XML files compliant to GML3.
 * 
 * @author schaeffer
 *
 */
public class GML3BasicParser4Files extends AbstractParser {

    private static Logger LOGGER = LoggerFactory.getLogger(GML3BasicParser4Files.class);

    public GML3BasicParser4Files() {
        super();
        supportedIDataTypes.add(GenericFileDataWithGTBinding.class);
    }

    public GenericFileDataWithGTBinding parse(InputStream stream,
            String mimeType,
            String schema) {

        FileOutputStream fos = null;
        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".gml3");
            finalizeFiles.add(tempFile); // mark for final delete
            fos = new FileOutputStream(tempFile);
            int i = stream.read();
            while (i != -1) {
                fos.write(i);
                i = stream.read();
            }
            fos.flush();
            fos.close();
            GenericFileDataWithGTBinding data = parseXML(tempFile);

            return data;
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e1) {
                }
            }
            throw new IllegalArgumentException("Error while creating tempFile", e);
        }
    }

    private GenericFileDataWithGTBinding parseXML(File file) {

        SimpleFeatureCollection fc = new GML3BasicParser().parseFeatureCollection(file);

        GenericFileDataWithGTBinding data = null;
        try {
            data = new GenericFileDataWithGTBinding(new GenericFileDataWithGT(fc));
        } catch (IOException e) {
            LOGGER.error("Exception while creating GenericFileData from FeatureCollection", e);
        }

        return data;
    }

}
