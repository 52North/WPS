/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.database;

import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

public class PostgresDatabaseConfigurationModule implements ConfigurationModule {

    private final String isWipeEnabledKey = "wipe.enabled";

    private final String wipePeriodKey = "wipe.period";

    private final String wipeThresholdKey = "wipe.threshold";

    private static final String databaseNameKey = "databaseName";

    private static final String databasePathKey = "databasePath";

    private static final String usernameKey = "username";

    private static final String passwordKey = "password";

    private static final String saveResultsToDBKey = "saveResultsToDb";

    private static final String databaseClassKey = "databaseClass";//TODO use ClassKnowingConfigModule??

    private ConfigurationEntry<Boolean> wipeEnabledEntry = new BooleanConfigurationEntry(isWipeEnabledKey, "Database wipe enabled", "Enable database wiping based on values below",
            false, true);
    private ConfigurationEntry<String> wipePeriodEntry = new StringConfigurationEntry(wipePeriodKey, "Wipe period",
            "How often to scan database (PT1H = every hour)", false, "PT1H");
    private ConfigurationEntry<String> wipeThresholdEntry = new StringConfigurationEntry(wipeThresholdKey, "Wipe threshold",
            "Delete files older than this period (P7D = 7 days)", false, "P7D");

    private ConfigurationEntry<String> databaseNameEntry = new StringConfigurationEntry(databaseNameKey, "Database name", "Name of the database",
            true, "postgres");

    private ConfigurationEntry<String> databasePathEntry = new StringConfigurationEntry(databasePathKey, "Database path", "Path database",
            true, "//localhost:5432");

    private ConfigurationEntry<String> usernameEntry = new StringConfigurationEntry(usernameKey, "Username", "Postgres user name",
            true, "postgres");

    private ConfigurationEntry<String> passwordEntry = new StringConfigurationEntry(passwordKey, "Password", "Postgres user password",
            true, "postgres");

    private ConfigurationEntry<Boolean> saveResultsToDBEntry = new BooleanConfigurationEntry(saveResultsToDBKey, "Save results to DB", "Enable saving of results to DB (not recommended for large results!)",
            false, false);

    private ConfigurationEntry<String> databaseClassEntry = new StringConfigurationEntry(databaseClassKey, "Database class name", "Database class name",
            true, "org.n52.wps.server.database.PostgresDatabase");

    private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(wipeEnabledEntry, wipePeriodEntry, wipeThresholdEntry, databaseNameEntry, databasePathEntry, usernameEntry, passwordEntry, saveResultsToDBEntry, databaseClassEntry);

    private String databaseName;

    private String databasePath;

    private String username;

    private String password;

    private boolean saveResultsToDB;

    private String databaseClass;

    private boolean isActive = true;

    private boolean isWipeEnabled;

    private String wipePeriod;

    private String wipeThreshold;

    @Override
    public String getModuleName() {
        return "Postgres database configuration";
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public ConfigurationCategory getCategory() {
        return ConfigurationCategory.DATABASE;
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
        return null;
    }

    public boolean isWipeEnabled() {
        return isWipeEnabled;
    }

    @ConfigurationKey(key = isWipeEnabledKey)
    public void setWipeEnabled(boolean isWipeEnabled) {
        this.isWipeEnabled = isWipeEnabled;
    }

    public String getWipePeriod() {
        return wipePeriod;
    }

    @ConfigurationKey(key = wipePeriodKey)
    public void setWipePeriod(String wipePeriod) {
        this.wipePeriod = wipePeriod;
    }

    public String getWipeThreshold() {
        return wipeThreshold;
    }

    @ConfigurationKey(key = wipeThresholdKey)
    public void setWipeThreshold(String wipeThreshold) {
        this.wipeThreshold = wipeThreshold;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @ConfigurationKey(key = databaseNameKey)
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    @ConfigurationKey(key = databasePathKey)
    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public String getUsername() {
        return username;
    }

    @ConfigurationKey(key = usernameKey)
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @ConfigurationKey(key = passwordKey)
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSaveResultsToDB() {
        return saveResultsToDB;
    }

    @ConfigurationKey(key = saveResultsToDBKey)
    public void setSaveResultsToDB(boolean saveResultsToDB) {
        this.saveResultsToDB = saveResultsToDB;
    }

    public String getDatabaseClass() {
        return databaseClass;
    }

    @ConfigurationKey(key = databaseClassKey)
    public void setDatabaseClass(String databaseClass) {
        this.databaseClass = databaseClass;
    }

}
