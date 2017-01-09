<%--

    Copyright (C) 2012-2015 52°North Initiative for Geospatial Open Source
    Software GmbH

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License version 2 as published
    by the Free Software Foundation.

    If the program is linked with libraries which are licensed under one of
    the following licenses, the combination of the program with the linked
    library is not considered a "derivative work" of the program:

        - Apache License, version 2.0
        - Apache Software License, version 1.0
        - GNU Lesser General Public License, version 3
        - Mozilla Public License, versions 1.0, 1.1 and 2.0
        - Common Development and Distribution License (CDDL), version 1.0

    Therefore the distribution of the program linked with libraries licensed
    under the aforementioned licenses, is permitted by the copyright holders
    if the distribution is compliant with both the GNU General Public
    License version 2 and the aforementioned licenses.

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="configurations" required="true" type="java.util.Map"%>
<%@ attribute name="baseUrl" required="true"%>

<div class="accordion" id="accordion2">
	<c:forEach var="configurationModule" items="${configurations}">

		<%-- Define initial variables --%>
		<c:set var="moduleName" value="${configurationModule.value.moduleName}" />
		<c:set var="fullClassName" value="${configurationModule.value['class'].name}" />
		<c:set var="simpleClassName" value="${configurationModule.value['class'].simpleName}" />
		<c:set var="targetModuleStatus" value="${!configurationModule.value.active}" />
		<c:set var="moduleStatusText" value="${configurationModule.value.active ? 'Active' : 'Inactive'}" />
		<c:set var="moduleStatusClass"
			value="${configurationModule.value.active ? 'btn btn-success btn-meduim' : 'btn btn-danger btn-meduim'}" />

		<%-- Start of accordion component. Each accordion holds a module --%>
		<div class="accordion-group">

			<%-- Accordion head which contains module name and toggle button --%>
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#${simpleClassName}"> <i
					class="glyphicon glyphicon-chevron-down"></i> ${moduleName} (${fullClassName})
				</a>
				<div class="accordion-button">
					<a id="moduleStatusButton" class="${moduleStatusClass}"
						href="<c:url value="/${baseUrl}/activate/${fullClassName}/${targetModuleStatus}" />"><c:out
							value="${moduleStatusText}" /></a>
				</div>
			</div>

			<%-- Accordion body --%>
			<div id="${simpleClassName}" class="accordion-body collapse">
				<div class="accordion-inner">

					<%-- A module form --%>
					<form id="standardModule" class="form-horizontal" method="POST" action="<c:url value="/${baseUrl}" />">

						<%--  Create an input for each configuration entry --%>
						<c:forEach var="configurationEntry" items="${configurationModule.value.configurationEntries}">
							<div id="${configurationEntry.key}" class="form-group">

								<%-- If the entry is boolean, display a checkbox, else, display a text input --%>
								<c:choose>
									<c:when test="${configurationEntry.type eq 'BOOLEAN'}">
										<div class="col-lg-offset-3 col-lg-7">
											<div class="checkbox">
												<label>
													<input name="value" id='valueHidden' type='hidden' value='false'>
													<input name="value" type="checkbox" value="true" <c:if test="${configurationEntry.value}">checked</c:if> />
													${configurationEntry.title}
												</label>
												<span class="help-block"> ${configurationEntry.description} </span>
											</div>
										</div>
									</c:when>
									<c:otherwise>
										<label class="col-lg-3 control-label">${configurationEntry.title}</label>
										<div class="col-lg-7">
											<input name="value" class="form-control" type="text" value="${configurationEntry.value}" />
											<span class="help-block"> ${configurationEntry.description} </span>
										</div>
									</c:otherwise>
								</c:choose>


								<%-- Hidden fields to identify and process the entry and module --%>
								<input name="key" type="hidden" value="${configurationEntry.key}" />
								<input name="module" type="hidden" value="${fullClassName}" />

								<%-- Only show the required label if the field is required and is not a boolean (checkbox) --%>
								<c:if test="${configurationEntry.required and configurationEntry.type ne 'BOOLEAN'}">
									<span class="label label-danger">Required</span>
								</c:if>
							</div>
						</c:forEach>

						<%-- Save button --%>
						<c:if test="${not empty configurationModule.value.configurationEntries}">
							<div class="form-group">
								<div class="col-lg-offset-3 col-lg-8">
									<button type="submit" class="btn btn-primary">Save</button>
								</div>
							</div>
						</c:if>
					</form>

					<%-- Display the algorithms table if the module has any --%>
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

								<%-- Create a row for each algorithm --%>
								<c:forEach var="algorithmEntry" items="${configurationModule.value.algorithmEntries}">
									<c:set var="targetAlgorithmStatus" value="${!algorithmEntry.active}" />
									<c:set var="algorithmStatusText" value="${algorithmEntry.active ? 'Active' : 'Inactive'}" />
									<c:set var="algorithmStatusClass"
										value="${algorithmEntry.active ? 'btn btn-success btn-mini' : 'btn btn-danger btn-mini'}" />

									<tr>
										<td>${algorithmEntry.algorithm}</td>
										<td><a id="algorithmStatusButton" class="${algorithmStatusClass}"
											href="<c:url value="/${baseUrl}/algorithms/activate/${fullClassName}/${algorithmEntry.algorithm}/${targetAlgorithmStatus}" />"><c:out
													value="${algorithmStatusText}" /></a></td>
										<td>
										<a data-toggle="modal" href="#editAlgorithm" class="btn btn-default btn-mini" onClick="setParametersForAlgorithmUpdate('${fullClassName}', '${algorithmEntry.algorithm}')">Edit</a>
										</td>
										<td><a id="deleteAlgorithm" class="btn btn-danger btn-mini" href="<c:url value="/repositories/algorithms/${fullClassName}/${algorithmEntry.algorithm}/delete" />">Delete</a>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
						<!--button type="submit" class="btn btn-primary" id="addAlgorithmButton" onClick="buttonClick('${fullClassName}')">Add algorithm</button-->						
                        <a data-toggle="modal" href="#addAlgorithm" class="btn btn-primary btn-lg" onClick="setHiddenModuleName('${fullClassName}')">Add algorithm</a>
					</c:if>											
					<c:if test="${fullClassName == 'org.n52.wps.server.modules.UploadedAlgorithmRepositoryCM'}">
						<a data-toggle="modal" href="#uploadProcess" class="btn btn-primary btn-lg">Upload Process</a>
				    </c:if>
					<c:if test="${fullClassName == 'org.n52.wps.server.r.LocalRAlgorithmRepositoryCM'}">
						<a data-toggle="modal" href="#uploadRScript" class="btn btn-primary btn-lg">Upload R Script</a>
				    </c:if>
					<%-- Display the formats table if the module has any --%>
					<c:if test="${not empty configurationModule.value.formatEntries}">

						<table id="${fullClassName}" class="table table-bordered table-striped table-hover">
							<colgroup>
								<col class="col-lg-9">
								<col class="col-lg-3">
							</colgroup>
							<thead>
		                       <tr>
			                     <th>Mime type</th>
			                     <th>Schema</th>
			                     <th>Encoding</th>
		                       </tr>
							</thead>
							<tbody>

								<%-- Create a row for each format --%>
								<c:forEach var="formatEntry" items="${configurationModule.value.formatEntries}">
									<c:set var="targetFormatStatus" value="${!formatEntry.active}" />
									<c:set var="formatStatusText" value="${formatEntry.active ? 'Active' : 'Inactive'}" />
									<c:set var="formatStatusClass"
										value="${formatEntry.active ? 'btn btn-success btn-mini' : 'btn btn-danger btn-mini'}" />
									<tr>
										<td>${formatEntry.mimeType}</td>
										<td>${formatEntry.schema}</td>
										<td>${formatEntry.encoding}</td>
										<td><a id="formatStatusButton" class="${formatStatusClass}"
											href="<c:url value="/${baseUrl}/formats/activate/${fullClassName}/${fn:replace(formatEntry.mimeType, '/', 'forwardslash')}/${formatEntry.schema == '' ? 'null' : formatEntry.schema}/${formatEntry.encoding == '' ? 'null' : formatEntry.encoding}/${targetFormatStatus}" />"><c:out
													value="${formatStatusText}" /></a></td>
										<td><a data-toggle="modal" href="#editFormat" class="btn btn-default btn-mini" onClick="setParametersForFormatUpdate('${fullClassName}', '${formatEntry.mimeType}', '${formatEntry.schema == '' ? '' : formatEntry.schema}', '${formatEntry.encoding == '' ? '' : formatEntry.encoding}')">Edit</a>
										</td>
										<td><a id="deleteFormat" class="btn btn-danger btn-mini" href="<c:url value="/parsers/formats/${fullClassName}/${fn:replace(formatEntry.mimeType, '/', 'forwardslash')}/${formatEntry.schema == '' ? 'null' : formatEntry.schema}/${formatEntry.encoding == '' ? 'null' : formatEntry.encoding}/delete" />">Delete</a>
										<!--td><a id="deleteFormat" class="btn btn-danger btn-mini" onClick="ajaxDeleteFormat('${fullClassName}', '${formatEntry.mimeType}', '${(formatEntry.schema == ' ') ? 'null' : formatEntry.schema}', '${formatEntry.encoding == ' ' ? 'nullo' : formatEntry.encoding}')">Delete</a-->
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
						<!--button type="submit" class="btn btn-primary" id="addAlgorithmButton" onClick="buttonClick('${fullClassName}')">Add algorithm</button-->						
						<a data-toggle="modal" href="#addFormat" class="btn btn-primary btn-lg" onClick="setHiddenModuleName('${fullClassName}')">Add format</a>
					</c:if>
				</div>
			</div>
		</div>
	</c:forEach>
