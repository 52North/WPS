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
<div class="well" style="padding: 8px 0;">
	<ul class="nav nav-list">
		<li class="nav-header">Configurations</li>
		<li class="server"><a href="<c:url value="/server" />">Server</a></li>
		<li class="repositories"><a href="<c:url value="/repositories" />">Repositories</a></li>
		<li class="generators"><a href="<c:url value="/generators" />">Generators</a></li>
		<li class="parsers"><a href="<c:url value="/parsers" />">Parsers</a></li>
		<li class="databases"><a href="<c:url value="/databases" />">Databases</a></li>
		<li class="nav-header">Settings</li>
		<li class="users"><a href="<c:url value="/users" />">Users</a></li>
		<li class="log"><a href="<c:url value="/log" />">Log</a></li>
		<li class="service_identification"><a href="<c:url value="/service_identification" />">Service Identification</a></li>
		<li class="service_provider"><a href="<c:url value="/service_provider" />">Service Provider</a></li>
		<li class="nav-header">Testing</li>
		<li class="test_client"><a href="<c:url value="/test_client" />">Test Client</a></li>
		<li class="divider"></li>
		<li class="backup"><a href="<c:url value="/backup" />">Backup &amp; Restore</a></li>
	</ul>
</div>
<c:set var="activeMenu" value="${requestScope['javax.servlet.forward.servlet_path']}" />
<script type="text/javascript">
	$(document).ready(function() {
		className = '${activeMenu}'.replace('/', '.');
		$(className).addClass('active');
	});
</script>
