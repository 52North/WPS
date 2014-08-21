<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ul id="tabs" class="nav nav-tabs" data-tabs="tabs">
	<li class="active"><a href="#backup" data-toggle="tab">Backup</a></li>
	<li><a href="#restore" data-toggle="tab">Restore</a></li>
</ul>

<div id="my-tab-content" class="tab-content">
	<div class="tab-pane active" id="backup">
		<p class="topSpace">Select what do you want to backup. Selected items will be added to the backup Zip archive.</p>
		<form id="backup" class="form-horizontal" method="POST" action="<c:url value="/backup" />">
			<div class="checkbox topSpace">
				<label>
					<input type="checkbox" name="backupSelections" value="database">
					Server, Repositories, Generators, Parsers, and Users
				</label>
				<span class="help-block">
					Backup "/WEB-INF/classes/db/data".<br>Note: The database will be locked during backup.
				</span>
			</div>
			<div class="checkbox topSpace">
				<label>
					<input type="checkbox" name="backupSelections" value="log">
					Log
				</label>
				<span class="help-block">Backup "/WEB-INF/classes/logback.xml"</span>
			</div>
			<div class="checkbox topSpace">
				<label>
					<input type="checkbox" name="backupSelections" value="wpscapabilities">
					Service Identification / Service Provider
				</label>
				<span class="help-block">Backup "/config/wpsCapabilitiesSkeleton.xml"</span>
			</div>
			<div class="form-group">
				<div class="col-lg-7 topSpace">
					<button type="submit" class="btn btn-primary">Backup</button>
				</div>
			</div>
		</form>
		<p>
			<b>Backup Download:</b>
			<span id="zipUrl">Click the backup button to generate the backup file.</span>
		</p>
	</div>
	<div class="tab-pane" id="restore">
		<p class="topSpace">The content of the uploaded Zip archive will overwrite current configurations.</p>
		<p class="text-warning">WARNING: If you're restoring the database, you MUST restart the application to restart the
			database and resync the values.</p>
		<form id="upload" class="form-horizontal" method="POST" action="<c:url value="/backup/restore" />"
			enctype="multipart/form-data">
			<div class="form-group">
				<div class="col-lg-7">
					<label for="zipFile">Backup File</label>
					<input type="file" name="zipFile" id="zipFile">
					<p class="help-block">Please select a WPSBackup Zip archive</p>
				</div>
			</div>
			<div class="form-group">
				<div class="col-lg-7">
					<button type="submit" class="btn btn-primary">Restore</button>
				</div>
			</div>
		</form>
	</div>
</div>
<div id="result"></div>
<script src="<c:url value="/static/js/library/jquery.form.js" />"></script>
<script type="text/javascript">
	$('form#backup').submit(function(event) {

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
			success : function(xhr) {
				var savePath = xhr;
				var zipUrl = savePath.substring(savePath.lastIndexOf('\\') + 1);
				$('span#zipUrl').html("<a href='static/" + zipUrl + "'>Download (" + zipUrl + ")</a>");
			},
			error : function(xhr) {
				var link = $('span#zipUrl');
				var errorMessage = xhr.responseText;
				link.text(errorMessage).addClass('text-danger');
			}

		});
	});

	$('form#upload').submit(function(event) {

		event.preventDefault();

		$('#result').html('');
		var form = $(this);
		var url = form.attr('action');
		var formData = new FormData();
		formData.append("zipFile", zipFile.files[0]);
		// reset and clear errors
		form.find('div#fieldError').remove();
		$(".form-group").each(function() {
			$(this).removeClass("has-error");
		});

		$.ajax({
			url : url,
			data : formData,
			dataType : 'text',
			processData : false,
			contentType : false,
			type : 'POST',
			success : function(xhr) {
				var alertDiv = $("<div data-dismiss class='alert alert-success'>Backup Restored</div>");
				var closeBtn = $("<button>").addClass("close").attr("data-dismiss", "alert");
				closeBtn.appendTo(alertDiv).text("x");
				alertDiv.insertBefore(form);
			},
			error : function(xhr) {
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
	});
</script>