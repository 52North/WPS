<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<p class="text-danger">${error}</p>
<form id="changePassword" class="form-horizontal" action="change_password" method="POST">
	<div class="form-group">
		<div class="col-lg-4">
			<label>Username</label>
			<div class="form-control-static">
				<security:authentication property="principal.username" />
			</div>
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>Current Password</label>
			<input type="password" class="form-control" name="currentPassword" class="form-control"
				placeholder="Current Password..." />
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>New Password</label>
			<input type="password" class="form-control" name="newPassword" class="form-control" placeholder="New Password..." />
			<div class="text-danger">${newPasswordError}</div>
		</div>
	</div>
	<button type="submit" class="btn btn-primary">Change</button>
</form>