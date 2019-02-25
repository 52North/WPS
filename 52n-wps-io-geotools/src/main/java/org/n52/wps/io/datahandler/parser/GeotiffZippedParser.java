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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public class GeotiffZippedParser extends AbstractParser {

    private static Logger LOGGER = LoggerFactory.getLogger(GeotiffZippedParser.class);

    public GeotiffZippedParser() {
        super();
        supportedIDataTypes.add(GTRasterDataBinding.class);
    }

    @Override
    public GTRasterDataBinding parse(InputStream input,
            String mimeType,
            String schema) {
        // unzip
        File zippedFile;
        try {
            zippedFile = IOUtils.writeStreamToFile(input, "zip");
            finalizeFiles.add(zippedFile); // mark for final delete

            List<File> files = IOUtils.unzipAll(zippedFile);
            finalizeFiles.addAll(files); // mark for final delete

            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".tiff")) {
                    return parseTiff(file);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Exception while trying to unzip tiff.", e);
        }
        throw new RuntimeException("Could not parse zipped geotiff.");
    }

    private GTRasterDataBinding parseTiff(File file) {
        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        GeoTiffReader reader;
        try {
            reader = new GeoTiffReader(file, hints);
            GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
            return new GTRasterDataBinding(coverage);
        } catch (Exception e) {
            LOGGER.error("Exception while trying to create GTRasterDataBinding out of tiff.", e);
            throw new RuntimeException(e);
        }
    }

}
