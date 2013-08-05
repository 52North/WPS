<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<html lang="en">
<head>
<title><tiles:getAsString name="title" /></title>
<link href="<c:url value="/resources/css/bootstrap.css" />" rel="stylesheet" type="text/css" />
<link href="<c:url value="/resources/css/bootstrap-glyphicons.css" />" rel="stylesheet" type="text/css" />
</head>
<body>
	<script src="<c:url value="/resources/js/jquery-1.10.1.js" />"></script>
	<script src="<c:url value="/resources/js/bootstrap.js" />"></script>
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