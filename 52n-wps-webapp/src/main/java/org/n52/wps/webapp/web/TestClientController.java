package org.n52.wps.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("test_client")
public class TestClientController {
	@RequestMapping(method = RequestMethod.GET)
	public String display() {
		return "test_client";
	}
}
