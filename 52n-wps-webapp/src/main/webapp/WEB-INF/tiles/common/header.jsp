<%--

    Copyright (C) 2012-2015 52°North Initiative for Geospatial Open Source
    Software GmbH

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License version 2 as published
    by the Free Software Foundation.

    If the program is linked with libraries which are licensed under one of
    the following licenses, the combination of the program with the linked
    library is not considered a "derivative work" of the program:

        - Apache License, version 2.0
        - Apache Software License, version 1.0
        - GNU Lesser General Public License, version 3
        - Mozilla Public License, versions 1.0, 1.1 and 2.0
        - Common Development and Distribution License (CDDL), version 1.0

    Therefore the distribution of the program linked with libraries licensed
    under the aforementioned licenses, is permitted by the copyright holders
    if the distribution is compliant with both the GNU General Public
    License version 2 and the aforementioned licenses.

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

--%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="navbar navbar-inverse navbar-static-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="navbar-brand" href="http://52north.org">52&deg;North</a>
			<ul class="nav navbar-nav">
				<li><a href="<c:url value="/" />">Home</a></li>
				<li><a href="<c:url value="http://52north.org/communities/geoprocessing/"/>" target="blank">Community</a></li>
				<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">Resources <b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a
							href="<c:url value="https://bugzilla.52north.org/describecomponents.cgi?product=52N%20Web%20Processing"/>" target="blank">Bugzilla</a></li>
						<li><a href="<c:url value="http://52north.org/communities/geoprocessing/mail-lists.html"/>" target="blank">Mailing Lists</a></li>
						<li><a href="<c:url value="http://geoprocessing.forum.52north.org/"/>" target="blank">Forum</a></li>
						<li><a href="<c:url value="https://github.com/52North/WPS"/>" target="blank">Source</a></li>
					</ul></li>
			</ul>
			<div class="pull-right">
				<security:authorize access="isAuthenticated()">
					<div class="dropdown">
						<a href="#" class="btn btn-primary btn-small navbar-btn dropdown-toggle" data-toggle="dropdown"><security:authentication
								property="principal.username" /> <b class="caret"></b></a>
						<ul class="dropdown-menu">
							<li><a href="<c:url value="/" />change_password">Change Password</a></li>
							<li><a href="j_spring_security_logout">Logout</a></li>
						</ul>
					</div>
				</security:authorize>
			</div>
			<security:authorize access="isAnonymous()">
				<form class="navbar-form pull-right" action="j_spring_security_check" method="POST">
					<input type="text" name="username" class="form-control" style="width: 160px;" placeholder="Username..." />
					<input type="password" name="password" class="form-control" style="width: 160px;" placeholder="Password..." />
					<label class="checkbox-inline text-muted">
						<input type="checkbox" name="_spring_security_remember_me">
						Remember Me
					</label>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
					<button type="submit" class="btn btn-primary btn-small">Login</button>
				</form>
			</security:authorize>
		</div>
	</div>
</div>