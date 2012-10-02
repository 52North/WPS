var debug = true;

var beginsWith = function(string, pattern) {
	return (string.indexOf(pattern) === 0);
}

var endsWith = function(string, pattern) {
	var d = string.length - pattern.length;
	return (d >= 0 && string.lastIndexOf(pattern) === d);
}

var urlIndex = window.location.href.lastIndexOf("/R");
var urlBasisString = window.location.href.substring(0, (urlIndex + 1));
var serviceUrlString = urlBasisString + "WebProcessingService";
var processIdentifier = 'org.n52.wps.server.r.SosPlot';

var requestPlot = function(days, offering) {

	var requestString = '<?xml version="1.0" encoding="UTF-8"?><wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">'
			+ '<ows:Identifier>'
			+ processIdentifier
			+ '</ows:Identifier>'
			+ '<wps:DataInputs><wps:Input><ows:Identifier>offering_days</ows:Identifier>'
			+ '<ows:Title></ows:Title>' + '<wps:Data>' + '<wps:LiteralData>'
			+ days
			+ '</wps:LiteralData></wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>offering_id</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ offering
			+ '</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>image_width</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>500</wps:LiteralData>'
			+ '	</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>image_height</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>500</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '</wps:DataInputs>'
			+ '<wps:ResponseForm>'
			+ '<wps:ResponseDocument>'
			+ '<wps:Output asReference="true">'
			+ '<ows:Identifier>output_image</ows:Identifier>'
			+ '</wps:Output>'
			+ '</wps:ResponseDocument>'
			+ '</wps:ResponseForm>'
			+ '</wps:Execute>';

	var requestXML = $.parseXML(requestString);
	var xmlstr = requestXML.xml ? requestXML.xml : (new XMLSerializer())
			.serializeToString(requestXML);

	if (debug) {
		$("#resultLog").html(
				"Sending to " + serviceUrlString
						+ " this request:<br /><textarea>" + xmlstr
						+ "</textarea>");
	}

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

}

var handleResponse = function(data) {
	// var $er = $(data).find("ns\\:ExecuteResponse");
	// alert($er.text());

	var outputs = $(data).find("ns\\:Output").each(
			function() {
				$(this).find("ns\\:Reference").each(
						function() {

							var literalData = "lalalaaa"; // $(this).$attributes().href();
							if (beginsWith(literalData, "http://")
									&& endsWith(literalData, ".jpg"))
								$("#plot").html(
										"<img src='" + literalData + "' />");
						});
			});

	// TODO find link to script file
}

$(function() {

	$("#executeRequest").click(function() {
		var days = $("#slider-days").val();
		var offering = 'ATMOSPHERIC_TEMPERATURE';
		$("#resultLog").html("Days: " + days + " | Offering: " + offering);

		requestPlot(days, offering);
	});

	$("#resultLog").ajaxError(
			function(event, request, settings, exception) {
				$("#resultLog").html(
						"Error Calling: " + settings.url + "<br />HTPP Code: "
								+ request.status + "<br />Exception: "
								+ exception);
			});
});

$(document).ready(function() {
	$("#serviceUrl").html("<em>" + serviceUrlString + "</em>");
});