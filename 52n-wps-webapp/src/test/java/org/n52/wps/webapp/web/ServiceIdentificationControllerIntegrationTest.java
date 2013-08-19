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
import org.n52.wps.webapp.common.AbstractIntegrationTest;
import org.n52.wps.webapp.dao.XmlCapabilitiesDAO;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ServiceIdentificationControllerIntegrationTest extends AbstractIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private ServiceIdentification module;
	
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
		RequestBuilder builder = get("/service_identification").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("service_identification"))
				.andExpect(model().attributeExists("configurationModule"));
	}

	@Test
	public void processPost_success() throws Exception {
		String path = resourcePathUtil.getWebAppResourcePath(XmlCapabilitiesDAO.FILE_NAME);
		Document originalDoc = jDomUtil.parse(path);
		
		RequestBuilder request = post("/service_identification").param("key", "title")
				.param("value", "Posted Title").param("module", module.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		assertEquals("Posted Title", module.getTitle());
		assertEquals("Posted Title", module.getConfigurationEntries().get(0).getValue());
		
		//reset document to original state
		jDomUtil.write(originalDoc, path);
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/service_identification").param("key", "title")
				.param("value", "").param("module", module.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
