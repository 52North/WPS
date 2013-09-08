/**
 * Copyright (C) 2013
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

package org.n52.wps.webapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("configurationDAO")
public class JdbcConfigurationDAO implements ConfigurationDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static Logger LOGGER = LoggerFactory.getLogger(JdbcConfigurationDAO.class);

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
		LOGGER.debug("Inserting algorithm entry '{}' in configuration module '{}' with the status of '{}' into the database.",
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
}
