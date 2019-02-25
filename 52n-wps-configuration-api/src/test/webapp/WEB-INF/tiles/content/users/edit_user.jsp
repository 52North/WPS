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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<p class="text-danger">${error}</p>
<form:form modelAttribute="user" method="POST" action="edit">
	<form:hidden path="userId" />
	<div class="form-group">
		<form:label class="control-label" path="username">Username</form:label>
		<p class="form-control-static">${user.username}</p>
	</div>
	<form:input class="form-control" type="hidden" path="password" />
	<div class="form-group">
		<form:label class="control-label" path="role">Role</form:label>
		<form:select class="form-control" path="role">
			<form:option value="ROLE_ADMIN">Admin</form:option>
			<form:option value="ROLE_USER">User</form:option>
		</form:select>
	</div>
	<div class="form-group">
		<button type="submit" class="btn btn-primary">Update</button>
	</div>
</form:form>