/*
 * Copyright (C) 2007 - 2017 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io.modules.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.io.datahandler.generator.GeoserverWFSGenerator;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

public class GeoserverWFSGeneratorCM extends ClassKnowingModule{

    private boolean isActive = true;

public static final String geoserverUsernameKey = "Geoserver_username";
public static final String geoserverPasswordKey = "Geoserver_password";
public static final String geoserverHostKey = "Geoserver_host";
public static final String geoserverPortKey = "Geoserver_port";

private ConfigurationEntry<String> geoserverUsernameEntry = new StringConfigurationEntry(geoserverUsernameKey, "GeoServer user name", "GeoServer user name",
true, "admin");
private ConfigurationEntry<String> geoserverPasswordEntry = new StringConfigurationEntry(geoserverPasswordKey, "GeoServer password", "GeoServer password",
true, "geoserver");
private ConfigurationEntry<String> geoserverHostEntry = new StringConfigurationEntry(geoserverHostKey, "GeoServer hostname", "GeoServer hostname",
true, "localhost");
private ConfigurationEntry<String> geoserverPortEntry = new StringConfigurationEntry(geoserverPortKey, "GeoServer port", "GeoServer port",
true, "8080");

private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(geoserverUsernameEntry, geoserverPasswordEntry, geoserverHostEntry, geoserverPortEntry);

private String geoserverUsername;
private String geoserverPassword;
private String geoserverHost;
private String geoserverPort;

    private List<FormatEntry> formatEntries;

    public GeoserverWFSGeneratorCM(){
        formatEntries = new ArrayList<>();
    }

    @Override
    public String getModuleName() {
        return "GeoserverWFSGenerator";
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public ConfigurationCategory getCategory() {
        return ConfigurationCategory.GENERATOR;
    }

    @Override
    public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
        return configurationEntries;
    }

    @Override
    public List<AlgorithmEntry> getAlgorithmEntries() {
        return null;
    }

    @Override
    public List<FormatEntry> getFormatEntries() {
        return formatEntries;
    }

    @Override
    public String getClassName() {
        return GeoserverWFSGenerator.class.getName();
}

public String getGeoserverUsername() {
return geoserverUsername;
}

@ConfigurationKey(key = geoserverUsernameKey)
public void setGeoserverUsername(String geoserverUsername) {
this.geoserverUsername = geoserverUsername;
}

public String getGeoserverPassword() {
return geoserverPassword;
}

@ConfigurationKey(key = geoserverPasswordKey)
public void setGeoserverPassword(String geoserverPassword) {
this.geoserverPassword = geoserverPassword;
}

public String getGeoserverHost() {
return geoserverHost;
}

@ConfigurationKey(key = geoserverHostKey)
public void setGeoserverHost(String geoserverHost) {
this.geoserverHost = geoserverHost;
}

public String getGeoserverPort() {
return geoserverPort;
}

@ConfigurationKey(key = geoserverPortKey)
public void setGeoserverPort(String geoserverPort) {
this.geoserverPort = geoserverPort;
    }

}
