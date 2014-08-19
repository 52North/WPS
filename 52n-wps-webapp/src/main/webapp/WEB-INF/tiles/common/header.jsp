<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="navbar navbar-inverse navbar-static-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="navbar-brand" href="<c:url value="/" />">52 North</a>
			<ul class="nav navbar-nav">
				<li><a href="<c:url value="/" />">Home</a></li>
				<li><a href="<c:url value="http://52north.org/communities/geoprocessing/"/>" target="blank">Community</a></li>
				<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">Resources <b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a
							href="<c:url value="https://bugzilla.52north.org/describecomponents.cgi?product=52N%20Web%20Processing"/>" target="blank">Bugzilla</a></li>
						<li><a href="<c:url value="http://52north.org/communities/geoprocessing/mail-lists.html"/>" target="blank">Mailing Lists</a></li>
						<li><a href="<c:url value="http://geoprocessing.forum.52north.org/"/>" target="blank">Forum</a></li>
						<li><a href="<c:url value="https://github.com/52North/WPS"/>" target="blank">Source</a></li>
					</ul></li>
			</ul>
			<div class="pull-right">
				<security:authorize access="isAuthenticated()">
					<div class="dropdown">
						<a href="#" class="btn btn-primary btn-small navbar-btn dropdown-toggle" data-toggle="dropdown"><security:authentication
								property="principal.username" /> <b class="caret"></b></a>
						<ul class="dropdown-menu">
							<li><a href="change_password">Change Password</a></li>
							<li><a href="j_spring_security_logout">Logout</a></li>
						</ul>
					</div>
				</security:authorize>
			</div>
			<security:authorize access="isAnonymous()">
				<form class="navbar-form pull-right" action="j_spring_security_check" method="POST">
					<input type="text" name="j_username" class="form-control" style="width: 160px;" placeholder="Username..." />
					<input type="password" name="j_password" class="form-control" style="width: 160px;" placeholder="Password..." />
					<label class="checkbox-inline text-muted">
						<input type="checkbox" name="_spring_security_remember_me">
						Remember Me
					</label>
					<button type="submit" class="btn btn-primary btn-small">Login</button>
				</form>
			</security:authorize>
		</div>
	</div>
</div>