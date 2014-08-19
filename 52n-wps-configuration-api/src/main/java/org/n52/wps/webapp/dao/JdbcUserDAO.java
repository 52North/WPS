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

import org.n52.wps.webapp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * An implementation for the {@link UserDAO} interface. This implementation uses JDBC through Spring's
 * {@code NamedParameterJdbcTemplate}.
 */
@Repository("userDAO")
public class JdbcUserDAO implements UserDAO {

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public User getUserById(int userId) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user_id", userId);
		String sql = "SELECT * FROM users WHERE user_id = :user_id";
		List<User> users = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<User>() {

			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User u = new User();
				u.setUserId(rs.getInt("user_id"));
				u.setUsername(rs.getString("username"));
				u.setPassword(rs.getString("password"));
				u.setRole(rs.getString("role"));
				return u;
			}
		});

		if (users.isEmpty()) {
			return null;
		} else if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	@Override
	public User getUserByUsername(String username) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("username", username);
		String sql = "SELECT * FROM users WHERE username = :username";
		List<User> users = namedParameterJdbcTemplate.query(sql, parameters, new RowMapper<User>() {

			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User u = new User();
				u.setUserId(rs.getInt("user_id"));
				u.setUsername(rs.getString("username"));
				u.setPassword(rs.getString("password"));
				u.setRole(rs.getString("role"));
				return u;
			}
		});

		if (users.isEmpty()) {
			return null;
		} else if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<User> getAllUsers() {
		String sql = "SELECT * FROM users";
		List<User> users = namedParameterJdbcTemplate.query(sql, new RowMapper<User>() {

			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User u = new User();
				u.setUserId(rs.getInt("user_id"));
				u.setUsername(rs.getString("username"));
				u.setPassword(rs.getString("password"));
				u.setRole(rs.getString("role"));
				return u;
			}
		});

		if (users.isEmpty()) {
			return null;
		} else {
			return users;
		}
	}

	@Override
	public void insertUser(User user) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("username", user.getUsername());
		parameters.put("password", user.getPassword());
		parameters.put("role", user.getRole());
		String sql = "INSERT INTO users (username, password, role) VALUES(:username," + ":password, :role)";
		namedParameterJdbcTemplate.update(sql, parameters);

	}

	@Override
	public void updateUser(User user) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user_id", user.getUserId());
		parameters.put("password", user.getPassword());
		parameters.put("role", user.getRole());
		String sql = "UPDATE users SET password = :password, role = :role WHERE user_id = :user_id";
		namedParameterJdbcTemplate.update(sql, parameters);
	}

	@Override
	public void deleteUser(int userId) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user_id", userId);
		String sql = "DELETE FROM users WHERE user_id = :user_id";
		namedParameterJdbcTemplate.update(sql, parameters);
	}

}
