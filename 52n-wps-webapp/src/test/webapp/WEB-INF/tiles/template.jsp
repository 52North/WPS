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
<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<html lang="en">
<head>
<title><tiles:getAsString name="title" /></title>
<link href="<c:url value="/static/css/bootstrap.css" />" rel="stylesheet" type="text/css" />
<link href="<c:url value="/static/css/bootstrap-glyphicons.css" />" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/52n.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/application.css" />" type="text/css" />
<!--[if IE]><link rel="shortcut icon" href="<c:url value="/static/favicon.ico" />"><![endif]-->
<link rel="icon" href="<c:url value="/static/favicon.ico" />">
<meta name="csrf_token" content="${_csrf.token}">
</head>
<body>
	<script src="<c:url value="/static/js/library/jquery-1.10.1.js" />"></script>
	<script src="<c:url value="/static/js/library/bootstrap.js" />"></script>
	<script src="<c:url value="/static/js/commonjs.js" />"></script>

	<tiles:insertAttribute name="header" />
	<div class="container">
		<div class="row">
			<div class="col-lg-3">
				<tiles:insertAttribute name="sidebar" />
			</div>
			<div class="col-lg-9">
				<div class="page-header">
					<h2>
						<tiles:getAsString name="pageHeader" />
					</h2>
					<tiles:getAsString name="description" />
				</div>
				<tiles:insertAttribute name="body" />
			</div>
		</div>
	</div>
	<tiles:insertAttribute name="footer" />
</body>
</html>