/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.R_Config;

public class CustomDataTypeManager {

    private static final String COMMENT_CHARACTER = "#";
    private static Logger LOGGER = LoggerFactory.getLogger(CustomDataTypeManager.class);
    private File configFile;
    private static CustomDataTypeManager instance;

    private static final String HINT_FILE = "file";

    private CustomDataTypeManager() {

    }

    // Call by RPropertyChangeManager and eventually after config file was changed
    public void update() {
        try {
            readConfig();
        }
        catch (IOException e) {
            LOGGER.error("Invalid r config file. Costum R data types cannot be registered.", e);
            RDataTypeRegistry.getInstance().clearCustomDataTypes();
        }
        catch (ExceptionReport e) {
            LOGGER.error("Failed to retrieve r config file. Costum R data types cannot be registered.", e);
            RDataTypeRegistry.getInstance().clearCustomDataTypes();
        }
    }

    private void readConfig() throws IOException, ExceptionReport {
        this.configFile = new File(R_Config.getInstance().getConfigVariableFullPath(RWPSConfigVariables.R_DATATYPE_CONFIG));
        if (getConfigFile() == null) {
            LOGGER.error("Config file not availailable. Costum R data types cannot be registered.");
            return;
        }
        RDataTypeRegistry.getInstance().clearCustomDataTypes();

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

    // TODO: add schema, default value;
    private static void addNewDataType(String key, String mimetype, String hint) {
        CustomDataType type = new CustomDataType();
        type.setKey(key);
        type.setProcessKey(mimetype);
        if (hint.equalsIgnoreCase(HINT_FILE)) {
            type.setEncoding("base64");
            type.setComplex(true);
        }

        RDataTypeRegistry.getInstance().register(type);

    }

    public File getConfigFile() {
        return this.configFile;
    }

    public static CustomDataTypeManager getInstance() {
        if (instance == null)
            instance = new CustomDataTypeManager();
        return instance;
    }

    // TODO update methode

}
