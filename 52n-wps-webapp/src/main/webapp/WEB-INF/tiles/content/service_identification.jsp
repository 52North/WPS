<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="moduleName" value="${configurationModule.moduleName}" />
<c:set var="fullClassName" value="${configurationModule['class'].name}" />
<c:set var="simpleClassName" value="${configurationModule['class'].simpleName}" />

<form id="module" class="form-horizontal" method="POST" action="<c:url value="/service_identification" />">
	<c:forEach var="configurationEntry" items="${configurationModule.configurationEntries}">
		<div class="form-group">
			<label class="col-lg-3 control-label">${configurationEntry.title}</label>
			<div class="col-lg-7">
				<c:choose>
					<c:when test="${configurationEntry.type eq 'BOOLEAN'}">
						<input name="value" id='valueHidden' type='hidden' value='false'>
						<input name="value" type="checkbox" value="true" <c:if test="${configurationEntry.value}">checked</c:if> />
					</c:when>
					<c:otherwise>
						<input name="value" class="form-control" type="text" value="${configurationEntry.value}" />
					</c:otherwise>
				</c:choose>
				<span class="help-block"> ${configurationEntry.description} </span>
			</div>
			<input name="key" type="hidden" value="${configurationEntry.key}" />
			<input name="module" type="hidden" value="${fullClassName}" />
			<c:if test="${configurationEntry.required and configurationEntry.type ne 'BOOLEAN'}">
				<span class="label label-danger">Required</span>
			</c:if>
		</div>
	</c:forEach>
	<div class="form-group">
		<div class="col-lg-offset-3 col-lg-8">
			<button type="submit" class="btn btn-primary">Save</button>
		</div>
	</div>
</form>