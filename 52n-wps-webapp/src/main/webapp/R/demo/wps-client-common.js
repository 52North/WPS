

var debug = false;

var urlIndex = window.location.href.lastIndexOf("/R/");
var urlBasisString = window.location.href.substring(0, (urlIndex + 1));
var serviceUrlString = urlBasisString + "WebProcessingService";

var handleResponse = function(data) {
	var isError = $(data).find("ns\\:ExceptionReport").length > 0;
	if (isError) {
		showError(data);
	} else {
		showResponse(data);
	}
}

var showError = function(error) {
//	var xmlString = (new XMLSerializer()).serializeToString(error);
//	alert(xmlString);

	var messages = "";
	$(error).find("ns\\:Exception").each(
			function() {

				var text = $(this).find("ns\\:ExceptionText").text();
				var locator = $(this).attr("locator");

				var errorMessage = "<p>Error: " + text + "<br />Locator: "
						+ locator + "</p>\n";
				messages = messages + errorMessage;
			});

	$("#resultLog").html("<div class=\"error\">" + messages + "</div>");
}

var beginsWith = function(string, pattern) {
	return (string.indexOf(pattern) === 0);
}

var endsWith = function(string, pattern) {
	var d = string.length - pattern.length;
	return (d >= 0 && string.lastIndexOf(pattern) === d);
}

$(document).ready(function() {
	$("#serviceUrl").html("<em>" + serviceUrlString + "</em>");
});