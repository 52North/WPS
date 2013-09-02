<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="accordion" id="accordion2">
	<c:forEach var="configurationModule" items="${configurations}">
		<c:set var="moduleName" value="${configurationModule.value.moduleName}" />
		<c:set var="fullClassName" value="${configurationModule.value['class'].name}" />
		<c:set var="simpleClassName" value="${configurationModule.value['class'].simpleName}" />

		<div class="accordion-group">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#${simpleClassName}"> <i
					class="glyphicon glyphicon-chevron-down"></i> ${moduleName} (${fullClassName})
				</a>
				<div class="accordion-button">
					<c:set var="moduleStatus" value="${configurationModule.value.active ? 'Active' : 'Inactive'}" />
					<c:set var="statusClass"
						value="${configurationModule.value.active ? 'btn btn-success btn-meduim' : 'btn btn-danger btn-meduim'}" />
					<a id="moduleStatusButton" class="${statusClass}" href="<c:url value="/repositories/activate/${fullClassName}" />"><c:out
							value="${moduleStatus}" /></a>
				</div>
			</div>
			<div id="${simpleClassName}" class="accordion-body collapse">
				<div class="accordion-inner">
					<form id="module" class="form-horizontal" method="POST" action="<c:url value="/repositories" />">
						<c:forEach var="configurationEntry" items="${configurationModule.value.configurationEntries}">
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

					<c:if test="${not empty configurationModule.value.algorithmEntries}">
						<table id="${fullClassName}" class="table table-bordered table-striped table-hover">
							<colgroup>
								<col class="col-lg-9">
								<col class="col-lg-3">
							</colgroup>
							<thead>
								<tr>
									<th colspan="2">Algorithms</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="algorithmEntry" items="${configurationModule.value.algorithmEntries}">
									<tr>
										<td>${algorithmEntry.algorithm}</td>
										<c:set var="status" value="${algorithmEntry.active ? 'Active' : 'Inactive'}" />
										<c:set var="statusClass"
											value="${algorithmEntry.active ? 'btn btn-success btn-mini' : 'btn btn-danger btn-mini'}" />
										<td><button id="algorithmButton" name="${algorithmEntry.algorithm}" class="${statusClass}">${status}</button></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</c:if>
				</div>
			</div>
		</div>
	</c:forEach>
</div>

<a data-toggle="modal" href="#uploadModal" class="btn btn-primary btn-lg">Upload Process</a>
<script src="<c:url value="/resources/js/library/jquery.form.js" />"></script>

<div class="modal fade" id="uploadModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Upload a process and its description</h4>
			</div>
			<div class="modal-body">
				<form id="uploadProcess" method="POST" action="<c:url value="/upload_process" />" enctype="multipart/form-data">
					<div class="form-group">
						<label for="javaFile">Java File</label>
						<input type="file" name="javaFile" id="javaFile" >
						<p class="help-block">Please select the .java file for the process.</p>
						<p class="text-danger">${javaFileError}</p>
					</div>
					<div class="form-group">
						<label for="processDescription">Process Description</label>
						<input type="file" name="xmlFile" id="xmlFile">
						<p class="help-block">The associated ProcessDescription.xml file (optional).</p>
					</div>
					<div class="form-group">
						<button type="submit" class="btn btn-primary">Upload</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>
<div id="result"></div>

<script src="resources/js/library/jquery.form.js"></script>

<script type="text/javascript">
	$('form#uploadProcess').submit(function(event) {

		event.preventDefault();

		$('#result').html('');
		var form = $(this);
		var formData = new FormData();
		formData.append("javaFile", javaFile.files[0]);
		formData.append("xmlFile", xmlFile.files[0]);
		$.ajax({
			url : 'upload_process',
			data : formData,
			dataType : 'text',
			processData : false,
			contentType : false,
			type : 'POST',
			success : function(xhr) {
				var alertDiv = $("<div data-dismiss class='alert alert-success'>Process uploaded</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);
			},
			error : function(xhr) {
				var json = JSON.parse(xhr.responseText);
				var errors = json.errorMessageList;
				for ( var i = 0; i < errors.length; i++) {
					var item = errors[i];

					//display the error after the field
					var field = $('#' + item.field);
					field.parents(".form-group").addClass("has-error");
					$("<div id='fieldError' class='text-danger'>" + item.defaultMessage + "</div>").insertAfter(field);
				}
			}

		});
	});
</script>