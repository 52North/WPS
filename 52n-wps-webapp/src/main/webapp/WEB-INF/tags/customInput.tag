<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ attribute name="field" required="false"%>
<%@ attribute name="label" required="true"%>
<%@ attribute name="desc" required="false"%>
<%@ attribute name="required" required="false"%>
<%@ attribute name="type" required="false"%>
<%@ attribute name="val" required="false"%>

<c:choose>
	<c:when test="${type == 'text' or empty type or type == 'password'}">
		<div class="form-group">
			<form:label class="col-lg-3 control-label" path="${field}">${label}</form:label>
			<div class="col-lg-6">
				<form:input class="form-control" type="${type}" path="${field}" />
				<span class="help-block">${desc}</span>
			</div>
			<c:if test="${empty required || required eq 'true'}">
				<div class="col-log-1">
					<span class="label label-danger">Required</span>
				</div>
			</c:if>
		</div>
	</c:when>
	<c:when test="${type == 'checkbox'}">
		<div class="checkbox">
			<label>
				<form:checkbox path="${field}" value="${val}" />
				${label}
			</label>
		</div>
	</c:when>
	<c:when test="${type == 'submit'}">
		<div class="form-group">
			<div class="col-lg-offset-3 col-lg-8">
				<button type="submit" class="btn btn-primary">${label}</button>
			</div>
		</div>
	</c:when>
</c:choose>