</div>
<script src="<c:url value="/static/js/standard.module.js" />"></script>
<script type="text/javascript">

	function setHiddenModuleName(moduleName) {
		$('input#hiddenModuleName').val(moduleName);
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$('#result').html('');
	}
	function setParametersForAlgorithmUpdate(moduleName, oldAlgorithmname) {
		$('input#hiddenModuleName').val(moduleName);
		$('input#newAlgorithmName').val(oldAlgorithmname);
		$('input#hiddenOldAlgorithmName').val(oldAlgorithmname);
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$('#result').html('');
	}
	function setParametersForFormatUpdate(moduleName, oldMimetype, oldSchema, oldEncoding) {
		$('input#hiddenModuleName').val(moduleName);
		$('input#newMimetype').val(oldMimetype);
		$('input#newSchema').val(oldSchema);
		$('input#newEncoding').val(oldEncoding);
		$('input#hiddenOldMimetype').val(oldMimetype);
		$('input#hiddenOldSchema').val(oldSchema);
		$('input#hiddenOldEncoding').val(oldEncoding);
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$('#result').html('');
	}
	$('a#deleteAlgorithm').click(
			function(event) {
				event.preventDefault();
				var a = $(this);
				var row = a.parents("tr");
				var url = a.attr('href');
				$.ajax({
					type : "POST",
					url : url,
					headers : {
						'X-CSRF-TOKEN' : "${_csrf.token}"
					},
					success : function() {
						(row).remove();
						alertMessage("", "Algorithm deleted",
								"alert alert-success", a);
					},
					error : function(textStatus, errorThrown) {
						alertMessage("Error: ", "Unable to delete algorithm",
								"alert alert-danger", a);
					}
				});
			});

	$('a#deleteFormat').click(
			function(event) {
				event.preventDefault();
				var a = $(this);
				var row = a.parents("tr");
				var url = a.attr('href');
				$.ajax({
					type : "POST",
					url : url,
					headers : {
						'X-CSRF-TOKEN' : "${_csrf.token}"
					},
					success : function() {
						(row).remove();
						alertMessage("", "Format deleted",
								"alert alert-success", a);
					},
					error : function(textStatus, errorThrown) {
						alertMessage("Error: ", "Unable to delete format",
								"alert alert-danger", a);
					}
				});
			});

	$('a#formatStatusButton').click(
			function(event) {
				event.preventDefault();
				var button = $(this);
				var url = button.attr('href');
				$.ajax({
					type : "POST",
					url : url,
					headers : {
						'X-CSRF-TOKEN' : "${_csrf.token}"
					},
					success : function() {
						var currentStatus = url
								.substring(url.lastIndexOf('/') + 1);
						var trgetStatus = currentStatus == 'true' ? 'false'
								: 'true';
						// remove the last false or true and replace it with the new target status for toggling
						url = url.substr(0, url.lastIndexOf('/') + 1)
								+ trgetStatus;
						button.attr('href', url);
						button.toggleClass("btn-success btn-danger").text(
								button.text() == 'Active' ? "Inactive"
										: "Active");
						$("<span class='text-success'>	Status updated</span>")
								.insertAfter(button).fadeOut(3000);
					},
					error : function(textStatus, errorThrown) {
						$("<span class='text-danger'>	Error</span>")
								.insertAfter(button).fadeOut(3000);
						alertMessage("Error: ",
								"unable to update format status",
								"alert alert-danger");
					}
				});
			});
</script>