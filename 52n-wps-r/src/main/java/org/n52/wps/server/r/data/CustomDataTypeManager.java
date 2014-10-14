/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.R_Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomDataTypeManager {

    private static final String COMMENT_CHARACTER = "#";

    private static Logger LOGGER = LoggerFactory.getLogger(CustomDataTypeManager.class);

    private File configFile;

    private static final String HINT_FILE = "file";

    @Autowired
    private R_Config config;

    @Autowired
    private RDataTypeRegistry datatypeRegistry;

    public CustomDataTypeManager() {
        LOGGER.info("NEW {}", this);
    }

    /**
     * Called by RPropertyChangeManager and eventually after config file was changed
     */
    public void update() {
        try {
            readConfig();
        }
        catch (IOException e) {
            LOGGER.error("Invalid r config file. Costum R data types cannot be registered.", e);
            datatypeRegistry.clearCustomDataTypes();
        }
        catch (ExceptionReport e) {
            LOGGER.error("Failed to retrieve r config file. Costum R data types cannot be registered.", e);
            datatypeRegistry.clearCustomDataTypes();
        }
    }

    private void readConfig() throws IOException, ExceptionReport {
        String file = config.getConfigVariableFullPath(RWPSConfigVariables.R_DATATYPE_CONFIG);
        this.configFile = new File(file);
        if (getConfigFile() == null) {
            LOGGER.error("Config file not availailable at '{}'. Costum R data types cannot be registered.", file);
            return;
        }

        datatypeRegistry.clearCustomDataTypes();

        FileReader fr = new FileReader(getConfigFile());
        BufferedReader reader = new BufferedReader(fr);

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(COMMENT_CHARACTER))
                continue;

            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            if (tokenizer.countTokens() == 3) {

                String key = tokenizer.nextToken().trim();
                String mimetype = tokenizer.nextToken().trim();
                String hint = tokenizer.nextToken().trim();
                addNewDataType(key, mimetype, hint);
            }

        }
        reader.close();
        fr.close();
    }

    private void addNewDataType(String key, String mimetype, String hint) {
        LOGGER.debug("Adding new data type with key '{}', mimetype '{}', and hint '{}'", key, mimetype, hint);

        CustomDataType type = new CustomDataType();
        type.setKey(key);
        type.setMimeType(mimetype);
        if (hint.equalsIgnoreCase(HINT_FILE)) {
            // type.setEncoding("base64");
            type.setComplex(true);
        }

        datatypeRegistry.register(type);
    }

    public File getConfigFile() {
        return this.configFile;
    }

}
