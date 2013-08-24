<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ attribute name="field" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="desc" required="false" %>
<%@ attribute name="required" required="false" %>

<div class="form-group">
	<form:label class="col-lg-3 control-label" path="${field}">${label}</form:label>
	<div class="col-lg-6">
		<form:input class="form-control" type="text" path="${field}" />
		<span class="help-block">${desc}</span>
	</div>
	<c:if test="${empty required || required eq 'true'}">
		<div class="col-log-1">
			<span class="label label-danger">Required</span>
		</div>
	</c:if>
</div>
