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
import org.n52.wps.webapp.dao.XmlLogConfigurationsDAO;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class LogConfigurationsControllerIntegrationTest extends AbstractIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	ConfigurationManager configurationManager;
	
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
		RequestBuilder builder = get("/log").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("log"))
				.andExpect(model().attributeExists("logConfigurations"));
	}

	@Test
	public void processPost_success() throws Exception {
		String path = resourcePathUtil.getClassPathResourcePath(XmlLogConfigurationsDAO.FILE_NAME);
		Document originalDoc = jDomUtil.parse(path);
		
		RequestBuilder request = post("/log")
				.param("wpsfileAppenderFileNamePattern", "testFileAppenderFileNamePattern")
		.param("wpsfileAppenderEncoderPattern", "testFileAppenderFileNamePattern")
		.param("wpsconsoleEncoderPattern", "testFileAppenderFileNamePattern")
		.param("wpsfileAppenderMaxHistory", "10")
		.param("rootLevel", "DEBUG")
		.param("fileAppenderEnabled", "true")
		.param("consoleAppenderEnabled", "true")
		.param("loggers['org.apache.axiom']", "ERROR")
		.param("loggers['org.apache.http.wire']", "OFF");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		LogConfigurations logConfigurations = configurationManager.getLogConfigurationsServices().getLogConfigurations();
		assertEquals("testFileAppenderFileNamePattern", logConfigurations.getWpsfileAppenderFileNamePattern());
		
		//reset document to original state
		jDomUtil.write(originalDoc, path);
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/log").param("wpsfileAppenderFileNamePattern", "");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
