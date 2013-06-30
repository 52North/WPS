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
	 * Retrieve the user from the underlining datastore
	 * @param username
	 * @return {@code User} instance
	 */
	User getUser(String username);
	
	/**
	 * Retrieve all users from the underling datastore
	 * @return list of all users
	 */
	List<User> getAllUsers();
	
	/**
	 * Save user to the underlining datastore
	 * @param {{@code User} instance
	 */
	void save(User user);
}
