<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<div class="accordion" id="accordion2">
	<c:forEach var="configurationModule" items="${configurations}">
		<c:set var="moduleName" value="${configurationModule.value.moduleName}" />
		<c:set var="fullClassName" value="${configurationModule.value['class'].name}" />
		<c:set var="simpleClassName" value="${configurationModule.value['class'].simpleName}" />
		<c:set var="active" value="${configurationModule.value.active}" />

		<div class="accordion-group">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#${simpleClassName}"> <i
					class="glyphicon glyphicon-chevron-down"></i> ${moduleName} (${fullClassName})
				</a>
				<div class="accordion-button">
					<a class="btn btn-success btn-meduim" href="<c:url value="/repositories/activate/${fullClassName}" />"><c:out
							value="${active ? 'Active' : 'Inactive'}" /></a>
				</div>
			</div>
			<div id="${simpleClassName}" class="accordion-body collapse">
				<div class="accordion-inner">
					<form class="form-horizontal" method="POST" action="<c:url value="/repositories" />">
						<div id="${fullClassName}"></div>
						<c:forEach var="configurationEntry" items="${configurationModule.value.configurationEntries}">
							<div class="form-group">
								<label class="col-lg-2 control-label">${configurationEntry.title}</label>
								<div class="col-lg-4">
									<c:choose>
										<c:when test="${configurationEntry.type eq 'BOOLEAN'}">
											<input name="value" type="hidden" value="false" />
											<input name="value" type="checkbox" value="true" />
										</c:when>
										<c:otherwise>
											<input name="value" class="form-control" type="text" value="${configurationEntry.value}" />
										</c:otherwise>
									</c:choose>
									<span class="help-block text-muted">
										<small>${configurationEntry.description}</small>
									</span>
								</div>
								<input name="key" type="hidden" value="${configurationEntry.key}" />
								<input name="module" type="hidden" value="${fullClassName}" />
								<c:if test="${configurationEntry.required and configurationEntry.type ne 'BOOLEAN'}">
									<span class="label label-danger">Required</span>
								</c:if>
							</div>
						</c:forEach>
						<div class="form-group">
							<div class="col-lg-offset-2 col-lg-8">
								<button type="submit" class="btn btn-primary">Save</button>
							</div>
						</div>
					</form>


					<table id="${fullClassName}" class="table table-striped table-bordered table-hover">
						<thead>
							<tr>
								<th colspan="2">Algorithms</th>
							</tr>
						</thead>
						<c:forEach var="algorithmEntry" items="${configurationModule.value.algorithmEntries}">
							<tr>
								<td>${algorithmEntry.algorithm}</td>
								<c:set var="status" value="${algorithmEntry.active ? 'Active' : 'Inactive'}" />
								<c:set var="statusClass"
									value="${algorithmEntry.active ? 'btn btn-success btn-mini' : 'btn btn-danger btn-mini'}" />
								<td><button name="${algorithmEntry.algorithm}" class="${statusClass}">${status}</button></td>
							</tr>
						</c:forEach>
					</table>
				</div>
			</div>
		</div>
	</c:forEach>
</div>

<script>
	$(document).ready(
			function() {

				$('form').submit(function(event) {
					event.preventDefault();
					var form = $(this);
					$.ajax({
						type : form.attr('method'),
						url : form.attr('action'),
						data : form.serialize(),
						success : function() {
							alertMessage("Success: ", "configurations updated", "alert alert-success", form, 5000);
						},
						error : function(xhr) {
							alertMessage("Error: ", xhr.responseText, "alert alert-danger", 5000);
						}
					});
				});

				$('.accordion').on(
						'show hide',
						function(n) {
							$(n.target).siblings('.accordion-heading').find('.accordion-toggle i').toggleClass(
									'glyphicon-chevron-up glyphicon-chevron-down');
						});
			});

	function alertMessage(title, text, messageClass, object, time) {
		var overlayDiv = $("<div id='overlay'>");
		var message = $("<div>").addClass(messageClass);
		$("<button>").addClass("close").attr("data-dismiss", "alert").appendTo(message).text("x");
		$("<strong>").text(title).appendTo(message);
		$("<span>").text(text).appendTo(message);
		overlayDiv.append(message).fadeOut(time);
		$("body").append(overlayDiv);
	}
</script>