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

import java.util.List;

import org.n52.wps.webapp.dao.UserDAO;
import org.n52.wps.webapp.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An implementation for the {@link UserService} interface. This implementation uses the {@link UserDAO} for database
 * operations.
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	private static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public User getUser(int userId) {
		return userDAO.getUserById(userId);
	}

	@Override
	public User getUser(String username) {
		return userDAO.getUserByUsername(username);
	}

	@Override
	public List<User> getAllUsers() {
		return userDAO.getAllUsers();
	}

	@Override
	public void insertUser(User user) {
		userDAO.insertUser(user);
		LOGGER.debug("New user '{}' with role '{}' has been inserted.", user.getUsername(), user.getRole());
	}

	@Override
	public void updateUser(User user) {
		User userToUpdate = userDAO.getUserById(user.getUserId());
		if (userToUpdate != null) {
			userDAO.updateUser(user);
			LOGGER.debug("New user '{}' with role '{}' has been updated.", user.getUsername(), user.getRole());
		}
	}

	@Override
	public void deleteUser(int userId) {
		User user = userDAO.getUserById(userId);
		if (user != null) {
			userDAO.deleteUser(userId);
			LOGGER.debug("New user '{}' with role '{}' has been deleted.", user.getUsername(), user.getRole());
		}
	}

}
