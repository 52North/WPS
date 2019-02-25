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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:if test="${not empty param.login_error}">
	<p class="text-danger">
		Incorrect username or password, please try again.<br />
	</p>
</c:if>
<form class="form-horizontal" action="j_spring_security_check" method="POST">
	<div class="form-group">
		<div class="col-lg-4">
			<label>Username</label>
			<input type="text" class="col-lg-2 form-control" name="username" placeholder="Username..." />
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>Password</label>
			<input type="password" class="form-control" name="password" class="form-control" placeholder="Password..." />
		</div>
	</div>
	<div class="checkbox">
		<label>
			<input type="checkbox" name="_spring_security_remember_me">
			Remember Me
		</label>
	</div>
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/> 
	<button type="submit" class="btn btn-default">Login</button>
</form>