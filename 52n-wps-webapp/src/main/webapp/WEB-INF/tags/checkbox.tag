<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ attribute name="field" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="val" required="true" %>

<div class="checkbox">
	<label>
		<form:checkbox path="${field}" value="${val}" />
		${label}
	</label>
</div>