<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="module" tagdir="/WEB-INF/tags" %>

<module:standardModule configurations="${configurations}" baseUrl="repositories" />

<!-- Start of upload process -->
<a data-toggle="modal" href="#uploadModal" class="btn btn-primary btn-lg">Upload Process</a>

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
<script src="<c:url value="/resources/js/library/jquery.form.js" />"></script>
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