<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags"%>

<p class="text-danger">${error}</p>

<form:form id="customForm" modelAttribute="logConfigurations" method="POST" action="log" class="form-horizontal">
	<legend>Patterns &amp; Formats</legend>
	<input:customInput label="File Name Pattern" field="wpsfileAppenderFileNamePattern" />
	<input:customInput label="File Encoder Pattern" field="wpsfileAppenderEncoderPattern" />
	<input:customInput label="Console Encoder Pattern" field="wpsconsoleEncoderPattern" />

	<legend>General</legend>
	<input:customInput label="Max History" field="wpsfileAppenderMaxHistory" desc="In days" />

	<legend>Appenders &amp; Loggers</legend>
	<div class="form-group">
		<form:label class="col-lg-3 control-label" path="rootLevel">Root Log Level</form:label>
		<div class="col-lg-6">
			<form:select class="form-control" path="rootLevel">
				<form:option value="DEBUG">DEBUG</form:option>
				<form:option value="INFO">INFO</form:option>
				<form:option value="WARN">WARN</form:option>
				<form:option value="ERROR">ERROR</form:option>
			</form:select>
		</div>
	</div>
	<div class="form-group">
		<form:label class="col-lg-3 control-label" path="rootLevel">Appenders</form:label>
		<div class="col-lg-6">
			<input:customInput label="File" field="fileAppenderEnabled" type="checkbox" val="wpsFile" />
			<input:customInput label="Console" field="consoleAppenderEnabled" type="checkbox" val="wpsConsole" />
		</div>
	</div>
	<form:label class="col-lg-3 control-label" path="loggers">Loggers</form:label>
	<div class="col-lg-7">
		<table class="table table-hover table-condensed">
			<colgroup>
				<col class="col-lg-9">
				<col class="col-lg-3">
			</colgroup>
			<tbody>
				<c:forEach var="logger" items="${logConfigurations.loggers}">
					<tr>
						<td>${logger.key}</td>
						<td><form:select class="form-control" path="loggers['${logger.key}']">
								<form:option value="DEBUG">DEBUG</form:option>
								<form:option value="INFO">INFO</form:option>
								<form:option value="WARN">WARN</form:option>
								<form:option value="ERROR">ERROR</form:option>
								<form:option value="OFF">OFF</form:option>
							</form:select></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	<input:customInput label="Save" type="submit" />
</form:form>
<script src="<c:url value="/static/js/custom.module.js" />"></script>