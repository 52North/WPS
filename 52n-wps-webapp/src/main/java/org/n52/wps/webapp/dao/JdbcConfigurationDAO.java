package org.n52.wps.webapp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcConfigurationDAO implements ConfigurationDAO {

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Override
	public Object getConfigurationEntryValue(String moduleClassName, String entryKey) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT configuration_value FROM configurationentry WHERE entry_key = :entry_key AND configuration_module = :configuration_module";

		List<Object> values = jdbcTemplate.query(sql, parameters, new RowMapper<Object>() {

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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("configuration_value", value);
		jdbcTemplate.update("INSERT INTO configurationentry (entry_key, configuration_module, configuration_value)"
				+ "VALUES(:entry_key, :configuration_module, :configuration_value)", parameters);
	}

	@Override
	public void updateConfigurationEntryValue(String moduleClassName, String entryKey, Object value) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("entry_key", entryKey);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("configuration_value", value);
		jdbcTemplate.update("UPDATE configurationentry SET configuration_value = :configuration_value "
				+ "WHERE entry_key = :entry_key AND configuration_module = :configuration_module", parameters);
	}

	@Override
	public AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		String sql = "SELECT * FROM algorithmentry WHERE algorithm_name = :algorithm_name AND configuration_module = :configuration_module";

		List<AlgorithmEntry> entries = jdbcTemplate.query(sql, parameters, new RowMapper<AlgorithmEntry>() {

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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("active", active);
		jdbcTemplate.update("INSERT INTO algorithmentry (algorithm_name, configuration_module, active)"
				+ "VALUES(:algorithm_name, :configuration_module, :active)", parameters);
	}

	@Override
	public void updateAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("algorithm_name", algorithm);
		parameters.put("configuration_module", moduleClassName);
		parameters.put("active", active);
		jdbcTemplate.update("UPDATE algorithmentry SET active = :active "
				+ "WHERE algorithm_name = :algorithm_name AND configuration_module = :configuration_module", parameters);
	}
}
