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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.wps.webapp.entities.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@TransactionConfiguration(defaultRollback = true)
public class JdbcUserDAOTest {
	
	private UserDAO userDAO;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private EmbeddedDatabaseBuilder builder;
	private EmbeddedDatabase db;

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		builder = new EmbeddedDatabaseBuilder();
		db = builder.setType(EmbeddedDatabaseType.HSQL)
				.addScript("db" + File.separator + "schema.sql")
				.addScript("test-data.sql")
				.build();
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(db);
		userDAO = new JdbcUserDAO();
		ReflectionTestUtils.setField(userDAO, "namedParameterJdbcTemplate", namedParameterJdbcTemplate);
	}
	
	@After
	public void tearDown() {
		db.shutdown();
	}
	
	@Test
	public void testGetUserById() {
		User user = userDAO.getUserById(1);
		assertEquals(1, user.getUserId());
		assertEquals("testUser1", user.getUsername());
		assertEquals("testPassword", user.getPassword());
		assertEquals("ROLE_ADMIN", user.getRole());
		
		User user2 = userDAO.getUserById(2);
		assertEquals(2, user2.getUserId());
		assertEquals("testUser2", user2.getUsername());
		assertEquals("testPassword", user2.getPassword());
		assertEquals("ROLE_USER", user2.getRole());
		
		User nullUser = userDAO.getUserById(3);
		assertNull(nullUser);
	}
	
	@Test
	public void testGetUserByUsername() {
		User user = userDAO.getUserByUsername("testUser1");
		assertEquals(1, user.getUserId());
		assertEquals("testUser1", user.getUsername());
		assertEquals("testPassword", user.getPassword());
		assertEquals("ROLE_ADMIN", user.getRole());
		
		User user2 = userDAO.getUserByUsername("testUser2");
		assertEquals(2, user2.getUserId());
		assertEquals("testUser2", user2.getUsername());
		assertEquals("testPassword", user2.getPassword());
		assertEquals("ROLE_USER", user2.getRole());
		
		User nullUser = userDAO.getUserByUsername("nonExistingUser");
		assertNull(nullUser);
	}
	
	@Test
	public void testGetAllUsers() {
		List<User> users = userDAO.getAllUsers();
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals(1, users.get(0).getUserId());
		assertEquals("testUser1", users.get(0).getUsername());
		assertEquals("testPassword", users.get(0).getPassword());
		assertEquals("ROLE_ADMIN", users.get(0).getRole());
		
		assertEquals(2, users.get(1).getUserId());
		assertEquals("testUser2", users.get(1).getUsername());
		assertEquals("testPassword", users.get(1).getPassword());
		assertEquals("ROLE_USER", users.get(1).getRole());
	}
	
	@Test
	public void testInsertUser() {
		User user = new User();
		user.setUsername("testUser3");
		user.setPassword("testPassword");
		user.setRole("USER_ADMIN");
		userDAO.insertUser(user);
		User addedUser = userDAO.getUserById(3);
		assertEquals(3, addedUser.getUserId());
		assertEquals("testUser3", addedUser.getUsername());
		assertEquals("testPassword", addedUser.getPassword());
		assertEquals("USER_ADMIN", addedUser.getRole());
	}
	
	@Test
	public void testInsertDuplicateUsername() {
		User user = new User();
		user.setUsername("testUser1");
		exception.expect(DataIntegrityViolationException.class);
		userDAO.insertUser(user);
	}
	
	@Test
	public void testUpdateUser() {
		User user = userDAO.getUserById(1);
		assertEquals("testPassword", user.getPassword());
		assertEquals("ROLE_ADMIN", user.getRole());
		user.setPassword("updatedPassword");
		user.setRole("ROLE_USER");
		userDAO.updateUser(user);
		user = userDAO.getUserById(1);
		assertEquals("updatedPassword", user.getPassword());
		assertEquals("ROLE_USER", user.getRole());
	}
	
	@Test
	public void testDeleteUser() {
		User user = userDAO.getUserById(2);
		assertNotNull(user);
		userDAO.deleteUser(2);
		user = userDAO.getUserById(2);
		assertNull(user);
	}
}
