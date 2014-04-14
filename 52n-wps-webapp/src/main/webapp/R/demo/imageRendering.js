
var offering = 'WASSERSTAND_ROHDATEN';
var stationname = 'Bake';
var processIdentifier = 'org.n52.wps.server.r.demo.timeseriesPlot';
var outputIdentifier = 'timeseries_plot';

var requestPlot = function(requestedHours, requestedOffering, paramLoessSpan, requestedStationname) {
	var imageWidth = '700';
	var imageHeight = '500';
	var sosUrl = 'http://sensorweb.demo.52north.org/PegelOnlineSOSv2.1/sos';

	var requestString = '<?xml version="1.0" encoding="UTF-8"?><wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">'
			+ '<ows:Identifier>'
			+ processIdentifier
			+ '</ows:Identifier>'
			+ '<wps:DataInputs>'
			+ '<wps:Input><ows:Identifier>offering_hours</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ requestedHours
			+ '</wps:LiteralData></wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input><ows:Identifier>sos_url</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ sosUrl
			+ '</wps:LiteralData></wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>offering_id</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ requestedOffering
			+ '</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>offering_stationname</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ requestedStationname
			+ '</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>loess_span</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ paramLoessSpan
			+ '</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>image_width</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ imageWidth
			+ '</wps:LiteralData>'
			+ '	</wps:Data>'
			+ '</wps:Input>'
			+ '<wps:Input>'
			+ '<ows:Identifier>image_height</ows:Identifier>'
			+ '<ows:Title></ows:Title>'
			+ '<wps:Data>'
			+ '<wps:LiteralData>'
			+ imageHeight
			+ '</wps:LiteralData>'
			+ '</wps:Data>'
			+ '</wps:Input>'
			+ '</wps:DataInputs>'
			+ '<wps:ResponseForm>'
			+ '<wps:ResponseDocument>'
			+ '<wps:Output asReference="true">'
			//+ '<wps:Output asReference="false">'
			+ '<ows:Identifier>'
			+ outputIdentifier
			+ '</ows:Identifier>'
			+ '</wps:Output>'
			+ '</wps:ResponseDocument>'
			+ '</wps:ResponseForm>'
			+ '</wps:Execute>';

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

};

var showResponse = function(executeResponse) {
	var status = $(executeResponse).find("wps\\:Status");
	var statusText = $(status).find("wps\\:ProcessSucceeded").text();
	$("#resultLog").html("<div class=\"success\">" + statusText + "</div>");

	$(executeResponse)
			.find("wps\\:Output")
			.each(
					function() {
						// check if the output is the desired image
						if ($(this).find("ows\\:Identifier").text() == outputIdentifier) {
							// alert("Found: " + outputIdentifier);

							var title = $(this).find("ows\\:Title").text();

							$(this).find("wps\\:Reference").each(
									function() {

										var link = $(this).attr("href");
										// var mime_type = $(this)
										// .attr("mimeType");

										if (beginsWith(link, "http://")) {
											$("#plot").html(
													"<img src='" + link
															+ "' alt='" + title
															+ "' />");
										}

										$("#resultLog").append(
												"<div class=\"info\">" + link
														+ "</div>");
									});
						}
					});
};

$(function() {

	$("#executeRequest").click(function() {
		$("#plot").html("<!-- no data -->");
		
		var hours = $("#slider-hours").val();
		var span = $("#slider-loess-span").val();
		
		$("#resultLog").html("Hours: " + hours + " | Offering: " + offering + " | LOESS span: " + span);

		requestPlot(hours, offering, span, stationname);
	});

	$("#resultLog").ajaxError(
			function(event, request, settings, exception) {
				$("#resultLog").html(
						"<div class=\"warning\">Error Calling: " + settings.url
								+ "<br />HTPP Code: " + request.status
								+ "<br />Exception: " + exception + "</div>");
			});
});

$(document).ready(function(){  
	  $("#link_processdescription").attr("href", "../../WebProcessingService?Request=DescribeSensor&Service=WPS&version=1.0.0&Identifier=" + processIdentifier);
	  //alert($("#link_processdescription").attr("href"));
	});
