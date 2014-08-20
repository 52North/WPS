$('form#customForm').submit(
		function(event) {
			event.preventDefault();
			var form = $(this);
			var url = form.attr('action');
			
			// reset and clear errors
			form.find('div#fieldError').remove();
			$(".form-group").each(function() {
				$(this).removeClass("has-error");
			});

			$.ajax({
				type : form.attr('method'),
				url : url,
				data : form.serialize(),
				success : function() {
					alertMessage("", "Configurations Updated", "alert alert-success");
					// if it is add user form, redirect to users page
					if (url.substring(url.lastIndexOf('/') + 1) == 'add_user') {
						window.location.replace("./");
					}
				},
				error : function(xhr) {
					var errors = xhr.responseJSON.errorMessageList;
					for ( var i = 0; i < errors.length; i++) {
						var item = errors[i];

						// display the error after the field
						var field = $('#' + item.field);
						field.parents(".form-group").addClass("has-error");
						$("<div id='fieldError' class='text-danger'>" + item.defaultMessage + "</div>").insertAfter(
								field);
						alertMessage("Error: ", "Configurations not updated. Please view form for errors.",
								"alert alert-danger");
					}
				}
			});
		});