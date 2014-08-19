<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="well" style="padding: 8px 0;">
	<ul class="nav nav-list">
		<li class="nav-header">Configurations</li>
		<li class="server"><a href="<c:url value="/server" />">Server</a></li>
		<li class="repositories"><a href="<c:url value="/repositories" />">Repositories</a></li>
		<li class="generators"><a href="<c:url value="/generators" />">Generators</a></li>
		<li class="parsers"><a href="<c:url value="/parsers" />">Parsers</a></li>
		<li class="databases"><a href="<c:url value="/databases" />">Databases</a></li>
		<li class="nav-header">Settings</li>
		<li class="users"><a href="<c:url value="/users" />">Users</a></li>
		<li class="log"><a href="<c:url value="/log" />">Log</a></li>
		<li class="service_identification"><a href="<c:url value="/service_identification" />">Service Identification</a></li>
		<li class="service_provider"><a href="<c:url value="/service_provider" />">Service Provider</a></li>
		<li class="nav-header">Testing</li>
		<li class="test_client"><a href="<c:url value="/test_client" />">Test Client</a></li>
		<li class="divider"></li>
		<li class="backup"><a href="<c:url value="/backup" />">Backup &amp; Restore</a></li>
	</ul>
</div>
<c:set var="activeMenu" value="${requestScope['javax.servlet.forward.servlet_path']}" />
<script type="text/javascript">
	$(document).ready(function() {
		className = '${activeMenu}'.replace('/', '.');
		$(className).addClass('active');
	});
</script>
