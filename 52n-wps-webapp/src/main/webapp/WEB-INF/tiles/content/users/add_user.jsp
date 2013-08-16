<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<p class="text-danger">${error}</p>
<form:form modelAttribute="user" method="POST" action="add_user">
	<form:hidden path="userId" />
	<div class="form-group">
		<form:label class="control-label" path="username">Username</form:label>
		<form:input class="form-control" path="username" />
		<form:errors path="username" cssClass="text-danger" element="div" />
	</div>
	<div class="form-group">
		<form:label class="control-label" path="password">Password</form:label>
		<form:input class="form-control" type="password" path="password" />
		<form:errors path="password" cssClass="text-danger" element="div" />
	</div>
	<div class="form-group">
		<form:label class="control-label" path="role">Role</form:label>
		<form:select class="form-control" path="role">
			<form:option value="ROLE_ADMIN">Admin</form:option>
			<form:option value="ROLE_USER">User</form:option>
		</form:select>
	</div>
	<div class="form-group">
		<button type="submit" class="btn btn-primary">Add</button>
	</div>
</form:form>