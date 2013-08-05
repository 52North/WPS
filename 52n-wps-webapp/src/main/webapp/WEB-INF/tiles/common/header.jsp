<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="navbar navbar-inverse navbar-static-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="navbar-brand" href="#">52 North</a>
			<ul class="nav navbar-nav">
				<li class="active"><a href="<c:url value="/" />">Home</a></li>
				<li><a href="#">Link</a></li>
				<li><a href="#">Link</a></li>
			</ul>
			<div class="pull-right">
				<security:authorize access="isAuthenticated()">
					<a href="j_spring_security_logout" class="btn btn-danger btn-small navbar-btn">Logout</a>
				</security:authorize>
			</div>
			<security:authorize access="isAnonymous()">
				<form class="navbar-form pull-right" action="j_spring_security_check" method="POST">
					<input type="text" name="j_username" class="form-control" style="width: 160px;" placeholder="Username..." />
					<input type="password" name="j_password" class="form-control" style="width: 160px;" placeholder="Password..." />
						<label class="checkbox-inline text-muted"> 
						<input type="checkbox" name="_spring_security_remember_me">Remember Me
						</label>
					<button type="submit" class="btn btn-primary btn-small">Login</button>
				</form>
			</security:authorize>
		</div>
	</div>
</div>