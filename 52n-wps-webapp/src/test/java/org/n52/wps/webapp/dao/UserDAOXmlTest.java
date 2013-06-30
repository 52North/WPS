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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.common.AbstracTest;
import org.n52.wps.webapp.entities.User;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDAOXmlTest extends AbstracTest {

	@Autowired
	@InjectMocks
	private UserDAO userDAO;

	@Autowired
	private ResourcePathUtil resourcePathUtil;
	
	@Mock
	JDomUtil jDomUtil;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetUserByUsername() throws JDOMException, IOException {
		String username = "TestUserName194*/";
		String absolutePath = resourcePathUtil.getClassPathResourcePath(UserDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(createTestDocument());
		User user = userDAO.getUser(username);
		assertNotNull(user);
		assertEquals(user.getUsername(), "TestUserName194*/");
	}
	
	@Test
	public void testNonExistingUserByUsername() throws JDOMException, IOException {
		String username = "NonExistingUserName";
		String absolutePath = resourcePathUtil.getClassPathResourcePath(UserDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(createTestDocument());
		User user = userDAO.getUser(username);
		assertNull(user);
	}
	
	@Test
	public void testGetAllUsers() throws JDOMException, IOException {
		String absolutePath = resourcePathUtil.getClassPathResourcePath(UserDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(createTestDocument());
		List<User> usersList = userDAO.getAllUsers();
		assertNotNull(usersList);
		assertEquals(usersList.get(0).getUsername(), "TestUserName194*/");
	}
	
	@Test
	public void testSaveUser() throws JDOMException, IOException {
		//create a test user
		User user = new User();
		user.setUsername("newUserName");
		user.setPassword("newPassword");
		
		//create a test document
		Document document = createTestDocument();
		Element root = document.getRootElement();
		
		//pass test document to userDAOXml
		String absolutePath = resourcePathUtil.getClassPathResourcePath(UserDAOXml.FILE_NAME);
		when(jDomUtil.load(absolutePath)).thenReturn(document);
		userDAO.save(user);
		
		//assert that save method correctly append new user to document
		@SuppressWarnings("unchecked")
		List<Element> children = root.getChildren();
		Element newElement = children.get(children.size() - 1);
		assertTrue(children.size() == 2);
		assertEquals(newElement.getAttributeValue("username"), user.getUsername());
		verify(jDomUtil).write(document, absolutePath);
	}
	
	private Document createTestDocument() {
		Document document = new Document();
		Element root = new Element("UserRepository");
		Element userElement = new Element("User");
		userElement.setAttribute("username", "TestUserName194*/");
		root.addContent(userElement);
		document.setRootElement(root);
		return document;
	}
}
