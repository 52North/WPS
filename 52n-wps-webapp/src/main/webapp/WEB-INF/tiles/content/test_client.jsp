<form id="requestForm" method="post" class="form-horizontal" action="">
	<div class="form-group">
		<label class="col-lg-2 control-label">Service URL</label>
		<div class="col-lg-10">
			<input class="form-control" name="url" id="serviceUrlField" value="./WebProcessingService" type="text" />
		</div>
	</div>
	<div class="form-group">
		<label class="col-lg-2 control-label">Request Examples</label>
		<div class="col-lg-10">
			<select id="selRequest" class="form-control">
				<option value=" "></option>
			</select>
		</div>
	</div>
	<div class="form-group">
		<pre class="editor"><textarea name="request" id="requestTextarea" class="form-control"></textarea></pre>
	</div>
	<div class="form-group">
		<button type="submit" class="btn btn-primary">Send</button>
		<button id="clearBtn" type="reset" class="btn btn-primary">Clear</button>
	</div>
</form>
<div class="form-horizontal">
	<div class="form-group">
		<pre><textarea name="request" id="responseTextarea"></textarea></pre>
	</div>
</div>

<script src="static/js/codemirror/codemirror.js" type="text/javascript"></script>

<script type="text/javascript">
	$(document).ready(
			function() {
				// derive service url from current location
				var urlIndex = window.location.href.lastIndexOf("/test_client");
				var urlBasisString = window.location.href.substring(0, (urlIndex + 1));
				var serviceUrlString = urlBasisString + "WebProcessingService";

				var datafolder = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1)
						+ "static/examples/requests/";

				initEditors();

				var placeholderIndex = "PLACEHOLDER";
				//load files
				var requests = new Array();
				requests[100] = datafolder + "GetCapabilities.xml";
				requests[101] = datafolder + "DescribeProcess.xml";

				requests[120] = datafolder + "/2.0.0/GetCapabilitiesRequest200.xml";
				requests[121] = datafolder + "/2.0.0/DescribeProcessRequest200.xml";
				requests[122] = datafolder + "/2.0.0/SimpleBufferExecute200.xml";

				requests[123] = datafolder + "SimpleBuffer.xml";
				requests[124] = datafolder + "Intersectionrequest.xml";
				requests[125] = datafolder + "Unionrequest.xml";

				var rasters = 200;
				requests[rasters] = datafolder + "r.contour_request_all_bands_out_gml.xml";
				requests[rasters + 1] = datafolder + "r.contour_request_all_bands_out_shp.xml";
				requests[rasters + 2] = datafolder + "r.contour_request_out_gml.xml";
				requests[rasters + 3] = datafolder + "r.contour_request_out_shp.xml";
				requests[rasters + 4] = datafolder + "r.los_request_out_img.xml";
				requests[rasters + 5] = datafolder + "r.los_request_out_png.xml";
				requests[rasters + 6] = datafolder + "r.los_request_out_tiff.xml";
				requests[rasters + 7] = datafolder + "r.neighbors_request.xml";
				requests[rasters + 8] = datafolder + "r.resample_request_out_tiff.xml";
				requests[rasters + 9] = datafolder + "r.resample_request_out_netcdf.xml";
				requests[rasters + 10] = datafolder + "r.to.vect_request_out_gml.xml";
				requests[rasters + 11] = datafolder + "r.to.vect_request_out_shp.xml";
				requests[rasters + 12] = datafolder + "r.watershed_request.xml";
				requests[rasters + 13] = datafolder + "r.math_request.xml";

				requests[300] = datafolder + "v.buffer_request_in_kml.xml";
				requests[301] = datafolder + "v.buffer_request_in_dgn.xml";
				requests[302] = datafolder + "v.buffer_request_out_gml.xml";
				requests[303] = datafolder + "v.buffer_request_out_kml.xml";
				requests[304] = datafolder + "v.buffer_request_out_shp.xml";
				requests[305] = datafolder + "v.delaunay_request_out_gml.xml";
				requests[306] = datafolder + "v.delaunay_request_out_shp.xml";
				requests[307] = datafolder + "v.hull_request_out_gml.xml";
				requests[308] = datafolder + "v.hull_request_out_kml.xml";
				requests[309] = datafolder + "v.hull_request_out_shp.xml";
				requests[310] = datafolder + "v.to.rast_request.xml";

				var rs = 400;
				requests[rs] = datafolder + "R_echo.xml";
				requests[rs + 1] = datafolder + "R_debug.xml";
				requests[rs + 2] = datafolder + "R_SessionVariables.xml";
				requests[rs + 3] = datafolder + "R_pegel-report.xml";
				requests[rs + 4] = datafolder + "R_pegel-report_pdf.xml";
				requests[rs + 5] = datafolder + "R_sweave-foo.xml";
				requests[rs + 6] = datafolder + "R_sweave-foo_pdf.xml";
				requests[rs + 7] = datafolder + "R_Idw.xml";
				requests[rs + 8] = datafolder + "R_image.xml";
				requests[rs + 10] = datafolder + "R_uniform.xml";
				requests[rs + 11] = datafolder + "R_meuse.xml";
				requests[rs + 12] = datafolder + "R_meuse2.xml";
				requests[rs + 13] = datafolder + "R_EO2H_AirQualitySaxony.xml";
				requests[rs + 14] = datafolder + "R_enviroCar_mapMatching.xml";
				requests[rs + 20] = datafolder + "R_SosPlot.xml";
				requests[rs + 20] = datafolder + "R_validateScript.xml";

				var mcs = 500;
				requests[mcs] = datafolder + "mc_echo.xml";

				//fill the select element
				var selRequest = $('#selRequest');

				l = requests.length;
				for ( var i = 0; i < l; i++) {
					var requestString = "";
					if (requests[i] == placeholderIndex) {
						//skip this one
					} else if (requests[i]) {
						try {
							var name = requests[i].substring(requests[i].lastIndexOf("/") + 1, requests[i].length);
							selRequest.append($("<option></option>").attr("value", requests[i]).text(name));
						} catch (err) {
							var txt = "";
							txt += "Error loading file: " + requests[i];
							txt += "Error: " + err + "\n\n";
							var requestTextarea = document.getElementById('requestTextarea').value = "";
							requestTextarea.value += txt;
						}
					} else {
						// request is null or empty string - do nothing
					}
				}

				// Put service url into service url field
				var serviceUrlField = document.getElementById("serviceUrlField");
				serviceUrlField.value = serviceUrlString;

				$('form#requestForm').submit(function(event) {
					event.preventDefault();
					var form = $(this);
					var requestTextareaValue = $('#requestTextarea').val();
					$.ajax({
						type : form.attr('method'),
						url : $("#requestForm input[name=url]").val(),
						data : requestTextareaValue,
						complete : function(xhr) {
							outputEditor.setCode(xhr.responseText);
						}
					});
				});

				$('#selRequest').change(function() {
					try {
						var selObj = $(this);
						var requestTextarea = $('#requestTextarea');
						var requestString = "";

						if ($('#selRequest').prop('selectedIndex') != 0) { // Handle selection of empty drop down entry.
							requestString = getFile(selObj.val());
						}

						if (requestString == null) {
							requestString = "Sorry! There is a problem, please refresh the page.";
						}

						inputEditor.setCode(requestString);

					} catch (err) {
						var txt = "";
						txt += "Error loading file: " + selObj.val();
						txt += "Error: " + err + "\n\n";
						requestTextarea.value += txt;
					}
				});
				
				$('#clearBtn').click(function(event){ 
					event.preventDefault();
					inputEditor.setCode('');
					outputEditor.setCode('');
					$('#selRequest').prop('selectedIndex', 0);
				});
			});

	function getFile(fileName) {
		oxmlhttp = null;
		try {
			oxmlhttp = new XMLHttpRequest();
			oxmlhttp.overrideMimeType("text/xml");
		} catch (e) {
			try {
				oxmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				return null;
			}
		}
		if (!oxmlhttp)
			return null;
		try {
			oxmlhttp.open("GET", fileName, false);
			oxmlhttp.send(null);
		} catch (e) {
			return null;
		}
		return oxmlhttp.responseText;
	}

	function initEditors() {
		var defaultInputString = "<!-- Insert your request here or select one of the examples from the menu above. -->";
		var defaultOutputString = "<!-- Output -->";

		inputEditor = CodeMirror.fromTextArea("requestTextarea", {
			height : "300px",
			parserfile : "parsexml.js",
			stylesheet : "static/js/codemirror/xmlcolors.css",
			path : "static/js/codemirror/",
			lineNumbers : true,
			content : defaultInputString
		});

		outputEditor = CodeMirror.fromTextArea("responseTextarea", {
			height : "300px",
			parserfile : "parsexml.js",
			stylesheet : "static/js/codemirror/xmlcolors.css",
			path : "static/js/codemirror/",
			lineNumbers : true,
			readOnly : true,
			content : defaultOutputString
		});
	}
</script>