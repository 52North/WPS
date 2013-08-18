package org.n52.wps.webapp.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebAppPropertiesListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String rootPath = sce.getServletContext().getRealPath("/");
		System.setProperty("webroot", rootPath);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}
