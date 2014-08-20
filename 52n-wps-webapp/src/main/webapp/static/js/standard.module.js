$('form#standardModule').submit(
		function(event) {
			event.preventDefault();
			var form = $(this);

			// if checkbox is unchecked, enable a hidden value of false
			form.find("input[type=checkbox]").each(function() {
				var disabled = $(this).prev("#valueHidden");
				if ($(this).is(':checked')) {
					disabled.attr('disabled', 'disabled');
				} else {
					disabled.removeAttr('disabled');
				}
			});

			// reset and clear errors
			form.find('div#fieldError').remove();
			form.find(".form-group").each(function() {
				$(this).removeClass("has-error");
			});

			$.ajax({
				type : form.attr('method'),
				url : form.attr('action'),
				data : form.serialize(),
				success : function() {
					form.addClass("has-success");
					setTimeout(function() {
						form.removeClass("has-success");
					}, 4000);
					alertMessage("", "Configurations updated", "alert alert-success");
				},
				error : function(xhr) {
					var errors = xhr.responseJSON.errorMessageList;
					for ( var i = 0; i < errors.length; i++) {
						var item = errors[i];

						// display the error after the field
						var fieldDiv = form.find("div[id='" + item.field + "']");
						var inputBox = fieldDiv.find("input[name='value']");
						fieldDiv.addClass("has-error");
						$("<div id='fieldError' class='text-danger'>" + item.defaultMessage + "</div>").insertAfter(
								inputBox);
						alertMessage("Error: ", "Configurations not updated. Please view form for errors.",
								"alert alert-danger");
					}
				}
			});
		});

$('a#moduleStatusButton').click(function(event) {
	event.preventDefault();
	var button = $(this);
	var url = button.attr('href');
	$.ajax({
		type : "POST",
		url : url,
		success : function() {
			var currentStatus = url.substring(url.lastIndexOf('/') + 1);
			var trgetStatus = currentStatus == 'true' ? 'false' : 'true';
			
			/*
			 * remove the last false or true and replace it with the new target
			 * status for toggling
			 */
			url = url.substr(0, url.lastIndexOf('/') + 1) + trgetStatus;
			button.attr('href', url);
			button.toggleClass("btn-success btn-danger").text(button.text() == 'Active' ? "Inactive" : "Active");
			alertMessage("", "Module status updated", "alert alert-success");
		},
		error : function() {
			alertMessage("Error: ", "Unable to update module status", "alert alert-danger");
		}
	});
});