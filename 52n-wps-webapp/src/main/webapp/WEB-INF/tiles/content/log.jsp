<%--

    Copyright (C) 2012-2015 52°North Initiative for Geospatial Open Source
    Software GmbH

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License version 2 as published
    by the Free Software Foundation.

    If the program is linked with libraries which are licensed under one of
    the following licenses, the combination of the program with the linked
    library is not considered a "derivative work" of the program:

        - Apache License, version 2.0
        - Apache Software License, version 1.0
        - GNU Lesser General Public License, version 3
        - Mozilla Public License, versions 1.0, 1.1 and 2.0
        - Common Development and Distribution License (CDDL), version 1.0

    Therefore the distribution of the program linked with libraries licensed
    under the aforementioned licenses, is permitted by the copyright holders
    if the distribution is compliant with both the GNU General Public
    License version 2 and the aforementioned licenses.

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags"%>

<p class="text-danger">${error}</p>

<form:form id="customForm" modelAttribute="logConfigurations" method="POST" action="log" class="form-horizontal">
	<legend>Patterns &amp; Formats</legend>
	<input:customInput label="File Name Pattern" field="wpsfileAppenderFileNamePattern" />
	<input:customInput label="File Encoder Pattern" field="wpsfileAppenderEncoderPattern" />
	<input:customInput label="Console Encoder Pattern" field="wpsconsoleEncoderPattern" />

	<legend>General</legend>
	<input:customInput label="Max History" field="wpsfileAppenderMaxHistory" desc="In days" />

	<legend>Appenders &amp; Loggers</legend>
	<div class="form-group">
		<form:label class="col-lg-3 control-label" path="rootLevel">Root Log Level</form:label>
		<div class="col-lg-6">
			<form:select class="form-control" path="rootLevel">
				<form:option value="DEBUG">DEBUG</form:option>
				<form:option value="INFO">INFO</form:option>
				<form:option value="WARN">WARN</form:option>
				<form:option value="ERROR">ERROR</form:option>
			</form:select>
		</div>
	</div>
	<div class="form-group">
		<form:label class="col-lg-3 control-label" path="rootLevel">Appenders</form:label>
		<div class="col-lg-6">
			<input:customInput label="File" field="fileAppenderEnabled" type="checkbox" val="wpsFile" />
			<input:customInput label="Console" field="consoleAppenderEnabled" type="checkbox" val="wpsConsole" />
		</div>
	</div>
	<form:label class="col-lg-3 control-label" path="loggers">Loggers</form:label>
	<div class="col-lg-7">
		<table id="loggertable" class="table table-hover table-condensed">
			<colgroup>
				<col class="col-lg-9">
				<col class="col-lg-3">
			</colgroup>
			<tbody>
				<c:forEach var="logger" items="${logConfigurations.loggers}">
					<tr>
						<td>${logger.key}</td>
						<td><form:select class="form-control" path="loggers['${logger.key}']" style="width:100px">
								<form:option value="DEBUG">DEBUG</form:option>
								<form:option value="INFO">INFO</form:option>
								<form:option value="WARN">WARN</form:option>
								<form:option value="ERROR">ERROR</form:option>
								<form:option value="OFF">OFF</form:option>
							</form:select></td>
				        </td>
				        <td><a id="deleteLogger" class="btn btn-danger btn-mini" />Delete</a>
				        </td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<a id="addLogger" class="btn btn-primary btn-lg" onclick="Add()" style="float: right" />New</a>
	</div>
	<input:customInput label="Save" type="submit" />
</form:form>
<script src="<c:url value="/static/js/custom.module.js" />"></script>
<script>
//prevent users from submitting the form with the enter key
$(document).ready(function() {
	  $(window).keydown(function(event){
	    if(event.keyCode == 13) {
	      event.preventDefault();
	      return false;
	    }
	  });
});
function Add(){
	var input = $("<input type='text' size='50' />");

	var td = $("<td></td>");
	
	var tr = $("<tr></tr>");
	
	var tdSelect = $("<td><select class=\"form-control\" style=\"width\:100px\">"
			+ "<option value=\"DEBUG\">DEBUG</option>"
			+ "<option value=\"INFO\">INFO</option>"
			+ "<option value=\"WARN\">WARN</option>"
			+ "<option value=\"ERROR\">ERROR</option>"
			+ "<option value=\"OFF\">OFF</option>"
			+ "</select></td>");
	
	var tdDelete = $("<td><a id=\"deleteLogger\" class=\"btn btn-danger btn-mini\" >Delete</a></td>");
	
	td.append(input);
	tr.append(td);
	tr.append(tdSelect);
	tr.append(tdDelete);

	$("#loggertable tbody")
			.append(tr);

	td.find('input').focus().blur(function(e) {
		var select = $(this).parent().parent().find('select');
		select.attr('id', "loggers'" + $(this).val() + "'");
		select.attr('name', "loggers['" + $(this).val() + "']");
		$(this).parent('td').text($(this).val());
	});
	//FIXME functions are not registered for new row
	//td.bind("click", editRow(td));
	//tdDelete.find('a').bind("click", deleteRow(tdDelete));
};
$('a#deleteLogger').click(function(event) {
    deleteRow($(this));
});
function deleteRow(td){
	event.preventDefault();
	var row = td.parents("tr");
	row.remove();
}
$("table td").click( function( e ){
    editRow($(this));    
});
function editRow(td){
    if ( td.find('input').length ) {
        return ;   
   }       
   if ( td.find('select').length ) {
       return ;   
  } 
   var input = $("<input type='text' size='50' />")
                     .val( td.text() );
       
   td.empty().append( input );
   
   td.find('input')
          .focus()
          .blur( function( e ){
                 var select = $(this).parent().parent().find('select');
                 select.attr('id', "loggers'" + $(this).val() + "'");
                 select.attr('name', "loggers['" + $(this).val() + "']");
                 $(this).parent('td').text( 
                    $(this).val()
                 );
     });
}
</script>