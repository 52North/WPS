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
<table id="${fullClassName}" class="table table-bordered table-striped table-hover">
	<colgroup>
		<col class="col-lg-6">
		<col class="col-lg-4">
		<col class="col-lg-1">
		<col class="col-lg-1">
	</colgroup>
	<thead>
		<tr>
			<th>Username</th>
			<th>Role</th>
			<th>Edit</th>
			<th>Delete</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="user" items="${users}">
			<tr>
				<td>${user.username}</td>
				<c:set var="role" value="${user.role eq 'ROLE_ADMIN' ? 'Admin' : 'User'}" />
				<td>${role}</td>
				<td><a id="editUser" class="btn btn-default btn-mini" href="<c:url value="/users/${user.userId}/edit" />">Edit</a>
				</td>
				<td><a id="deleteUser" class="btn btn-danger btn-mini" href="<c:url value="/users/${user.userId}/delete" />">Delete</a>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<a href="<c:url value="/users/add_user" />" class="btn btn-primary btn-small">Add User</a>
<script>
	$('a#deleteUser').click(function(event) {
		event.preventDefault();
		var a = $(this);
		var row = a.parents("tr");
		var url = a.attr('href');
		$.ajax({
			type : "POST",
			url : url,
			success : function() {
				(row).remove();
				alertMessage("", "User deleted", "alert alert-success", a);
			},
			error : function(textStatus, errorThrown) {
				alertMessage("Error: ", "Unable to delete user", "alert alert-danger", a);
			}
		});
	});
</script>