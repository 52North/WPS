
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