package org.n52.wps.webapp.web;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractIntegrationTest;
import org.n52.wps.webapp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UsersControllerIntegrationTest extends AbstractIntegrationTest {
	private MockMvc mockMvc;

	@Autowired
	ConfigurationManager configurationManager;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void getUsers() throws Exception {
		RequestBuilder request = get("/users").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("users")).andExpect(model().attributeExists("users"));
	}

	@Test
	public void getChangePasswordForm() throws Exception {
		RequestBuilder request = get("/change_password").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("change_password"));
	}

	@Test
	public void processChangePasswordForm_matchingCurrentPassword() throws Exception {
		Authentication user = new UsernamePasswordAuthenticationToken("testUser1", "testPassword");
		RequestBuilder request = post("/change_password").param("currentPassword", "testPassword")
				.param("newPassword", "newPassword").principal(user);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isMovedTemporarily()).andExpect(view().name("redirect:/"));
	}

	@Test
	public void processChangePasswordForm_notMatchingCurrentPassword() throws Exception {
		Authentication user = new UsernamePasswordAuthenticationToken("testUser1", "testPassword");
		RequestBuilder request = post("/change_password").param("currentPassword", "testPassword55")
				.param("newPassword", "newPassword").principal(user);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("change_password"))
				.andExpect(model().attributeExists("error"));
	}

	@Test
	public void processChangePasswordForm_emptyNewPassword() throws Exception {
		Authentication user = new UsernamePasswordAuthenticationToken("testUser2", "testPassword");
		RequestBuilder request = post("/change_password").param("currentPassword", "testPassword")
				.param("newPassword", "").principal(user);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("change_password"))
				.andExpect(model().attributeExists("newPasswordError"));
	}

	@Test
	public void getEditUserForm() throws Exception {
		RequestBuilder request = get("/users/{userId}/edit", 1).accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("edit_user"))
				.andExpect(model().attribute("user", hasProperty("username", is("testUser1"))));
	}

	@Test
	public void processEditUserForm() throws Exception {
		User user = configurationManager.getUserServices().getUser(1);
		assertEquals("ROLE_ADMIN", user.getRole());
		RequestBuilder request = post("/users/{userId}/edit", user.getUserId()).param("password", user.getPassword())
				.param("username", user.getUsername()).param("role", "ROLE_USER").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isMovedTemporarily()).andExpect(view().name("redirect:/users"));
		user = configurationManager.getUserServices().getUser(1);
		assertEquals("ROLE_USER", user.getRole());
	}

	@Test
	public void deleteUser() throws Exception {
		User user = configurationManager.getUserServices().getUser(1);
		assertNotNull(user);
		RequestBuilder request = post("/users/{userId}/delete", user.getUserId()).accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		user = configurationManager.getUserServices().getUser(1);
		assertNull(user);
	}

	@Test
	public void getAddUserForm() throws Exception {
		RequestBuilder request = get("/users/add_user").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk()).andExpect(view().name("add_user")).andExpect(model().attributeExists("user"));
	}

	@Test
	public void processAddUserForm_success() throws Exception {
		User user = configurationManager.getUserServices().getUser(3);
		assertNull(user);
		RequestBuilder request = post("/users/add_user").param("username", "testUser3")
				.param("password", "testPassword3").param("role", "ROLE_USER");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		user = configurationManager.getUserServices().getUser(3);
		assertEquals(3, user.getUserId());
		assertEquals("testUser3", user.getUsername());
		assertEquals("ROLE_USER", user.getRole());
	}

	@Test
	public void processAddUserForm_failure() throws Exception {
		RequestBuilder request = post("/users/add_user").param("username", "testUser3").param("password", "")
				.param("role", "ROLE_USER");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
