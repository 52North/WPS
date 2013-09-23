package org.n52.wps.webapp.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Maps error URIs and codes to a default error page.
 */
@Controller
public class ErrorController {
	@RequestMapping(value = { "/error", "/404", "/403" })
	public String handle(HttpServletRequest request, Model model) {
		model.addAttribute("errorCode", request.getAttribute("javax.servlet.error.status_code"));
		model.addAttribute("errorMessage", request.getAttribute("javax.servlet.error.message"));
		return "error";
	}
}
