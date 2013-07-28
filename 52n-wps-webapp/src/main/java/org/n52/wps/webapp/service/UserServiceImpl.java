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

import java.util.List;

import org.n52.wps.webapp.dao.UserDAO;
import org.n52.wps.webapp.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
		LOGGER.debug("New user '" + user.getUsername() + "' with role '" + user.getRole() + "' has been inserted.");
	}

	@Override
	public void updateUser(User user) {
		User userToUpdate = userDAO.getUserById(user.getUserId());
		if (userToUpdate != null) {
			userDAO.updateUser(user);
			LOGGER.debug("User '" + user.getUsername() + "' with role '" + user.getRole() + "' has been updated.");
		}
	}

	@Override
	public void deleteUser(int userId) {
		User user = userDAO.getUserById(userId);
		if (user != null) {
			userDAO.deleteUser(userId);
			LOGGER.debug("User '" + user.getUsername() + "' with role '" + user.getRole() + "' has been deleted.");
		}
	}

}
