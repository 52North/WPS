/**
 * Copyright (C) 2007 - 2015 52°North Initiative for Geospatial Open Source
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class AsciiGrassGenerator extends AbstractGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(AsciiGrassGenerator.class);
    
    public AsciiGrassGenerator() {
        super();
        this.supportedIDataTypes.add(GTRasterDataBinding.class);
    }
    
    public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
        
//        // check for correct request before returning the stream
//        if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//            throw new IOException("I don't support the incoming datatype");
//        }
        
        InputStream stream = null;
        
        GridCoverage2D grid = ((GTRasterDataBinding) data).getPayload();
        String fileName = "temp" + UUID.randomUUID();
        File outputFile = File.createTempFile(fileName, ".tmp");
        outputFile.deleteOnExit();
        this.finalizeFiles.add(outputFile); // mark file for final delete
        GridCoverageWriter writer;
        try {
            writer = new ArcGridWriter(outputFile);
        
            // setting write parameters
            ParameterValueGroup params = writer.getFormat().getWriteParameters();
            params.parameter("GRASS").setValue(true);
            GeneralParameterValue[] gpv = { params.parameter("GRASS") };
            
            writer.write(grid, gpv);
            writer.dispose();
            
            stream = new FileInputStream(outputFile);
            
        } catch (DataSourceException e) {
            LOGGER.error(e.getMessage());
            throw new IOException("AsciiGRID cannot be read from source");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IOException("Illegal configuration of AsciiGRID writer");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new IOException("AsciiGrassGenerator could not create output due to an IO error");
        }
        
        return stream;
    }

}
