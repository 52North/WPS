package org.n52.wps.webapp.web;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jdom.Document;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractIntegrationTest;
import org.n52.wps.webapp.dao.XmlCapabilitiesDAO;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ServiceProviderControllerIntegrationTest extends AbstractIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private ConfigurationManager configurationManager;
	
	@Autowired
	private JDomUtil jDomUtil;
	
	@Autowired
	private ResourcePathUtil resourcePathUtil;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void display() throws Exception {
		RequestBuilder builder = get("/service_provider").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("service_provider"))
				.andExpect(model().attributeExists("serviceProvider"));
	}

	@Test
	public void processPost_success() throws Exception {
		String path = resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME);
		Document originalDoc = jDomUtil.parse(path);
		
		RequestBuilder request = post("/service_provider")
				.param("providerName", "providerName")
				.param("providerSite", "providerSite")
				.param("individualName", "individualName")
				.param("position", "position")
				.param("phone", "phone")
				.param("facsimile", "facsimile")
				.param("email", "email")
				.param("deliveryPoint", "deliveryPoint")
				.param("city", "city")
				.param("administrativeArea", "administrativeArea")
				.param("postalCode", "postalCode")
				.param("country", "country");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		ServiceProvider serviceProvider = configurationManager.getCapabilitiesServices().getServiceProvider();
		assertEquals("providerName", serviceProvider.getProviderName());
		
		//reset document to original state
		jDomUtil.write(originalDoc, path);
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/service_provider")
				.param("providerName", "")
				.param("providerSite", "providerSite")
				.param("individualName", "individualName")
				.param("position", "position")
				.param("phone", "phone")
				.param("facsimile", "facsimile")
				.param("email", "email")
				.param("deliveryPoint", "deliveryPoint")
				.param("city", "city")
				.param("administrativeArea", "administrativeArea")
				.param("postalCode", "postalCode")
				.param("country", "country");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
