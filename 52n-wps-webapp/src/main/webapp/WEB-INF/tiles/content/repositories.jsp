<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="module" tagdir="/WEB-INF/tags"%>

<module:standardModule configurations="${configurations}" baseUrl="repositories" />

<!-- Start of upload code -->
<a data-toggle="modal" href="#uploadProcess" class="btn btn-primary btn-lg">Upload Process</a>
<a data-toggle="modal" href="#uploadRScript" class="btn btn-primary btn-lg">Upload R Script</a>

<!-- Add algorithm -->
<div class="modal fade" id="addAlgorithm" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Add an algorithm class to this repository</h4>
			</div>
			<div class="modal-body">
				<form id="addAlgorithm" method="POST" action="repositories/algorithms/add_algorithm">
					<div class="form-group">
						<label for="javaFile">Algorithm class</label>
						<input type="text" name="algorithmName" id="algorithmName">
						<input id="hiddenModuleName" type="hidden" />
						<p class="help-block">Please specify the fully qualified class name of the algorithm.</p>
					</div>
					<div class="form-group">
						<button type="submit" class="btn btn-primary">Add</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<!-- Upload process -->
<div class="modal fade" id="uploadProcess" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
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
						<input type="file" name="javaFile" id="javaFile">
						<p class="help-block">Please select the .java file for the process.</p>
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

<!-- Upload R script -->
<div class="modal fade" id="uploadRScript" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Upload R Script</h4>
			</div>
			<div class="modal-body">
				<form id="uploadRScript" class="form-horizontal" method="POST" action="<c:url value="/upload_process" />"
					enctype="multipart/form-data">
					<div class="form-group">
						<div class="col-lg-7">
							<label class="control-label">Process Name</label>
							<input type="text" class="form-control" name="rScriptProcessName" id="rScriptProcessName">
							<p class="help-block">If the process name should be different from the filename. (Don't include the file
								extension.)</p>
						</div>
					</div>
					<div class="form-group">
						<div class="col-lg-7">
							<label for="javaFile">R Script</label>
							<input type="file" name="rScript" id="rScript">
							<p class="help-block">An annotated R script</p>
						</div>
					</div>
					<div class="form-group">
						<div class="col-lg-7">
							<button type="submit" class="btn btn-primary">Upload</button>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<div id="result"></div>
<script src="<c:url value="/static/js/library/jquery.form.js" />"></script>
<script type="text/javascript">
	$('form#uploadProcess').submit(function(event) {
		event.preventDefault();
		$('#result').html('');
		var form = $(this);
		var formData = new FormData();
		formData.append("javaFile", javaFile.files[0]);
		formData.append("xmlFile", xmlFile.files[0]);
		ajaxUpload(formData, form);
	});

	$('form#uploadRScript').submit(function(event) {
		event.preventDefault();
		$('#result').html('');
		var form = $(this);
		var formData = new FormData();
		formData.append("rScriptProcessName", $('#rScriptProcessName').fieldValue()[0]);
		formData.append("rScript", rScript.files[0]);
		ajaxUpload(formData, form);
	});

	$('form#addAlgorithm').submit(function(event) {
		event.preventDefault();
		$('#result').html('');
		var form = $(this);
		var formData = new FormData();
		formData.append("algorithmName", $('#algorithmName').fieldValue()[0]);
		formData.append("moduleClassName", $('input#hiddenModuleName').val());
		ajaxAddAlgorithm(formData, form);
	});

	function ajaxAddAlgorithm(formData, form) {
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$(".form-group").each(function() {
			$(this).removeClass("has-error");
		});
		
		$.ajax({
			url : 'repositories/algorithms/add_algorithm',
			data : formData,
			dataType : 'text',
			processData : false,
			contentType : false,
			type : 'POST',
			success : function(xhr) {
				// success alert
				var alertDiv = $("<div id='alert' data-dismiss class='alert alert-success'>Upload successful</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);
			},
			error : function(xhr) {
				// error alert
				var alertDiv = $("<div id='alert' data-dismiss class='alert alert-danger'>Upload error</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);

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
	}

	function ajaxUpload(formData, form) {
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$(".form-group").each(function() {
			$(this).removeClass("has-error");
		});
		
		$.ajax({
			url : 'upload',
			data : formData,
			dataType : 'text',
			processData : false,
			contentType : false,
			type : 'POST',
			success : function(xhr) {
				// success alert
				var alertDiv = $("<div id='alert' data-dismiss class='alert alert-success'>Upload successful</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);
			},
			error : function(xhr) {
				// error alert
				var alertDiv = $("<div id='alert' data-dismiss class='alert alert-danger'>Upload error</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);

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
	}

	$('a#algorithmStatusButton').click(function(event) {
		event.preventDefault();
		var button = $(this);
		var url = button.attr('href');
		$.ajax({
			type : "POST",
			url : url,
			success : function() {
				var currentStatus = url.substring(url.lastIndexOf('/') + 1);
				var trgetStatus = currentStatus == 'true' ? 'false' : 'true';
				// remove the last false or true and replace it with the new target status for toggling
				url = url.substr(0, url.lastIndexOf('/') + 1) + trgetStatus;
				button.attr('href', url);
				button.toggleClass("btn-success btn-danger").text(button.text() == 'Active' ? "Inactive" : "Active");
				$("<span class='text-success'>	Status updated</span>").insertAfter(button).fadeOut(3000);
			},
			error : function(textStatus, errorThrown) {
				$("<span class='text-danger'>	Error</span>").insertAfter(button).fadeOut(3000);
				alertMessage("Error: ", "unable to update algorithm status", "alert alert-danger");
			}
		});
	});
</script>