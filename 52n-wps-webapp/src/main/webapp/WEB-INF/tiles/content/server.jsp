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

<c:set var="moduleName" value="${configurationModule.moduleName}" />
<c:set var="fullClassName" value="${configurationModule['class'].name}" />
<c:set var="simpleClassName" value="${configurationModule['class'].simpleName}" />


<%-- A module form --%>
<form id="standardModule" class="form-horizontal" method="POST" action="<c:url value="/server" />">

	<%--  Create an input for each configuration entry --%>
	<c:forEach var="configurationEntry" items="${configurationModule.configurationEntries}">
		<div id="${configurationEntry.key}" class="form-group">

			<%-- If the entry is boolean, display a checkbox, else, display a text input --%>
			<c:choose>
				<c:when test="${configurationEntry.type eq 'BOOLEAN'}">
					<div class="col-lg-offset-3 col-lg-7">
						<div class="checkbox">
							<label>
								<input name="value" id='valueHidden' type='hidden' value='false'>
								<input name="value" type="checkbox" value="true" <c:if test="${configurationEntry.value}">checked</c:if> />
								${configurationEntry.title}
							</label>
							<span class="help-block"> ${configurationEntry.description} </span>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<label class="col-lg-3 control-label">${configurationEntry.title}</label>
					<div class="col-lg-7">
						<input name="value" class="form-control" type="text" value="${configurationEntry.value}" />
						<span class="help-block"> ${configurationEntry.description} </span>
					</div>
				</c:otherwise>
			</c:choose>


			<%-- Hidden fields to identify and process the entry and module --%>
			<input name="key" type="hidden" value="${configurationEntry.key}" />
			<input name="module" type="hidden" value="${fullClassName}" />

			<%-- Only show the required label if the field is required and is not a boolean (checkbox) --%>
			<c:if test="${configurationEntry.required and configurationEntry.type ne 'BOOLEAN'}">
				<span class="label label-danger">Required</span>
			</c:if>
		</div>
	</c:forEach>

	<%-- Save button --%>
	<div class="form-group">
		<div class="col-lg-offset-3 col-lg-8">
			<button type="submit" class="btn btn-primary">Save</button>
		</div>
	</div>
</form>

<script src="<c:url value="/static/js/standard.module.js" />"></script>