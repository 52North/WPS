$(document).ready(
		function() {
		
		});

function alertMessage(title, text, messageClass) {
	var overlayDiv = $("<div id='overlay'>");
	var message = $("<div>").addClass(messageClass);
	$("<button>").addClass("close").attr("data-dismiss", "alert").appendTo(message).text("x");
	$("<strong>").text(title).appendTo(message);
	$("<span>").text(text).appendTo(message);
	overlayDiv.append(message);
	// if it is an error, don't fade out
	if (title.indexOf("Error") != -1) {
		overlayDiv.appendTo($("body"));
	} else {
		overlayDiv.appendTo($("body")).fadeOut(5000);
	}
}



function ajaxHandleFormat(formData, form, type, action) {
	// reset and clear errors and alerts
	$('#fieldError').remove();
	$('#alert').remove();
	$(".form-group").each(function() {
		$(this).removeClass("has-error");
	});
	
	var url;
	var successMessage;
	var errorMessage;
	
	if(action == 'edit'){
		url = type + '/formats/edit_format';
		successMessage = 'Format successfully edited';
		errorMessage = 'Error editing format';
	}else if(action == 'add'){
		url = type + '/formats/add_format';
		successMessage = 'Format successfully added';
		errorMessage = 'Error adding format';
	}
	
	$.ajax({
		url : url,
		data : formData,
		dataType : 'text',
		processData : false,
		contentType : false,
		headers: { 'X-CSRF-TOKEN': $('[name="csrf_token"]').attr('content') },
		type : 'POST',
		success : function(xhr) {
			// success alert
			var alertDiv = $("<div id='alert' data-dismiss class='alert alert-success'>" + successMessage + "</div>");
			var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
			closeBtn.appendTo(alertDiv).text("x");
			alertDiv.insertBefore(form);
		},
		error : function(xhr) {
			// error alert
			var alertDiv = $("<div id='alert' data-dismiss class='alert alert-danger'>" + errorMessage + "</div>");
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