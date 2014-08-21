<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags"%>

<p class="text-danger">${error}</p>
<form:form id="customForm" modelAttribute="user" method="POST" action="add_user" class="form-horizontal">
	<form:hidden path="userId" />
	<input:customInput label="Username" field="username" type="text" />
	<input:customInput label="Password" field="password" type="password" />
	<div class="form-group">
		<form:label class="col-lg-3 control-label" path="role">Role</form:label>
		<div class="col-lg-6">
			<form:select class="form-control" path="role">
				<form:option value="ROLE_ADMIN">Admin</form:option>
				<form:option value="ROLE_USER">User</form:option>
			</form:select>
		</div>
	</div>
	<input:customInput label="Add" type="submit" />
</form:form>
<script src="<c:url value="/static/js/custom.module.js" />"></script>