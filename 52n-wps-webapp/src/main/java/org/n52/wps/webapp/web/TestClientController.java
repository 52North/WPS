package org.n52.wps.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles the test client URI requests and mapping.
 */
@Controller
@RequestMapping("test_client")
public class TestClientController {

	/**
	 * Display the test client.
	 * 
	 * @return The test client view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String display() {
		return "test_client";
	}
}
