/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

/**
 * An implementation for the {@link ConfigurationDAO} interface. This implementation uses JDBC through Spring's
 * {@code NamedParameterJdbcTemplate}.
 */
@Repository("configurationDAO")
public class JdbcConfigurationDAO implements ConfigurationDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private DataSource dataSource;

	private static Logger LOGGER = LoggerFactory.getLogger(JdbcConfigurationDAO.class);

	@PostConstruct
	public void init(){

		if (namedParameterJdbcTemplate != null) {

			try {
				/*
				 * if there is a user, data has already been loaded into the db 
				 */
				String sql = "SELECT * FROM users";
				
				List<String> list = namedParameterJdbcTemplate.query(sql, new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
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
		parameters.put("module_class_name", module.getClass().getName());
		parameters.put("status", module.isActive());
		namedParameterJdbcTemplate.update("INSERT INTO configurationmodule (module_class_name, status)"
				+ "VALUES(:module_class_name, :status)", parameters);
	}

	@Override
	public void updateConfigurationModuleStatus(ConfigurationModule module) {
		LOGGER.debug("Updating configuration module '{}' in the database.", module.getClass().getName());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("module_class_name", module.getClass().getName());
		parameters.put("status", module.isActive());
		namedParameterJdbcTemplate.update("UPDATE configurationmodule SET status = :status "
				+ "WHERE module_class_name = :module_class_name", parameters);
	}

	@Override
	public Boolean getConfigurationModuleStatus(ConfigurationModule module) {
		LOGGER.debug("Getting configuration module '{}' status from the database.", module.getClass().getName());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("module_class_name", module.getClass().getName());
		String sql = "SELECT status FROM configurationmodule WHERE module_class_name = :module_class_name";

		List<Boolean> status = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<Boolean>() {
			@Override
			public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getBoolean("status");
			}
		});

		if (status.isEmpty()) {
			return null;
		} else if (status.size() == 1) {
			return status.get(0);
		} else {
			return null;
		}
	}

	@Override
	public Object getConfigurationEntryValue(String moduleClassName, String entryKey) {
		LOGGER.debug("Getting configuration entry '{}' in configuration module '{}' from the database.", entryKey,
				moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT configuration_value FROM configurationentry WHERE entry_key = :entry_key AND configuration_module = :configuration_module";

		List<Object> values = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getObject("configuration_value");
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
	public void insertConfigurationEntryValue(String moduleClassName, String entryKey, Object value) {
		LOGGER.debug(
				"Inserting value '{}' for configuration entry '{}' in configuration module '{}' into the database.",
				value, entryKey, moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("configuration_value", value);
		namedParameterJdbcTemplate.update(
				"INSERT INTO configurationentry (entry_key, configuration_module, configuration_value)"
						+ "VALUES(:entry_key, :configuration_module, :configuration_value)", parameters);
	}

	@Override
	public void updateConfigurationEntryValue(String moduleClassName, String entryKey, Object value) {
		LOGGER.debug(
				"Updating configuration entry '{}' in configuration module '{}' to the value of '{}' in the database.",
				entryKey, moduleClassName, value);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("configuration_value", value);
		namedParameterJdbcTemplate.update("UPDATE configurationentry SET configuration_value = :configuration_value "
				+ "WHERE entry_key = :entry_key AND configuration_module = :configuration_module", parameters);
	}

	@Override
	public AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm) {
		LOGGER.debug("Getting algorithm entry '{}' in configuration module '{}' from the database.", algorithm,
				moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT * FROM algorithmentry WHERE algorithm_name = :algorithm_name AND configuration_module = :configuration_module";

		List<AlgorithmEntry> entries = namedParameterJdbcTemplate.query(sql, parameters,
				new RowMapper<AlgorithmEntry>() {

					@Override
					public AlgorithmEntry mapRow(ResultSet rs, int rowNo) throws SQLException {
						AlgorithmEntry e = new AlgorithmEntry(rs.getString("algorithm_name"), rs.getBoolean("active"));
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
	public void insertAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		LOGGER.debug(
				"Inserting algorithm entry '{}' in configuration module '{}' with the status of '{}' into the database.",
				algorithm, moduleClassName, active);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("active", active);
		namedParameterJdbcTemplate.update("INSERT INTO algorithmentry (algorithm_name, configuration_module, active)"
				+ "VALUES(:algorithm_name, :configuration_module, :active)", parameters);
	}

	@Override
	public void updateAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		LOGGER.debug(
				"Updating algorithm entry '{}' in configuration module '{}' to the status of '{}' in the database.",
				algorithm, moduleClassName, active);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("active", active);
		namedParameterJdbcTemplate
				.update("UPDATE algorithmentry SET active = :active "
						+ "WHERE algorithm_name = :algorithm_name AND configuration_module = :configuration_module",
						parameters);
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries(String moduleClassName) {
		LOGGER.debug("Getting all algorithm entries of configuration module '{}' from the database.", moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT * FROM algorithmentry WHERE configuration_module = :configuration_module";

		List<AlgorithmEntry> algorithmEntries = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<AlgorithmEntry>() {
			@Override
			public AlgorithmEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
				AlgorithmEntry e = new AlgorithmEntry(rs.getString("algorithm_name"), rs.getBoolean("active"));
				return e;
			}
		});
		
		return algorithmEntries;
	}

	@Override
	public void deleteAlgorithmEntry(String moduleClassName,
			String algorithmName) {
		LOGGER.debug(
				"Deleting algorithm entry '{}' from configuration module '{}'.",
				algorithmName, moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithmName);
		parameters.put("configuration_module", moduleClassName);
		int numberOfRowsAffected = namedParameterJdbcTemplate
				.update("DELETE FROM algorithmentry "
						+ "WHERE algorithm_name = :algorithm_name AND configuration_module = :configuration_module",
						parameters);
		LOGGER.debug("Number of rows affected: " + numberOfRowsAffected);
	}
	
	@Override
	public FormatEntry getFormatEntry(String moduleClassName, String mimeType, String schema, String encoding) {
		LOGGER.debug("Getting format entry '{}', '{}', '{}' in configuration module '{}' from the database.", mimeType, schema, encoding,
				moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("mime_type", mimeType);
		parameters.put("schema", schema);
		parameters.put("encoding", encoding);
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT * FROM formatentry WHERE id = :id";

		List<FormatEntry> entries = namedParameterJdbcTemplate.query(sql, parameters,
				new RowMapper<FormatEntry>() {

					@Override
					public FormatEntry mapRow(ResultSet rs, int rowNo) throws SQLException {
						FormatEntry e = new FormatEntry(rs.getString("mime_type"), rs.getString("schema"), rs.getString("encoding"), rs.getBoolean("active"));
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
	public void insertFormatEntry(String moduleClassName, String mimeType, String schema, String encoding, boolean active) {
		LOGGER.debug(
				"Inserting format entry '{}' in configuration module '{}' with the status of '{}' into the database.",
				mimeType, moduleClassName, active);//TODO update log
//		Map<String, Object> parameters = new HashMap<String, Object>();
//		parameters.put("mime_type", mimeType);
//		parameters.put("schema", schema);
//		parameters.put("encoding", encoding);
//		parameters.put("configuration_module", moduleClassName);
//		parameters.put("active", active);
				
		MapSqlParameterSource parameterSource = new MapSqlParameterSource();
		
		parameterSource.addValue("mime_type", mimeType);
		parameterSource.addValue("schema", schema);
		parameterSource.addValue("encoding", encoding);
		parameterSource.addValue("configuration_module", moduleClassName);
		parameterSource.addValue("active", active);
		
		namedParameterJdbcTemplate.update("INSERT INTO formatentry (mime_type, schema, encoding, configuration_module, active)"
				+ "VALUES(:mime_type, :schema, :encoding, :configuration_module, :active)", parameterSource);

	}

	@Override
	public void updateFormatEntry(String moduleClassName, String mimeType, String schema, String encoding, boolean active) {
		LOGGER.debug(
				"Updating format entry '{}', '{}', '{}' of configuration module '{}' to the status of '{}' in the database.",
				mimeType, schema, encoding, moduleClassName, active);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("mime_type", mimeType);
		parameters.put("schema", schema);
		parameters.put("encoding", encoding);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("active", active);
		namedParameterJdbcTemplate
				.update("UPDATE formatentry SET active = :active "
						+ "WHERE mime_type = :mime_type AND schema = :schema AND encoding = :encoding AND configuration_module = :configuration_module",
						parameters);
	}

	@Override
	public List<FormatEntry> getFormatEntries(String moduleClassName) {
		LOGGER.debug("Getting all format entries of configuration module '{}' from the database.", moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT * FROM formatentry WHERE configuration_module = :configuration_module";

		List<FormatEntry> formatEntries = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<FormatEntry>() {
			@Override
			public FormatEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
				FormatEntry e = new FormatEntry(rs.getString("mime_type"), rs.getString("schema"), rs.getString("encoding"), rs.getBoolean("active"));
				return e;
			}
		});
		
		return formatEntries;
	}

	@Override
	public void deleteFormatEntry(String moduleClassName, String mimeType, String schema, String encoding) {
		LOGGER.debug(
				"Deleting format entry '{}', '{}', '{}' of module, '{}' from database",
				mimeType, schema, encoding, moduleClassName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("mime_type", mimeType);
		parameters.put("schema", schema);
		parameters.put("encoding", encoding);
		parameters.put("configuration_module", moduleClassName);
		namedParameterJdbcTemplate
				.update("DELETE FROM formatentry "
						+ "WHERE mime_type = :mime_type AND schema = :schema AND encoding = :encoding AND configuration_module = :configuration_module",
						parameters);
		
	}
}
