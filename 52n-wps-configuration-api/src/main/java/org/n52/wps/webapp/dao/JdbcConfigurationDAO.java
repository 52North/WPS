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
package org.n52.wps.webapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

/**
 * An implementation for the {@link ConfigurationDAO} interface. This
 * implementation uses JDBC through Spring's {@code NamedParameterJdbcTemplate}.
 */
@Repository("configurationDAO")
public class JdbcConfigurationDAO implements ConfigurationDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(JdbcConfigurationDAO.class);

    private static final String MIME_TYPE = "mime_type";

    private static final String SCHEMA = "schema";

    private static final String ENCODING = "encoding";

    private static final String STATUS = "status";

    private static final String MODULE_CLASS_NAME = "module_class_name";

    private static final String CONFIGURATION_VALUE = "configuration_value";

    private static final String ENTRY_KEY = "entry_key";

    private static final String CONFIGURATION_MODULE = "configuration_module";

    private static final String ALGORITHM_NAME = "algorithm_name";

    private static final String ACTIVE = "active";

    private static final String AND_CONFIG_MODULE = "AND configuration_module = :configuration_module";

    private static final String WHERE_CLAUSE_1 = "WHERE entry_key = :entry_key " + AND_CONFIG_MODULE;

    private static final String WHERE_CLAUSE_2 = "WHERE mime_type = :mime_type "
            + "AND schema = :schema AND encoding = :encoding ";

    private static final String WHERE_CLAUSE_3 = "WHERE algorithm_name = :algorithm_name ";

    private static final String UPDATING_FORMAT_ENTRY = "Updating format entry '{}', '{}', '{}' ";


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {

        if (namedParameterJdbcTemplate != null) {

            try {
                /*
                 * if there is a user, data has already been loaded into the db
                 */
                String sql = "SELECT * FROM users";

                List<String> list = namedParameterJdbcTemplate.query(sql, new RowMapper<String>() {

                    @Override
                    public String mapRow(ResultSet rs,
                            int rowNum) throws SQLException {
                        return rs.getString("username");
                    }

                });

                if (list.isEmpty()) {
                    ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
                    rdp.addScript(new ClassPathResource("db/initial-data.sql"));
                    rdp.populate(dataSource.getConnection());
                }
            } catch (SQLException e) {
                LOGGER.error("Could not load initial data.", e.getMessage());
            }
        }
    }

    @Override
    public void insertConfigurationModule(ConfigurationModule module) {
        LOGGER.debug("Inserting configuration module '{}' into the database.", module.getClass().getName());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MODULE_CLASS_NAME, module.getClass().getName());
        parameters.put(STATUS, module.isActive());
        namedParameterJdbcTemplate.update(
                "INSERT INTO configurationmodule (module_class_name, status)" + "VALUES(:module_class_name, :status)",
                parameters);
    }

    @Override
    public void updateConfigurationModuleStatus(ConfigurationModule module) {
        LOGGER.debug("Updating configuration module '{}' in the database.", module.getClass().getName());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MODULE_CLASS_NAME, module.getClass().getName());
        parameters.put(STATUS, module.isActive());
        namedParameterJdbcTemplate.update(
                "UPDATE configurationmodule SET status = :status " + "WHERE module_class_name = :module_class_name",
                parameters);
    }

    @Override
    public Boolean getConfigurationModuleStatus(ConfigurationModule module) {
        LOGGER.debug("Getting configuration module '{}' status from the database.", module.getClass().getName());
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MODULE_CLASS_NAME, module.getClass().getName());
        String sql = "SELECT status FROM configurationmodule WHERE module_class_name = :module_class_name";

        List<Boolean> status = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet rs,
                    int rowNum) throws SQLException {
                return rs.getBoolean(STATUS);
            }
        });

        if (status.isEmpty()) {
            return false;
        } else if (status.size() == 1) {
            return status.get(0);
        } else {
            return false;
        }
    }

    @Override
    public Object getConfigurationEntryValue(String moduleClassName,
            String entryKey) {
        LOGGER.debug("Getting configuration entry '{}' in configuration module '{}' from the database.", entryKey,
                moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ENTRY_KEY, entryKey);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        String sql =
                "SELECT configuration_value FROM configurationentry "
                + WHERE_CLAUSE_1;

        List<Object> values = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs,
                    int rowNum) throws SQLException {
                return rs.getObject(CONFIGURATION_VALUE);
            }
        });

        if (values.isEmpty()) {
            return null;
        } else if (values.size() == 1) {
            return values.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void insertConfigurationEntryValue(String moduleClassName,
            String entryKey,
            Object value) {
        try {
            LOGGER.debug(
                    "Inserting value '{}' for configuration entry '{}' "
                    + "in configuration module '{}' into the database.",
                    value, entryKey, moduleClassName);
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(ENTRY_KEY, entryKey);
            parameters.put(CONFIGURATION_MODULE, moduleClassName);
            parameters.put(CONFIGURATION_VALUE, value);
            namedParameterJdbcTemplate
                    .update("INSERT INTO configurationentry (entry_key, configuration_module, configuration_value)"
                            + "VALUES(:entry_key, :configuration_module, :configuration_value)", parameters);
        } catch (DataAccessException e) {
            String valueString = value != null ? value.toString() : null;
            LOGGER.warn("{}: could not insert {}={}", moduleClassName, entryKey, valueString, e);
        }
    }

    @Override
    public void updateConfigurationEntryValue(String moduleClassName,
            String entryKey,
            Object value) {
        LOGGER.debug(
                "Updating configuration entry '{}' in configuration module '{}' to the value of '{}' in the database.",
                entryKey, moduleClassName, value);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ENTRY_KEY, entryKey);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        parameters.put(CONFIGURATION_VALUE, value);
        namedParameterJdbcTemplate.update("UPDATE configurationentry SET configuration_value = :configuration_value "
                + WHERE_CLAUSE_1, parameters);
    }

    @Override
    public AlgorithmEntry getAlgorithmEntry(String moduleClassName,
            String algorithm) {
        LOGGER.debug("Getting algorithm entry '{}' in configuration module '{}' from the database.", algorithm,
                moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ALGORITHM_NAME, algorithm);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        String sql =
                "SELECT * FROM algorithmentry " + WHERE_CLAUSE_3
                + AND_CONFIG_MODULE;

        List<AlgorithmEntry> entries =
                namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<AlgorithmEntry>() {

                    @Override
                    public AlgorithmEntry mapRow(ResultSet rs,
                            int rowNo) throws SQLException {
                        AlgorithmEntry e = new AlgorithmEntry(rs.getString(ALGORITHM_NAME), rs.getBoolean(ACTIVE));
                        return e;
                    }

                });

        if (entries.isEmpty()) {
            return null;
        } else if (entries.size() == 1) {
            return entries.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void insertAlgorithmEntry(String moduleClassName,
            String algorithm,
            boolean active) {
        LOGGER.debug(
                "Inserting algorithm entry '{}' in configuration "
                + "module '{}' with the status of '{}' into the database.",
                algorithm, moduleClassName, active);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ALGORITHM_NAME, algorithm);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        parameters.put(ACTIVE, active);
        namedParameterJdbcTemplate.update("INSERT INTO algorithmentry (algorithm_name, configuration_module, active)"
                + "VALUES(:algorithm_name, :configuration_module, :active)", parameters);
    }

    @Override
    public void updateAlgorithmEntry(String moduleClassName,
            String algorithm,
            boolean active) {
        LOGGER.debug(
                "Updating algorithm entry '{}' in configuration module '{}' to the status of '{}' in the database.",
                algorithm, moduleClassName, active);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ALGORITHM_NAME, algorithm);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        parameters.put(ACTIVE, active);
        namedParameterJdbcTemplate.update(
                "UPDATE algorithmentry SET active = :active "
                        + WHERE_CLAUSE_3 + AND_CONFIG_MODULE,
                parameters);
    }

    @Override
    public void updateAlgorithmEntry(String moduleClassName,
            String newAlgorithmName,
            String oldAlgorithmName) {
        LOGGER.debug("Updating algorithm entry '{}' in configuration module '{}' to new entry '{}' in the database.",
                oldAlgorithmName, moduleClassName, newAlgorithmName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("new_algorithm_name", newAlgorithmName);
        parameters.put("old_algorithm_name", oldAlgorithmName);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        namedParameterJdbcTemplate.update(
                "UPDATE algorithmentry SET algorithm_name = :new_algorithm_name "
                        + "WHERE algorithm_name = :old_algorithm_name " + AND_CONFIG_MODULE,
                parameters);
    }

    @Override
    public List<AlgorithmEntry> getAlgorithmEntries(String moduleClassName) {
        LOGGER.debug("Getting all algorithm entries of configuration module '{}' from the database.", moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        String sql = "SELECT * FROM algorithmentry WHERE configuration_module = :configuration_module";

        List<AlgorithmEntry> algorithmEntries =
                namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<AlgorithmEntry>() {
                    @Override
                    public AlgorithmEntry mapRow(ResultSet rs,
                            int rowNum) throws SQLException {
                        AlgorithmEntry e = new AlgorithmEntry(rs.getString(ALGORITHM_NAME), rs.getBoolean(ACTIVE));
                        return e;
                    }
                });

        return algorithmEntries;
    }

    @Override
    public void deleteAlgorithmEntry(String moduleClassName,
            String algorithmName) {
        LOGGER.debug("Deleting algorithm entry '{}' from configuration module '{}'.", algorithmName, moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ALGORITHM_NAME, algorithmName);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        int numberOfRowsAffected = namedParameterJdbcTemplate.update(
                "DELETE FROM algorithmentry "
                        + WHERE_CLAUSE_3 + AND_CONFIG_MODULE,
                parameters);
        LOGGER.debug("Number of rows affected: " + numberOfRowsAffected);
    }

    @Override
    public FormatEntry getFormatEntry(String moduleClassName,
            String mimeType,
            String schema,
            String encoding) {
        LOGGER.debug("Getting format entry '{}', '{}', '{}' in configuration module '{}' from the database.", mimeType,
                schema, encoding, moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MIME_TYPE, mimeType);
        parameters.put(SCHEMA, schema);
        parameters.put(encoding, encoding);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        String sql = "SELECT * FROM formatentry WHERE id = :id";

        List<FormatEntry> entries = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<FormatEntry>() {

            @Override
            public FormatEntry mapRow(ResultSet rs,
                    int rowNo) throws SQLException {
                FormatEntry e = new FormatEntry(rs.getString(MIME_TYPE), rs.getString(SCHEMA),
                        rs.getString(ENCODING), rs.getBoolean(ACTIVE));
                return e;
            }

        });

        if (entries.isEmpty()) {
            return null;
        } else if (entries.size() == 1) {
            return entries.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void insertFormatEntry(String moduleClassName,
            String mimeType,
            String schema,
            String encoding,
            boolean active) {
        LOGGER.debug(
                "Inserting format entry '{}' in configuration module '{}' with the status of '{}' into the database.",
                mimeType, moduleClassName, active);
        // TODO update log
        // Map<String, Object> parameters = new HashMap<String, Object>();
        // parameters.put("mime_type", mimeType);
        // parameters.put("schema", schema);
        // parameters.put("encoding", encoding);
        // parameters.put("configuration_module", moduleClassName);
        // parameters.put("active", active);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        parameterSource.addValue(MIME_TYPE, mimeType);
        parameterSource.addValue(SCHEMA, schema);
        parameterSource.addValue(ENCODING, encoding);
        parameterSource.addValue(CONFIGURATION_MODULE, moduleClassName);
        parameterSource.addValue(ACTIVE, active);

        namedParameterJdbcTemplate
                .update("INSERT INTO formatentry (mime_type, schema, encoding, configuration_module, active)"
                        + "VALUES(:mime_type, :schema, :encoding, :configuration_module, :active)", parameterSource);

    }

    @Override
    public void updateFormatEntry(String moduleClassName,
            String mimeType,
            String schema,
            String encoding,
            boolean active) {
        LOGGER.debug(UPDATING_FORMAT_ENTRY
                + "of configuration module '{}' to the status of '{}' in the database.",
                mimeType, schema, encoding, moduleClassName, active);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MIME_TYPE, mimeType);
        parameters.put(SCHEMA, schema);
        parameters.put(ENCODING, encoding);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        parameters.put(ACTIVE, active);
        namedParameterJdbcTemplate.update("UPDATE formatentry SET active = :active "
                + WHERE_CLAUSE_2
                + AND_CONFIG_MODULE,
                parameters);
    }

    @Override
    public void updateFormatEntry(String moduleClassName,
            String oldMimeType,
            String oldSchema,
            String oldEncoding,
            String newMimeType,
            String newSchema,
            String newEncoding) {
        LOGGER.debug(
                UPDATING_FORMAT_ENTRY
                + "in configuration module '{}' to entry '{}', '{}', '{}' in the database.",
                oldMimeType, oldSchema, oldEncoding, moduleClassName, newMimeType, newSchema, newEncoding);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("new_mimetype", newMimeType);
        parameters.put("new_schema", newSchema);
        parameters.put("new_encoding", newEncoding);
        parameters.put("old_mimetype", oldMimeType);
        parameters.put("old_schema", oldSchema);
        parameters.put("old_encoding", oldEncoding);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        namedParameterJdbcTemplate.update(
                "UPDATE formatentry SET mime_type = :new_mimetype, schema =:new_schema, encoding = :new_encoding "
                        + "WHERE mime_type = :old_mimetype AND schema =:old_schema AND encoding = :old_encoding "
                        + AND_CONFIG_MODULE,
                parameters);
    }

    @Override
    public List<FormatEntry> getFormatEntries(String moduleClassName) {
        LOGGER.debug("Getting all format entries of configuration module '{}' from the database.", moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        String sql = "SELECT * FROM formatentry WHERE configuration_module = :configuration_module";

        List<FormatEntry> formatEntries =
                namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<FormatEntry>() {
                    @Override
                    public FormatEntry mapRow(ResultSet rs,
                            int rowNum) throws SQLException {
                        FormatEntry e = new FormatEntry(rs.getString(MIME_TYPE), rs.getString(SCHEMA),
                                rs.getString(ENCODING), rs.getBoolean(ACTIVE));
                        return e;
                    }
                });

        return formatEntries;
    }

    @Override
    public void deleteFormatEntry(String moduleClassName,
            String mimeType,
            String schema,
            String encoding) {
        LOGGER.debug("Deleting format entry '{}', '{}', '{}' of module, '{}' from database", mimeType, schema, encoding,
                moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MIME_TYPE, mimeType);
        parameters.put(SCHEMA, schema);
        parameters.put(encoding, encoding);
        parameters.put(CONFIGURATION_MODULE, moduleClassName);
        namedParameterJdbcTemplate.update("DELETE FROM formatentry "
                + WHERE_CLAUSE_2
                + AND_CONFIG_MODULE,
                parameters);

    }

    @Override
    public Boolean isConfigurationModulePersistent(String moduleClassName) {
        LOGGER.debug("Getting configuration module '{}' from the database.", moduleClassName);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MODULE_CLASS_NAME, moduleClassName);
        String sql = "SELECT * FROM configurationmodule WHERE module_class_name = :module_class_name";

        Boolean isModulePersistent =
                namedParameterJdbcTemplate.query(sql, parameters, new ResultSetExtractor<Boolean>() {

                    @Override
                    public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                        return rs.next();
                    }

                });

        LOGGER.debug("Configuration module is persistent: ", isModulePersistent);

        return isModulePersistent;
    }
}
