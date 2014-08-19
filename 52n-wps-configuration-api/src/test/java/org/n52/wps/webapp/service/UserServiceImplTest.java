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
