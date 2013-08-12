package org.n52.wps.webapp.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {
    @RequestMapping(value={"/error", "/404"})
    public String handle(HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", request.getAttribute("javax.servlet.error.status_code"));
        model.addAttribute("errorMessage", request.getAttribute("javax.servlet.error.message"));
        return "error";
    }
}
