<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="text/plain" pageEncoding="UTF-8"%>
<%@ page import="org.n52.wps.server.r.R_Config"%><%@ page
	isErrorPage="false"%>
<%=R_Config.getInstance().getCurrentSessionInfo()%>