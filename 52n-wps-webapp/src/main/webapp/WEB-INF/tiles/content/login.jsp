<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:if test="${not empty param.login_error}">
	<p class="text-danger">
		Incorrect username or password, please try again.<br />
	</p>
</c:if>
<form class="form-horizontal" action="j_spring_security_check" method="POST">
	<div class="form-group">
		<div class="col-lg-4">
			<label>Username</label>
			<input type="text" class="col-lg-2 form-control" name="j_username" placeholder="Username..." />
		</div>
	</div>
	<div class="form-group">
		<div class="col-lg-4">
			<label>Password</label>
			<input type="password" class="form-control" name="j_password" class="form-control" placeholder="Password..." />
		</div>
	</div>
	<div class="checkbox">
		<label>
			<input type="checkbox" name="_spring_security_remember_me">
			Remember Me
		</label>
	</div>
	<button type="submit" class="btn btn-default">Login</button>
</form>