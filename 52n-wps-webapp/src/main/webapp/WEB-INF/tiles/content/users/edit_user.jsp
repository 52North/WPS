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