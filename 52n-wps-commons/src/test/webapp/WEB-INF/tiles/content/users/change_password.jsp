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
<p class="text-danger">${error}</p>
<form id="changePassword" class="form-horizontal" action="change_password" method="POST">
	<div class="form-group">
		<div class="col-lg-4">
			<label>Username</label>
			<div class="form-control-static">
				<security:authentication property="principal.username" />
			</div>
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>Current Password</label>
			<input type="password" class="form-control" name="currentPassword" class="form-control"
				placeholder="Current Password..." />
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>New Password</label>
			<input type="password" class="form-control" name="newPassword" class="form-control" placeholder="New Password..." />
			<div class="text-danger">${newPasswordError}</div>
		</div>
	</div>
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
	<button type="submit" class="btn btn-primary">Change</button>
</form>