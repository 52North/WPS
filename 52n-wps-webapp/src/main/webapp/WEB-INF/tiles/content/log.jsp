<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/"%>

<p class="text-danger">${error}</p>

<form:form modelAttribute="logConfigurations" method="POST" action="log" class="form-horizontal">
	<legend>Patterns &amp; Formats</legend>
	<input:textInput label="File Name Pattern" field="wpsfileAppenderFileNamePattern" />
	<input:textInput label="File Encoder Pattern" field="wpsfileAppenderEncoderPattern" />
	<input:textInput label="Console Encoder Pattern" field="wpsconsoleEncoderPattern" />

	<legend>General</legend>
	<input:textInput label="Max History" field="wpsfileAppenderMaxHistory" desc="In days" />

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
			<input:checkbox label="File" field="fileAppenderEnabled" val="wpsFile" />
			<input:checkbox label="Console" field="consoleAppenderEnabled" val="wpsConsole" />
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
	<div class="form-group">
		<div class="col-lg-offset-3 col-lg-8">
			<button type="submit" class="btn btn-primary">Save</button>
		</div>
	</div>
</form:form>

<script>
	$('form').submit(function(event) {
		event.preventDefault();
		var form = $(this);

		//reset and clear errors
		form.find('#error').remove();
		$(".form-group").each(function() {
			$(this).removeClass("has-error");
		});
		$.ajax({
			type : form.attr('method'),
			url : form.attr('action'),
			data : form.serialize(),
			success : function() {
				alertMessage("", "Configurations updated", "alert alert-success", form);
			},
			error : function(xhr) {
				var errors = xhr.responseJSON.errorMessageList;
				for ( var i = 0; i < errors.length; i++) {
					var item = errors[i];
					
					//display the error after the field
					var field = $('#' + item.field);
					field.parents(".form-group").addClass("has-error");
					$("<div id='error' class='text-danger'>" + item.defaultMessage + "</div>").insertAfter(field);
				}
			}
		});
	});
</script>