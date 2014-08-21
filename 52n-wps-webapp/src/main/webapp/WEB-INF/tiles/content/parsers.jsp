<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="module" tagdir="/WEB-INF/tags"%>

<module:standardModule configurations="${configurations}" baseUrl="parsers" />

<!-- Add format -->
<div class="modal fade" id="addFormat" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title">Add a format to this parser</h4>
			</div>
			<div class="modal-body">
				<form id="addFormat" method="POST" action="parsers/formats/add_format">
					<div class="form-group">
						<label for="mimeType">Mime type</label>
						<input type="text" name="mimeType" id="mimeType">
						<label for="schema">Schema</label>
						<input type="text" name="schema" id="schema">
						<label for="encoding">Encoding</label>
						<input type="text" name="encoding" id="encoding">
						<input id="hiddenModuleName" type="hidden" />
						<p class="help-block">Please specify the mime type, schema and encoding of the format.</p>
					</div>
					<div class="form-group">
						<button type="submit" class="btn btn-primary">Add</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<script src="<c:url value="/static/js/library/jquery.form.js" />"></script>
<script type="text/javascript">
	$('form#addFormat').submit(function(event) {
		event.preventDefault();
		$('#result').html('');
		var form = $(this);
		var formData = new FormData();
		formData.append("mimeType", $('#mimeType').fieldValue()[0]);
		formData.append("schema", $('#schema').fieldValue()[0]);
		formData.append("encoding", $('#encoding').fieldValue()[0]);
		formData.append("moduleClassName", $('input#hiddenModuleName').val());
		ajaxAddFormat(formData, form);
	});

	function ajaxAddFormat(formData, form) {
		// reset and clear errors and alerts
		$('#fieldError').remove();
		$('#alert').remove();
		$(".form-group").each(function() {
			$(this).removeClass("has-error");
		});
		
		$.ajax({
			url : 'parsers/formats/add_format',
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
</script>