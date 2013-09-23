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
