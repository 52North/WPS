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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ attribute name="field" required="false"%>
<%@ attribute name="label" required="true"%>
<%@ attribute name="desc" required="false"%>
<%@ attribute name="required" required="false"%>
<%@ attribute name="type" required="false"%>
<%@ attribute name="val" required="false"%>

<c:choose>
	<c:when test="${type == 'text' or empty type or type == 'password'}">
		<div class="form-group">
			<form:label class="col-lg-3 control-label" path="${field}">${label}</form:label>
			<div class="col-lg-6">
				<form:input class="form-control" type="${type}" path="${field}" />
				<span class="help-block">${desc}</span>
			</div>
			<c:if test="${empty required || required eq 'true'}">
				<div class="col-log-1">
					<span class="label label-danger">Required</span>
				</div>
			</c:if>
		</div>
	</c:when>
	<c:when test="${type == 'checkbox'}">
		<div class="checkbox">
			<label>
				<form:checkbox path="${field}" value="${val}" />
				${label}
			</label>
		</div>
	</c:when>
	<c:when test="${type == 'submit'}">
		<div class="form-group">
			<div class="col-lg-offset-3 col-lg-8">
				<button type="submit" class="btn btn-primary">${label}</button>
			</div>
		</div>
	</c:when>
</c:choose>
