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
package org.n52.wps.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.dao.UserDAO;
import org.n52.wps.webapp.entities.User;

public class UserServiceImplTest {
	
	@InjectMocks
	private UserService userService;
	
	@Mock
	private UserDAO userDAO;
	
	@Before
	public void setup() {
		userService = new UserServiceImpl();
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void tearDown() {
		userDAO = null;
	}
	
	@Test
	public void getUser_validId() {
		when(userDAO.getUserById(4)).thenReturn(new User());
		User user = userService.getUser(4);
		assertNotNull(user);
	}
	
	@Test
	public void getUser_invalidId() {
		when(userDAO.getUserById(-99)).thenReturn(null);
		User user = userService.getUser(-99);
		assertNull(user);
	}
	
	@Test
	public void getUser_validUsername() {
		when(userDAO.getUserByUsername("username")).thenReturn(new User());
		User user = userService.getUser("username");
		assertNotNull(user);
	}
	
	@Test
	public void getUser_invalidUsername() {
		when(userDAO.getUserByUsername("invalid_username")).thenReturn(null);
		User user = userService.getUser("invalid_username");
		assertNull(user);
	}
	
	@Test
	public void getAllUsers() {
		List<User> users = new ArrayList<User>();
		users.add(new User());
		users.add(new User());
		when(userDAO.getAllUsers()).thenReturn(users);
		List<User> usersList = userService.getAllUsers();
		assertEquals(2, usersList.size());
	}
	
	@Test
	public void insertUser() {
		User user = new User();
		userService.insertUser(user);
		verify(userDAO).insertUser(user);
	}
	
	@Test
	public void updatetUser_existingUser() {
		User user = new User();
		user.setUserId(5);
		when(userDAO.getUserById(user.getUserId())).thenReturn(user);
		userService.updateUser(user);
		verify(userDAO).updateUser(user);
	}
	
	@Test
	public void updatetUser_nonExistingUser() {
		User user = new User();
		user.setUserId(5);
		when(userDAO.getUserById(user.getUserId())).thenReturn(null);
		userService.updateUser(user);
		verify(userDAO, never()).updateUser(user);
	}
	
	@Test
	public void deleteUser_existingUser() {
		when(userDAO.getUserById(4)).thenReturn(new User());
		userService.deleteUser(4);
		verify(userDAO).deleteUser(4);
	}
	
	@Test
	public void deleteUser_nonExistingUser() {
		when(userDAO.getUserById(4)).thenReturn(null);
		userService.deleteUser(4);
		verify(userDAO, never()).deleteUser(4);
	}
}
