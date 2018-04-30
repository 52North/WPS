/*
 * Copyright (C) 2010-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.r;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component("rActualConfigurationModule")
public class RConfigurationModule extends ClassKnowingModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(RConfigurationModule.class);

    private boolean isActive = true;

    private static final String enableBatchStartKey = "R_enableBatchStart"; //RWPSConfigVariables.ENABLE_BATCH_START.toString();
    private static final String datatypeConfigKey = "R_datatypeConfig"; //RWPSConfigVariables.R_DATATYPE_CONFIG.toString();
    private static final String wdStrategyKey = "R_wdStrategy"; //RWPSConfigVariables.R_WORK_DIR_STRATEGY.toString();
    private static final String wdNameKey = "R_wdName"; //RWPSConfigVariables.R_WORK_DIR_NAME.toString();
    private static final String resourceDirectoryKey = "R_resourceDirectory"; //RWPSConfigVariables.RESOURCE_DIR.toString();
    private static final String scriptDirectoryKey = "R_scriptDirectory"; //RWPSConfigVariables.SCRIPT_DIR.toString();
    private static final String rServeHostKey = "R_RserveHost"; //RWPSConfigVariables.RSERVE_HOST.toString();
    private static final String rServePortKey = "R_RservePort"; //RWPSConfigVariables.RSERVE_PORT.toString();
    private static final String rServeUserKey = "R_RserveUser"; //RWPSConfigVariables.RSERVE_USER.toString();
    private static final String rServePasswordKey = "R_RservePassword"; //RWPSConfigVariables.RSERVE_PASSWORD.toString();
    private static final String rServeUtilsScriptDirectoryKey = "R_utilsScriptDirectory"; //RWPSConfigVariables.R_UTILS_DIR.toString();
    private static final String cacheProcessesKey = "R_cacheProcesses"; //RWPSConfigVariables.R_CACHE_PROCESSES.toString();
    private static final String sessionMemoryLimitKey = "R_session_memoryLimit"; //RWPSConfigVariables.R_SESSION_MEMORY_LIMIT.toString();
    private static final String resourceDownloadEnabledKey = "R_enableResourceDownload";
    private static final String importDownloadEnabledKey = "R_enableImportDownload";
    private static final String scriptDownloadEnabledKey = "R_enableScriptDownload";
    private static final String sessionInfoDownloadEnabledKey = "R_enableSessionInfoDownload";

    private ConfigurationEntry<Boolean> enableBatchStartEntry = new BooleanConfigurationEntry(enableBatchStartKey, "Enable Batch Start", "Try to start Rserve on the local machine", false, false);
    private ConfigurationEntry<String> datatypeConfigEntry = new StringConfigurationEntry(datatypeConfigKey, "Custom data type mappings", "Location of a config file were you may add costum data types that WPS4R should handle (see below)", false, "R/R_Datatype.conf");
    private ConfigurationEntry<String> wdStrategyEntry = new StringConfigurationEntry(wdStrategyKey, "Working Directory Strategy", "Influences WPS4R on choosing the R working directory for each process run", false, "default");
    private ConfigurationEntry<String> wdNameEntry = new StringConfigurationEntry(wdNameKey, "Working Directory", "The path for the manually set work directory or base directory in conjuction with the strategy 'manualbasedir'", false, "wps4r_working_dir");
    private ConfigurationEntry<String> resourceDirectoryEntry = new StringConfigurationEntry(resourceDirectoryKey, "Resource Directory", "The (relative) path to a directory with resources that can be requested in scripts (default: 'R/resources')", false, "R/resources");
    private ConfigurationEntry<String> scriptDirectoryEntry = new StringConfigurationEntry(scriptDirectoryKey, "Script Directory", "One (or several delimited by ';') folder where R scripts are located and loaded from (default: 'R/scripts')", false, "R/scripts");
    private ConfigurationEntry<String> rServeHostEntry = new StringConfigurationEntry(rServeHostKey, "RServe Host", "The RServe Host URL (default: 'localhost')", false, "localhost");
    private ConfigurationEntry<String> rServePortEntry = new StringConfigurationEntry(rServePortKey, "RServe Port", "The RServe Port (default: '6311')", false, "6311");
    private ConfigurationEntry<String> rServeUserEntry = new StringConfigurationEntry(rServeUserKey, "RServe User", "Username for Rserve authentication (only if Rserve requires login)", false, "");
    private ConfigurationEntry<String> rServePasswordEntry = new StringConfigurationEntry(rServePasswordKey, "RServe Password", "Password for Rserve authentication (only if Rserve requires login)", false, "");
    private ConfigurationEntry<String> rServeUtilsScriptsDirectoryEntry = new StringConfigurationEntry(rServeUtilsScriptDirectoryKey, "RServe Utils Scripts", "Set the directory that utils scripts are loaded from. This can be a directory relative to the webapp, a full path, or a relative path to a *file* within the classpath (no folders are supported on the classpath and files from the classpath are copied to a temp file so that R can load them)", false, "R/utils;org/n52/wps/R/unzipRenameFile.R;org/n52/wps/R/wpsStatus.R");
    private ConfigurationEntry<Boolean> cacheProcessesEntry = new BooleanConfigurationEntry(cacheProcessesKey, "Cache Processes", "If deactivated, the scripts files are loaded every time the algorithm is called, for development (default: true)", false, true);
    private ConfigurationEntry<String> sessionMemoryLimitEntry = new StringConfigurationEntry(sessionMemoryLimitKey, "Session Memory Limit", "Set the R session memory limit (default: 1000)", false, "1000");
    private ConfigurationEntry<Boolean> resourceDownloadEnabledEntry = new BooleanConfigurationEntry(resourceDownloadEnabledKey, "Enable resource download", "Allows to download R resources", false, true);
    private ConfigurationEntry<Boolean> importDownloadEnabledEntry = new BooleanConfigurationEntry(importDownloadEnabledKey, "Enable imports download", "Allows to download R imports", false, true);
    private ConfigurationEntry<Boolean> scriptDownloadEnabledEntry = new BooleanConfigurationEntry(scriptDownloadEnabledKey, "Enable script download", "Allows to download R scripts", false, true);
    private ConfigurationEntry<Boolean> sessionInfoDownloadEnabledEntry = new BooleanConfigurationEntry(sessionInfoDownloadEnabledKey, "Enable session info download", "Allows to download R session info", false, true);

    private Boolean enableBatchStart;
    private String datatypeConfig;
    private String wdStrategy;
    private String wdName;
    private String resourceDirectory;
    private String scriptDirectory;
    private String rServeHost;
    private String rServePort;
    private String rServeUser;
    private String rServePassword;
    private String rServeUtilsScriptsDirectory;
    private Boolean cacheProcesses;
    private String sessionMemoryLimit;
    private boolean resourceDownloadEnabled;
    private boolean importDownloadEnabled;
    private boolean scriptDownloadEnabled;
    private boolean sessionInfoDownloadEnabled;

    private List<AlgorithmEntry> algorithmEntries;

    private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(enableBatchStartEntry, datatypeConfigEntry,
            wdStrategyEntry, wdNameEntry, resourceDirectoryEntry, scriptDirectoryEntry, rServeHostEntry, rServePortEntry, rServeUserEntry,
            rServePasswordEntry, rServeUtilsScriptsDirectoryEntry, cacheProcessesEntry, sessionMemoryLimitEntry, resourceDownloadEnabledEntry,
            importDownloadEnabledEntry, scriptDownloadEnabledEntry, sessionInfoDownloadEnabledEntry);

    public RConfigurationModule() {
        algorithmEntries = new ArrayList<>();
    }

    @Override
    public String getModuleName() {
        return "R Configuration Module";
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
        return ConfigurationCategory.REPOSITORY;
    }

    @Override
    public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
        return configurationEntries;
    }

    @Override
    public List<AlgorithmEntry> getAlgorithmEntries() {
        return algorithmEntries;
    }

    @Override
    public List<FormatEntry> getFormatEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClassName() {
        return RAlgorithmRepository.class.getName();
    }

    public Boolean isEnableBatchStart() {
        return isNullOrEmpty(enableBatchStart, enableBatchStartEntry)
                ? enableBatchStartEntry.getValue()
                : enableBatchStart;
    }

    @ConfigurationKey(key = enableBatchStartKey)
    public void setEnableBatchStart(boolean enableBatchStart) {
        this.enableBatchStart = enableBatchStart;
    }

    public String getDatatypeConfig() {
        return isNullOrEmpty(datatypeConfig, datatypeConfigEntry)
                ? datatypeConfigEntry.getValue()
                : datatypeConfig;
    }

    @ConfigurationKey(key = datatypeConfigKey)
    public void setDatatypeConfig(String datatypeConfig) {
        this.datatypeConfig = datatypeConfig;
    }

    public String getWdStrategy() {
        return isNullOrEmpty(wdStrategy, wdStrategyEntry)
                ? wdStrategyEntry.getValue()
                : wdStrategy;
    }

    @ConfigurationKey(key = wdStrategyKey)
    public void setWdStrategy(String wdStrategy) {
        this.wdStrategy = wdStrategy;
    }

    public String getWdName() {
        return isNullOrEmpty(wdName, wdNameEntry)
                ? wdNameEntry.getValue()
                : wdName;
    }

    @ConfigurationKey(key = wdNameKey)
    public void setWdName(String wdName) {
        this.wdName = wdName;
    }

    public String getResourceDirectory() {
        return isNullOrEmpty(resourceDirectory, resourceDirectoryEntry)
                ? resourceDirectoryEntry.getValue()
                : resourceDirectory;
    }

    @ConfigurationKey(key = resourceDirectoryKey)
    public void setResourceDirectory(String resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }

    public String getScriptDirectory() {
        return isNullOrEmpty(scriptDirectory, scriptDirectoryEntry)
                ? scriptDirectoryEntry.getValue()
                : scriptDirectory;
    }

    @ConfigurationKey(key = scriptDirectoryKey)
    public void setScriptDirectory(String scriptDirectory) {
        this.scriptDirectory = scriptDirectory;
    }

    public String getRServeHost() {
        return isNullOrEmpty(rServeHost, rServeHostEntry)
                ? rServeHostEntry.getValue()
                : rServeHost;
    }

    @ConfigurationKey(key = rServeHostKey)
    public void setRServeHost(String rServeHost) {
        if (rServeHost != null && !rServeHost.isEmpty()) {
            this.rServeHost = rServeHost;
        }
    }

    public String getRServePort() {
        return isNullOrEmpty(rServePort, rServePortEntry)
                ? rServePortEntry.getValue()
                : rServePort;
    }

    @ConfigurationKey(key = rServePortKey)
    public void setRServePort(String rServePort) {
        try {
            Integer.parseInt(rServePort);
        } catch (NumberFormatException e) {
            LOGGER.warn("Config variable {} does not contain a parseble integer. Using default port 6311.", rServePortKey, e);
            rServePort = "6311";
        }
        this.rServePort = rServePort;
    }

    public String getRServeUser() {
        return isNullOrEmpty(rServeUser, rServeUserEntry)
                ? rServeUserEntry.getValue()
                : rServeUser;
    }

    @ConfigurationKey(key = rServeUserKey)
    public void setRServeUser(String rServeUser) {
        this.rServeUser = rServeUser;
    }

    public String getrServePassword() {
        return isNullOrEmpty(rServePassword, rServePasswordEntry)
                ? rServePasswordEntry.getValue()
                : rServePassword;
    }

    @ConfigurationKey(key = rServePasswordKey)
    public void setRServePassword(String rServePassword) {
        this.rServePassword = rServePassword;
    }

    public String getRServeUtilsScriptsDirectory() {
        return isNullOrEmpty(rServeUtilsScriptsDirectory, rServeUtilsScriptsDirectoryEntry)
                ? rServeUtilsScriptsDirectoryEntry.getValue()
                : rServeUtilsScriptsDirectory;
    }

    @ConfigurationKey(key = rServeUtilsScriptDirectoryKey)
    public void setRServeUtilsScriptsDirectory(String rServeUtilsScriptsDirectory) {
        this.rServeUtilsScriptsDirectory = rServeUtilsScriptsDirectory;
    }

    public Boolean isCacheProcesses() {
        return isNullOrEmpty(cacheProcesses, cacheProcessesEntry)
                ? cacheProcessesEntry.getValue()
                : cacheProcesses;
    }

    @ConfigurationKey(key = cacheProcessesKey)
    public void setCacheProcesses(boolean cacheProcesses) {
        this.cacheProcesses = cacheProcesses;
    }

    public String getSessionMemoryLimit() {
        return isNullOrEmpty(sessionMemoryLimit, sessionMemoryLimitEntry)
                ? sessionMemoryLimitEntry.getValue()
                : sessionMemoryLimit;
    }

    @ConfigurationKey(key = sessionMemoryLimitKey)
    public void setSessionMemoryLimit(String sessionMemoryLimit) {
        this.sessionMemoryLimit = sessionMemoryLimit;
    }

    public boolean isImportDownloadEnabled() {
        return importDownloadEnabled;
    }

    @ConfigurationKey(key = importDownloadEnabledKey)
    public void setImportDownloadEnabled(boolean importDownloadEnabled) {
        this.importDownloadEnabled = importDownloadEnabled;
    }

    public boolean isResourceDownloadEnabled() {
        return resourceDownloadEnabled;
    }

    @ConfigurationKey(key = resourceDownloadEnabledKey)
    public void setResourceDownloadEnabled(boolean resourceDownloadEnabled) {
        this.resourceDownloadEnabled = resourceDownloadEnabled;
    }

    public boolean isScriptDownloadEnabled() {
        return scriptDownloadEnabled;
    }

    @ConfigurationKey(key = scriptDownloadEnabledKey)
    public void setScriptDownloadEnabled(boolean scriptDownloadEnabled) {
        this.scriptDownloadEnabled = scriptDownloadEnabled;
    }

    public boolean isSessionInfoDownloadEnabled() {
        return sessionInfoDownloadEnabled;
    }

    @ConfigurationKey(key = sessionInfoDownloadEnabledKey)
    public void setSessionInfoDownloadEnabled(boolean sessionInfoDownloadEnabled) {
        this.sessionInfoDownloadEnabled = sessionInfoDownloadEnabled;
    }

    private boolean isNullOrEmpty(Object value, ConfigurationEntry<?> configEntry) {
        return value == null; // TODO
    }

    private boolean isNullOrEmpty(String value, ConfigurationEntry<?> configEntry) {
        boolean nullOrEmpty = value == null || value.isEmpty();
        if (nullOrEmpty) {
            if (configEntry.isRequired()) {
                LOGGER.warn("Required config parameter '{}' is null or empty!", configEntry.getKey());
            } else {
                LOGGER.info("Config parameter '{}' is null or empty!", configEntry.getKey());
            }
        }
        return nullOrEmpty;
    }

}
