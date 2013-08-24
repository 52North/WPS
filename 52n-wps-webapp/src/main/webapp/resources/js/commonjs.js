$(document).ready(
		function() {
			$('form#module').submit(function(event) {
				event.preventDefault();
				var form = $(this);
				
				//if checkbox is unchecked, enable hidden value of false
				$('form input[type=checkbox]').each(function() {
					var disabled = $(this).prev("#valueHidden");
					if ($(this).is(':checked')) {
						disabled.attr('disabled', 'disabled');
					} else {
						disabled.removeAttr('disabled');
					}
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
						alertMessage("", "Configurations updated", "alert alert-success", form);
					},
					error : function(xhr) {
						form.addClass("has-error");
						setTimeout(function() {
							form.removeClass("has-error");
						}, 4000);
						alertMessage("Error: ", xhr.responseText, "alert alert-danger", form);
					}
				});
			});

			$('a#moduleStatusButton').click(
					function(event) {
						event.preventDefault();
						var button = $(this);
						var url = button.attr('href');
						$
								.ajax({
									type : "POST",
									url : url,
									success : function() {
										button.toggleClass("btn-success btn-danger").text(
												button.text() == 'Active' ? "Inactive" : "Active");
										alertMessage("", "Module status updated", "alert alert-success", button);
									},
									error : function(textStatus, errorThrown) {
										alertMessage("Error: ", "Unable to update module status", "alert alert-danger",
												button);
									}
								});
					});

			$('a#deleteUser').click(function(event) {
				event.preventDefault();
				var a = $(this);
				var row = a.parents("tr");
				var url = a.attr('href');
				$.ajax({
					type : "POST",
					url : url,
					success : function() {
						(row).remove();
						alertMessage("", "User deleted", "alert alert-success", a);
					},
					error : function(textStatus, errorThrown) {
						alertMessage("Error: ", "Unable to delete user", "alert alert-danger", a);
					}
				});
			});

			$('button#algorithmButton').click(
					function(event) {
						event.preventDefault();
						var button = $(this);
						var moduleClassName = button.parents("table").attr('id');
						var algorithm = button.attr('name');
						var url = "repositories/algorithms/activate/" + moduleClassName + "/" + algorithm;
						$.ajax({
							type : "POST",
							url : url,
							success : function() {
								button.toggleClass("btn-success btn-danger").text(
										button.text() == 'Active' ? "Inactive" : "Active");
								$("<span class='text-success'>	Status updated</span>").insertAfter(button)
										.fadeOut(3000);
							},
							error : function(textStatus, errorThrown) {
								$("<span class='text-danger'>	Error</span>").insertAfter(button).fadeOut(3000);
								alertMessage("Error: ", "unable to update algorithm status", "alert alert-danger",
										button);
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

function alertMessage(title, text, messageClass, object) {
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