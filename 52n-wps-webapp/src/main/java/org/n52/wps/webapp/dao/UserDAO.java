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

import java.util.List;

import org.n52.wps.webapp.entities.User;

public interface UserDAO {

	/**
	 * Get user by user id
	 * 
	 * @param userId
	 *            the id of the user
	 * @return The user specified by the id
	 */
	User getUserById(int userId);

	/**
	 * Get user by username
	 * 
	 * @param username
	 *            the username of the user
	 * @return The user specified by the username
	 */
	User getUserByUsername(String username);

	/**
	 * Get all users
	 * 
	 * @return The list of all users
	 */
	List<User> getAllUsers();

	/**
	 * Insert new user
	 * 
	 * @param user
	 */
	void insertUser(User user);

	/**
	 * Update existing user
	 * 
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * Delete user
	 * 
	 * @param userId
	 *            the id of the user to be deleted
	 */
	void deleteUser(int userId);
}
