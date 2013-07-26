var outputIdentifier = "report";

var processIdentifier_pegel = "org.n52.wps.server.r.pegel-report";
var processIdentifier_sweavefoo = "org.n52.wps.server.r.sweave-foo";

$(document).ready(function(){  
	  $("#link_processdescription_pegel").attr("href", "../../WebProcessingService?Request=DescribeProcess&Service=WPS&version=1.0.0&Identifier=" + processIdentifier_pegel);
	  $("#link_processdescription_sweavefoo").attr("href", "../../WebProcessingService?Request=DescribeProcess&Service=WPS&version=1.0.0&Identifier=" + processIdentifier_sweavefoo);
	  //alert($("#link_processdescription").attr("href"));
	});


var sendRequest = function(processId, outputId, outputSourceId, outputFormat) {

	var beforeOutput = '<?xml version="1.0" encoding="UTF-8"?>'
			+ '<wps:Execute service="WPS" version="1.0.0" '
			+ 'xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" '
			+ 'xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
			+ 'xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">'
			+ '<ows:Identifier>' + processId + '</ows:Identifier>'
			+ '<wps:ResponseForm>';

	var rawOutput = '<wps:RawDataOutput>' + '<ows:Identifier>' + outputId
			+ '</ows:Identifier>' + '</wps:RawDataOutput>';
	var linkOutput = '<wps:ResponseDocument>'
			+ '<wps:Output asReference="true">' + '<ows:Identifier>' + outputId
			+ '</ows:Identifier>' + '</wps:Output>'
			+ '<wps:Output asReference="false">' + '<ows:Identifier>'
			+ outputSourceId + '</ows:Identifier>' + '</wps:Output>'
			+ '</wps:ResponseDocument>';

	var afterOutput = '</wps:ResponseForm>' + '</wps:Execute>';

	var requestString = null;
	if (outputFormat == "pdf") {
		requestString = beforeOutput + rawOutput + afterOutput;
		var data = {
			"request" : requestString,
			"data-ajax" : "false",
			"rel" : "external"
		};
		download(serviceUrlString, data, "post");

	} else if (outputFormat == "link") {
		requestString = beforeOutput + linkOutput + afterOutput;

		var requestXML = $.parseXML(requestString);
		var xmlstr = requestXML.xml ? requestXML.xml : (new XMLSerializer())
				.serializeToString(requestXML);

		$("#resultLog").html(
				"<div class=\"info\">Sent request to " + serviceUrlString
						+ " :<br /><textarea>" + xmlstr + "</textarea><div>");

		$.ajax({
			type : "POST",
			url : serviceUrlString, // "http://localhost:8080/wps/WebProcessingService",
			data : {
				request : xmlstr
			},
			cache : false,
			dataType : "xml",
			success : handleResponse
		});
	} else {
		$("#resultLog")
				.html(
						"<div class=\"validation\">Output format must be pdf or link.</div>");
		return;
	}

}

// http://stackoverflow.com/questions/10627051/jquery-ajax-pdf-response
// http://stackoverflow.com/questions/1149454/non-ajax-get-post-using-jquery-plugin
download = function(url, data, method) {
	// url and data options required
	if (url && data) {
		var form = $('<form />', {
			action : url,
			method : (method || 'get')
		});

		$.each(data, function(key, value) {
			var input = $('<input />', {
				type : 'hidden',
				name : key,
				value : value
			});
			input.appendTo(form);
		});

		// return form.appendTo('body').submit().remove();
		form.appendTo('body');
		form.submit();
	}

	throw new Error('$.download(url, data) - url or data invalid');
};

var showResponse = function(executeResponse) {
	var status = $(executeResponse).find("ns\\:Status");
	var statusText = $(status).find("ns\\:ProcessSucceeded").text();
	$("#resultLog").html("<div class=\"success\">" + statusText + "</div>");

	var message = "";

	$(executeResponse).find("ns\\:Output").each(
			function() {
				var title = $(this).find("ns1\\:Title").text();

				$(this).find("ns\\:Reference").each(
						function() {

							var link = $(this).attr("href");
							var mime_type = $(this).attr("mimeType");

							if (beginsWith(link, "http://")) {
								message = message + "<p>" + title + " ("
										+ mime_type + "): " + "<a href='"
										+ link + "' alt='" + title + " ("
										+ mime_type + ")" + "'>" + link
										+ "</a></p>";
							}
						});

				$(this).find("ns\\:LiteralData").each(
						function() {

							var value = $(this).text();
							var dataType = $(this).attr("dataType");

							if (beginsWith(value, "http://")) {
								message = message + "<p>" + title + " ("
										+ dataType + ")" + ": <a href='"
										+ value + "' alt='" + title + "'>"
										+ value + "</a></p>";
							} else {
								message = message + "<p>" + title + "("
										+ dataType + ")" + " = " + value
										+ "</p>";
							}
						});
			});

	$("#report").html("<div class=\"success\">" + message + "</div>");
}

$(function() {

	$("#executeRequest").click(function() {
		var process = $("input:radio[name=radio-content]:checked").val();
		var format = $("input:radio[name=radio-output]:checked").val();

		$("#resultLog").html("Process: " + process + " | Format: " + format);

		sendRequest(process, outputIdentifier, "report_source", format);
	});

	$("#resultLog").ajaxError(
			function(event, request, settings, exception) {
				$("#resultLog").html(
						"<div class=\"warning\">Error Calling: " + settings.url
								+ "<br />HTPP Code: " + request.status
								+ "<br />Exception: " + exception + "</div>");
			});

	// suppossed to help with the form submission, disabled transitions
	$(document).bind("mobileinit", function() {
		$.mobile.ajaxenabled = false;
	});

});
