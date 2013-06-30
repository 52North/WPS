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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.n52.wps.webapp.entities.User;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOXml implements UserDAO {

	@Autowired
	JDomUtil jDomUtil;
	
	@Autowired
	ResourcePathUtil resourcePathUtil;
	
	public static String FILE_NAME = "users.xml";
	
	private static Logger LOGGER = Logger.getLogger(UserDAOXml.class);
	
	@Override
	public User getUser(String username) {
		User user = null;
		try {
			String absolutePath = resourcePathUtil.getClassPathResourcePath(FILE_NAME);
			Document document = jDomUtil.load(absolutePath);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> children = root.getChildren();
			Element userElement = null;
			for (Element element : children) {
				if (element.getAttributeValue("username").equals(username)) {
					userElement = element;
				}
			}
			if (userElement != null ) {
				user = new User();
				user.setUsername(userElement.getAttributeValue("username"));
				user.setPassword(userElement.getAttributeValue("password"));
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return user;
	}

	@Override
	public List<User> getAllUsers() {
		List<User> usersList = new ArrayList<User>();
		
		try {
			String absolutePath = resourcePathUtil.getClassPathResourcePath(FILE_NAME);
			Document document = jDomUtil.load(absolutePath);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> children = root.getChildren();
			for (Element element : children) {
				User user = new User();
				user.setUsername(element.getAttributeValue("username"));
				user.setPassword(element.getAttributeValue("password"));
				usersList.add(user);
			}
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return usersList;
	}

	@Override
	public void save(User user) {
		
		try {
			String absolutePath = resourcePathUtil.getClassPathResourcePath(FILE_NAME);
			Document document = jDomUtil.load(absolutePath);
			Element root = document.getRootElement();
			Element userElement = new Element("User");
			userElement.setAttribute("username", user.getUsername());
			userElement.setAttribute("password", user.getPassword());
			root.addContent(userElement);
			jDomUtil.write(document, absolutePath);
		} catch (JDOMException | IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
