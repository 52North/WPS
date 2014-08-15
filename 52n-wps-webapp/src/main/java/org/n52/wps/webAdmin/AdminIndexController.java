package org.n52.wps.webAdmin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Daniel Nüst
 *
 */
@Controller
public class AdminIndexController {

    @RequestMapping("/webAdmin")
    public String adminPage(ModelMap model) {
        return "webadmin.jsp";
    }
}
