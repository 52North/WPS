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