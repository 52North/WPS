<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wps" uri="http://52north.org/communities/geoprocessing/wps/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="org.n52.wps.webadmin.ConfigUploadBean"%>
<%@ page import="org.n52.wps.webadmin.ChangeConfigurationBean"%>

<!DOCTYPE PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<jsp:useBean id="fileUpload" class="org.n52.wps.webadmin.ConfigUploadBean" scope="session" />
<jsp:useBean id="changeConfiguration" class="org.n52.wps.webadmin.ChangeConfigurationBean" scope="session" />
<jsp:setProperty name="changeConfiguration" property="*" />

<% fileUpload.doUpload(request); %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>WPS Web Admin</title>

	<link type="text/css" rel="stylesheet" href="../static/css/ui.all.css" media="screen">
	<link type="text/css" rel="stylesheet" href="../static/css/lightbox-form.css">

	<script type="text/javascript"	src="../static/js/webadmin/lightbox-form.js"></script>
	<script type="text/javascript"	src="../static/js/webadmin/jquery.js"></script>
	<script type="text/javascript"	src="../static/js/webadmin/jquery-ui.js"></script>
	<script type="text/javascript" 	src="../static/js/webadmin/jquery.ajax_upload.js"></script>

	<script type="text/javascript"><!--
            // constants
            var itemListTypes = new Array("Generator","Parser","Repository","RemoteRepository");
            var itemListTypeNr = {"Generator":0,"Parser":1,"Repository":2,"RemoteRepository":3};
            var relativeConfigPath = "../config/";
            var configurationFileName = "file"; //"../${wps.config.file}";

            // upload req
            var uploadId = "";
            var WPS4RId = "";
            var WPS4RErrors = new Array();

            // at page load
            $(document).ready(function(){

                $("#Tabs > ul").tabs();
                $("#sections").accordion({
                    header: "div.accHeader",
                    fillSpace: false
                });

                new Ajax_upload('#upload_button', {
                  // Location of the server-side upload script
                  action: 'file', //'index.jsp',
                  // File upload name
                  name: 'userfile',
                  // Additional data to send
                  //data: {
                  //  example_key1 : 'example_value',
                  //  example_key2 : 'example_value2'
                  //},
                  // Fired when user selects file
                  // You can return false to cancel upload
                  // @param file basename of uploaded file
                  // @param extension of that file
                  onSubmit: function(file, extension) {
                  },
                  // Fired when file upload is completed
                  // @param file basename of uploaded file
                  // @param response server response
                  onComplete: function(file, response) {
                      loadConfiguration(relativeConfigPath + "<%=fileUpload.getFilenamePrefix()%>" + file);
                  }
                });


                $("#upload_process").click(function(){
                    openbox('Upload a WPS process', 1, 'box');
                });

				$("#manage_rem_repos").click(function(){
                    openbox('Manage Remote Repositories', 1, 'box');
                });

                <c:if test="${wps:hasR()}">
                $("#upload_r_script").click(function(){
                    openbox('Upload an R script', 1, 'box2');
                });
                </c:if>

                $("#loadConfBtn").click(function(){
                      <c:choose>
                          <c:when test="${wps:hasR()}">
                                loadConfiguration(configurationFileName);
                          </c:when>
                          <c:otherwise>
                                loadConfiguration(relativeConfigPath + configurationFileName);
                          </c:otherwise>
                      </c:choose>
                });

                $("#saveConfBtn").click(function(){
					// check if there are "unsaved" properties, because they can contain empty data
                	if($("img#saveEditImg").length > 0){
                		alert("There are unsaved properties, please save or delete them.");
					} else {
	                    if (confirm("Save and Activate Configuration?")) {
	                        $("input[name='serializedWPSConfiguraton']:first").val($("#form1").serialize());
	                        $("#saveConfogurationForm").submit();
	                    }
				    }
                });
                <c:choose>
                    <c:when test="${wps:hasR()}">
                          loadConfiguration(configurationFileName);
                    </c:when>
                    <c:otherwise>
                          loadConfiguration(relativeConfigPath + configurationFileName);
                    </c:otherwise>
                </c:choose>
            });

            function uploadFiles() {
	          	var uploadCheck = new Boolean(false);
	            var extA = document.getElementById("processFile").value;
	            var extB = document.getElementById("processDescriptionFile").value;
		  			extA = extA.substring(extA.length-3,extA.length);
		  			extA = extA.toLowerCase();
		  			extB = extB.substring(extB.length-3,extB.length);
		  			extB = extB.toLowerCase();

				if(extA !== 'ava' & extA !== 'zip' | extB !== 'xml' & extB !== '')
	  			{
		  			if (extA !== 'ava' & extA !== 'zip')
	  				{
		  				alert('You selected a .'+extA+ ' file containing the process; please select a .java or .zip file instead!');
	  				if (extB !== 'xml' & extB !== '') alert('You also selected a .'+extB+ ' file containing the process description; please select a .xml file instead!');
	  				}
	  				else{
		  				alert('You selected a .'+extB+ ' file containing the process description; please select a .xml file instead!');}
	  				uploadCheck=false;
	  			}
	  			else {
	  				uploadCheck=true;
	  			}

	  			if (uploadCheck)
	  			{
		  			appendProcessToList();
		  			$("input[name='serializedWPSConfiguraton']:first").val($("#form1").serialize());
		            $("#saveConfogurationForm").submit();
		            return true;
	            }
		  		return false;
           	}


            function loadConfiguration(configFile){
                // ensure not getting cached version
                var confFile = configFile; // + "?" + 1*new Date();

                $.get(confFile,{},function(xml){
                    var hostname = $("Server:first",xml).attr("hostname");
                    var hostport = $("Server:first",xml).attr("hostport");
                    var includeDataInputsInResponse = $("Server:first",xml).attr("includeDataInputsInResponse");
                    var computationTimeoutMilliSeconds = $("Server:first",xml).attr("computationTimeoutMilliSeconds");
                    var cacheCapabilites = $("Server:first",xml).attr("cacheCapabilites");
                    var webappPath = $("Server:first",xml).attr("webappPath");
                    var repoReloadInterval = $("Server:first",xml).attr("repoReloadInterval");

                    $("#Server_Settings input[name='Server-hostname']:first").val(hostname);
                    $("#Server_Settings input[name='Server-hostport']:first").val(hostport);
                    $("#Server_Settings input[name='Server-includeDataInputsInResponse']:first").val(includeDataInputsInResponse);
                    $("#Server_Settings input[name='Server-computationTimeoutMilliSeconds']:first").val(computationTimeoutMilliSeconds);
                    $("#Server_Settings input[name='Server-cacheCapabilites']:first").val(cacheCapabilites);
                    $("#Server_Settings input[name='Server-webappPath']:first").val(webappPath);
                    $("#Server_Settings input[name='Server-repoReloadInterval']:first").val(repoReloadInterval);

                    // display all algorithm repositories, parsers and generators
                    for (itemType in itemListTypes ){					// "Generator" / "Parser" / "Repository"
                        var listType = itemListTypes[itemType];
                        $("#"+listType+"_List").empty();				// clear the old entries
                        $(listType,xml).each(function(i) {
                            nameEntry = $(this).attr("name");
                            className = $(this).attr("className");
                            activeString = $(this).attr("active");

                            var active = true;
                            if(activeString === "false"){
								active = false;
                            }

                            var itemID;
                            <c:choose>
                                <c:when test="${wps:hasR()}">
                                       if (nameEntry === "LocalRAlgorithmRepository"){
                                            itemID = addListItemForWPS4R(listType);
                                            setWPS4RId(itemID);
                                        }else{
                                            itemID = addListItem(listType);
                                        }
                                </c:when>
                                <c:otherwise>
                                      itemID = addListItem(listType);
                                </c:otherwise>
                            </c:choose>

                            if (nameEntry === "UploadedAlgorithmRepository"){setUploadId(itemID);}


                            // now that the list item exists, add name, class and active to the elements
                            $("#" + listType + "-" + itemID + "_NameEntry").val(nameEntry);					// set the name entry
                            $("#" + listType + "-" + itemID + "_ClassEntry").val(className);				// set the class entry
                            $("#" + listType + "-" + itemID + "_Activator").attr('checked', active);		// set the active state

                            $('Property',this).each(function(j) {
                                propertyName = $(this).attr("name");
                                propertyValue = $(this).text();
                                propActiveString = $(this).attr("active");

                                var propActive = true;
                                if(propActiveString === "false"){
                                	propActive = false;
                                }

                                var propID = addPropItem(listType + "-" + itemID + '_Property');

                                // now that the property items exist, add name, value and active state
                                $("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_Name").val(propertyName);
                                $("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_Value").val(propertyValue);
                                $("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_Activator").attr('checked', propActive);

                                if(propertyName==="Algorithm" && itemID===WPS4RId){
                                	var flagID = propertyValue.replace(/\./g,"_")+"_flag";
									$("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_flag").attr('id',flagID);
                                }
                            });

                             $('Format',this).each(function(j) {
                                formatMime = $(this).attr("mimetype");
                                formatEnc = $(this).attr("encoding");

                                if(!formatEnc){
                                	formatEnc = "default";
                                }

                                formatSchem = $(this).attr("schema");

                                var formatID = addFormatItem(listType + "-" + itemID + '_Format');

                                // now that the property items exist, add name, value and active state
                                $("#" + listType + "-" + itemID + "_Format" + "-" + formatID + "_Mime").val(formatMime);
                                $("#" + listType + "-" + itemID + "_Format" + "-" + formatID + "_Enc").val(formatEnc);
                                $("#" + listType + "-" + itemID + "_Format" + "-" + formatID + "_Schem").val(formatSchem);
                            });

                            <c:if test="${wps:hasR()}">
                                setWPS4RValidityFlags();
                            </c:if>
                        });
                    }
                });
            }

            function addListItem(itemType) {
                var id = document.getElementById("id").value;
                if(itemType === itemListTypes[itemListTypeNr.RemoteRepository]){
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
						"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+
	                "</p>"
                );
                }else if((itemType === itemListTypes[itemListTypeNr.Parser]) || (itemType === itemListTypes[itemListTypeNr.Generator])){
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
						"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Class</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Formats <img id=\"minMax-"+ itemType + "-" + id + "_Format" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Format'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Format" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Format_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"formatMimeTypeLabel\" style=\"font-weight:bold;color:black;\">MimeType</label>" +
					                "<label class=\"formatEncodingLabel\" style=\"font-weight:bold;color:black;\">Encoding</label>" +
					                "<label class=\"formatSchemaLabel\" style=\"font-weight:bold;color:black;\">Schema</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewFormatItem('" + itemType + "-" + id + "_Format'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+

			            "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+

	                "</p>"
                );
                }else{
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
						"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Class</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+
	                "</p>"
                );
                }
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }


            function addNewListItem(itemType) {
                var id = document.getElementById("id").value;
                if(itemType === itemListTypes[itemListTypeNr.RemoteRepository]){
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
	                	"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" style=\"border:1px solid black;background-color:#F5F8F9;\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" checked style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;cursor:pointer;\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+
	                "</p>"
                );
                }else if((itemType === itemListTypes[itemListTypeNr.Parser]) || (itemType === itemListTypes[itemListTypeNr.Generator])){
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
						"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" style=\"border:1px solid black;background-color:#F5F8F9;\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Class</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" style=\"border:1px solid black;background-color:#F5F8F9;\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" checked style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Formats <img id=\"minMax-"+ itemType + "-" + id + "_Format" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Format'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Format" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Format_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"formatMimeTypeLabel\" style=\"font-weight:bold;color:black;\">MimeType</label>" +
					                "<label class=\"formatEncodingLabel\" style=\"font-weight:bold;color:black;\">Encoding</label>" +
					                "<label class=\"formatSchemaLabel\" style=\"font-weight:bold;color:black;\">Schema</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewFormatItem('" + itemType + "-" + id + "_Format'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+

			            "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+

	                "</p>"
                );
                }else{
                $("#"+itemType+"_List").append
                (
	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
	                	"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
						"<table class=\"nameClass\">"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" style=\"border:1px solid black;background-color:#F5F8F9;\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Class</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" style=\"border:1px solid black;background-color:#F5F8F9;\" /></td></tr>"+
							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" checked style=\"width:0\" /></td></tr>"+
						"</table>"+

		                "<br>" +

		                "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;cursor:pointer;\" />"+
						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
				                "<div class=\"propListHeader\">" +
					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
				                "</div>" +
			                "</div>" +
			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
			            "</div>"+
	                "</p>"
                );
                }
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function removeList(id){
            	$("p#" + id).remove();
            	$("div#maximizer-" + id).remove();
            }

            function maximize_minimize(id){
				var div = $("div#maximizer-" + id);
				if(div.css("display") === "none"){
					div.show("fast");
					$("img#minMax-"+ id).attr("src","../static/images/minimize.gif");
				} else {
					div.hide("fast");
					$("img#minMax-"+ id).attr("src","../static/images/maximize.gif");
				}
            }

            function addPropItem(itemType) {
                var id = document.getElementById("id").value;

                //Assigns a special behaviour to WPS4R properties (esp. for scripts):
                if(itemType === "Repository-"+WPS4RId+"_Property"){
                $("#" + itemType + "_List").append
                (
                "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    "<input type=\"text\" class=\"propertyName\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" readonly />"+
                    "<input type=\"text\" class=\"propertyValue\" size=\"20\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" readonly />"+
					"<input type=\"checkbox\" name=\"" + itemType + "-" + id +"_Activator\" id=\"" + itemType + "-" + id +"_Activator\" />" +
                    "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/del.png\" width=\"16\" height=\"16\" alt=\"Remove\" style=\"cursor:pointer\" />"+
                    "<img id=\"editImg\" onClick=\"edit('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/edit.png\" alt=\"Edit\" style=\"cursor:pointer\" />"+
                    "<div class=\"validity_flag\" id=\""+ itemType + "-" + id +"_flag\"></div>"+
                "</div>"
                );
                }else{
                    $("#" + itemType + "_List").append
                    (
                    "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                        "<input type=\"text\" class=\"propertyName\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" readonly />"+
                        "<input type=\"text\" class=\"propertyValue\" size=\"20\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" readonly />"+
    					"<input type=\"checkbox\" name=\"" + itemType + "-" + id +"_Activator\" id=\"" + itemType + "-" + id +"_Activator\" />" +
                        "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/del.png\" width=\"16\" height=\"16\" alt=\"Remove\" style=\"cursor:pointer\" />"+
                        "<img id=\"editImg\" onClick=\"edit('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/edit.png\" alt=\"Edit\" style=\"cursor:pointer\" />"+
                    "</div>"
                    );
                }
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function addNewPropItem(itemType) {
                var id = document.getElementById("id").value;
                $("#" + itemType + "_List").append
                (
                "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    "<input type=\"text\" class=\"propertyName\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" style=\"border:1px solid black;background-color:#F5F8F9;\" />"+
                    "<input type=\"text\" class=\"propertyValue\" size=\"20\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" style=\"border:1px solid black;background-color:#F5F8F9;\" />"+
					"<input type=\"checkbox\" name=\"" + itemType + "-" + id +"_Activator\" id=\"" + itemType + "-" + id +"_Activator\" checked />" +
                    "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/del.png\" alt=\"Remove\" style=\"cursor:pointer\" />"+
                    "<img id=\"saveEditImg\" onClick=\"saveEdit('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/save.png\" alt=\"Save edit\" style=\"cursor:pointer\" />"+
                "</div>"
                );
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function addFormatItem(itemType) {
                var id = document.getElementById("id").value;
                $("#" + itemType + "_List").append
                (
                "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    "<input type=\"text\" class=\"formatMimeType\" size=\"20\" name=\""+ itemType + "-" + id +"_Mime\" id=\"" + itemType + "-" + id + "_Mime\" readonly />"+
                    "<input type=\"text\" class=\"formatEncoding\" size=\"20\" name=\""+ itemType + "-" + id +"_Enc\" id=\""+ itemType + "-" + id + "_Enc\" readonly />"+
                    "<input type=\"text\" class=\"formatSchema\" size=\"20\" name=\""+ itemType + "-" + id +"_Schem\" id=\""+ itemType + "-" + id + "_Schem\" readonly />"+
                    "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/del.png\" width=\"16\" height=\"16\" alt=\"Remove\" style=\"cursor:pointer\" />"+
                    "<img id=\"editImg\" onClick=\"edit('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/edit.png\" alt=\"Edit\" style=\"cursor:pointer\" />"+
                "</div>"
                );
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function addNewFormatItem(itemType) {
                var id = document.getElementById("id").value;
                $("#" + itemType + "_List").append
                (
                "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    "<input type=\"text\" class=\"formatMimeType\" size=\"20\" name=\""+ itemType + "-" + id +"_Mime\" id=\"" + itemType + "-" + id + "_Mime\" style=\"border:1px solid black;background-color:#F5F8F9;\" />"+
                    "<input type=\"text\" class=\"formatEncoding\" size=\"20\" name=\""+ itemType + "-" + id +"_Enc\" id=\""+ itemType + "-" + id + "_Enc\" style=\"border:1px solid black;background-color:#F5F8F9;\" />"+
                    "<input type=\"text\" class=\"formatSchema\" size=\"20\" name=\""+ itemType + "-" + id +"_Schem\" id=\""+ itemType + "-" + id + "_Schem\" style=\"border:1px solid black;background-color:#F5F8F9;\" />"+
                    "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/del.png\" alt=\"Remove\" style=\"cursor:pointer\" />"+
                    "<img id=\"saveEditImg\" onClick=\"saveEdit('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/save.png\" alt=\"Save edit\" style=\"cursor:pointer\" />"+
                "</div>"
                );
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function removeItemList(listType,id) {
                $("#" + listType + "-" + id).remove();
                $("#" + listType + "-" + id + "_Property_List").remove();
            }

            function removeItem(id) {
                $(id).remove();
            }

            function resetLisings(){
                if (confirm("Reset Form data?")) {
                    for (itemListType in itemListTypes) {
                        $("#"+ itemListTypes[itemListType] +"_List").empty();
                    }
                    return true;
                } else {
                    return false;
                }
            }

            function setUploadId(itemID){
             	uploadId = itemID;
            }

            <c:if test="${wps:hasR()}">
                function setWPS4RId(itemID){
                    WPS4RId = itemID;
                }
            </c:if>

			function appendProcessToList() {
				 itemType= "Repository-" + uploadId + "_Property";
				 listName= "Repository-" + uploadId + "_Property_List";
				 var id = document.getElementById("id").value;
				 processNameId = document.getElementById("processNameId").value;
				 algorithmName = "Algorithm";

	             $("#"+listName).append("<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
	                    	"<input class=\"propertyName\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" value=\"" + algorithmName +"\" />"+
	                    	"<input class=\"propertyValue\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" value=\"" + processNameId + "\" />"+
	                   	 	"<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"../static/images/min_icon.png\" width=\"14\" height=\"18\" alt=\"Remove\"/>"+
	                		"</div>");


	            var newId = (id - 1) + 2;
	            document.getElementById("id").value = newId;
	            return id;
            }

            function edit(id){
                 // change the css
            	 $(id+"> input").css({	"border":"0.1em solid #4297D7",
                	 					"background-color":"#F5F8F9"
                 });
				 // remove the readonly attribute
				 $(id+"> input").removeAttr("readonly");

                 // append the save button
            	 $(id+" > img#editImg").remove();
            	 $(id).append($("<img id=\"saveEditImg\" onClick=\"saveEdit('"+ id + "'); return false;\" src=\"../static/images/save.png\" alt=\"Save edit\" style=\"cursor:pointer\" />"));
            }

            function saveEdit(id){
            	$(id+"> input").css({	"border":"none",
                						"background-color":"#CDE2ED"
                });

            	$(id+"> input").attr("readonly", "readonly");

            	$(id+" > img#saveEditImg").remove();
            	$(id).append($("<img id=\"editImg\" onClick=\"edit('"+ id + "'); return false;\" src=\"../static/images/edit.png\" alt=\"Edit\" style=\"cursor:pointer\" />"));
            }

            function editServerSettings(){
				// display warnings
				$("div#editWarn").show();

                // change the css
	            $("div#Server_Settings input").css({	"border":"0.1em solid #4297D7",
	               	 									"background-color":"#F5F8F9"
	            });
				// remove the readonly attribute
				$("div#Server_Settings input").removeAttr("readonly");

	            // append the save button
	           	$("div#editSave img#editImg").remove();
	           	$("div#editSave").append($("<img id=\"editImg\" onClick=\"saveEditServerSettings(); return false;\" src=\"../static/images/save.png\" alt=\"Save edit\" style=\"cursor:pointer\" />"));
            }

			function saveEditServerSettings(){
				// hide warnings
				$("div#editWarn").hide();

                // change the css
	            $("div#Server_Settings input").css({	"border":"none",
														"background-color":"#CDE2ED"
	            });
				// remove the readonly attribute
				$("div#Server_Settings input").attr("readonly", "readonly");

	            // append the save button
	           	$("div#editSave img#editImg").remove();
	           	$("div#editSave").append($("<img id=\"editImg\" onClick=\"editServerSettings(); return false;\" src=\"../static/images/edit.png\" alt=\"Save edit\" style=\"cursor:pointer\" />"));
            }

            <c:if test="${wps:hasR()}">
            function addListItemForWPS4R(itemType){
                var id = document.getElementById("id").value;
                 $("#"+itemType+"_List").append
                    (
    	                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
    						"<img src=\"../static/images/del.png\" onClick=\"removeList('"+ itemType + "-" + id + "')\" />"+
    						"<table class=\"nameClass\">"+
    							"<tr><td style=\"font-weight:bold; padding-right:15px\">Name</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" /></td></tr>"+
    							"<tr><td style=\"font-weight:bold; padding-right:15px\">Class</td><td><input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" /></td></tr>"+
    							"<tr><td style=\"font-weight:bold; padding-right:15px\">Active</td><td><input type=\"checkbox\" name=\"" + itemType + "-" + id + "_Activator\" id=\""+ itemType + "-" + id + "_Activator\" style=\"width:0\" /></td></tr>"+
    						"</table>"+

    		                "<br><br>" +

    		                "Properties <img id=\"minMax-"+ itemType + "-" + id + "_Property" + "\" src=\"../static/images/maximize.gif\" onClick=\"maximize_minimize('" + itemType + "-" + id + "_Property'); return false;\" style=\"padding-left:3em;\" style=\"cursor:pointer\" />"+
    						"<div id=\"maximizer-"+ itemType + "-" + id + "_Property" + "\" style=\"display:none;\">"+
    			                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
    				                "<div class=\"propListHeader\">" +
    					                "<label class=\"propertyNameLabel\" style=\"font-weight:bold;color:black;\">Name</label>" +
    					                "<label class=\"propertyValueLabel\" style=\"font-weight:bold;color:black;\">Value</label>" +
    				                "</div>" +
    			                "</div>" +
    			                "<div class=\"propEnd\"><img onClick=\"addNewPropItem('" + itemType + "-" + id + "_Property'); return false;\" src=\"../static/images/add.png\" alt=\"Add\" style=\"cursor:pointer\" /></div>"+
    			            "</div>"+
    	                "</p>"
                    );

                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

			function rProcessInfo(options){
				this.algorithmName = options.algorithmName;
				this.isValid = options.isValid;
				this.isAvailable = options.isAvailable;
				this.exception = options.exception;
				this.scriptURL = options.scriptURL;
			}


			function setWPS4RValidityFlags(){
				var rProcessInfos = new Array();

                <c:forEach var="rProcessInfo" items="${org.n52.wps.server.r.info.RProcessInfo.getRProcessInfoList()}">
                        rProcessInfos.push(new rProcessInfo({
                            algorithmName: "${rProcessInfo.getWkn()}",
                            isAvailable: "${rProcessInfo.isAvailable()}",
                            isValid: "${rProcessInfo.isValid()}",
                            scriptURL: "${rProcessInfo.getScriptURL()}",
                            exception: "${ProcessInfo.getLastErrormessage()}";
                        });
                </c:forEach>

				for(var i = 0; i<rProcessInfos.length; i++){
					var flagId = rProcessInfos[i].algorithmName.replace(/\./g,"_")+"_flag";
					if(rProcessInfos[i].isValid){
						$("#"+flagId).append(
								"<img class=\"flagIcon\" src=\"../static/images/script_valid.png\" alt=\"Script is valid\" title=\"Script is valid\" style=\"background-color:transparent\"></img>"
							);
					}else
						if(!rProcessInfos[i].isAvailable){
							var message = rProcessInfos[i].exception;
							WPS4RErrors[i] = message;
							$("#"+flagId).append(
									"<img class=\"flagIcon\" src=\"../static/images/script_missing.png\" alt=\"Script not available\" title=\"Script not available\" style=\"background-color:transparent; cursor:pointer\" onclick=alert(WPS4RErrors["+i+"])></img>"
								);
						}
					else{
						var message = rProcessInfos[i].exception;
						WPS4RErrors[i] = message;
 						text = 	"<img class=\"flagIcon\" src=\"../static/images/script_invalid.png\" alt=\"Script not valid\" "
 							+ "title=\"Script is not valid, click here to see the last errormessage\" style=\"background-color:transparent; cursor:pointer\" onclick=alert(WPS4RErrors["+i+"])></img>";
 						$("#"+flagId).append(text);

						}

				}
				//TODO: insert exceptions

			}

            </c:if>
        -->
    </script>
</head>
<body>

	<div style="height: 75px">
		<img style="float: left" src="../static/images/52northlogo_small.png" alt="52northlogo_small" />
		<h1	style="padding-left: 3em; color: #4297d7; font-family: Lucida Grande, Lucida Sans, Arial, sans-serif; font-size: 3em;">Web Admin Console</h1>
	</div>
	<div id="Tabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
		<div id="tab-1">
			<!-- <form action="#" method="post" id="saveConfogurationForm"> -->
				<form action="file" method="post" id="saveConfogurationForm">
				<input type="hidden" name="serializedWPSConfiguraton" />
			</form>
			<form action="#" method="get" id="form1" onreset="return resetLisings()">
				<table border="0" cellspacing="0">
					<tr>
						<td><input class="formButtons" id="saveConfBtn" type="button" value="Save and Activate Configuration" name="save" style="border:1px solid black;background:white;" /></td>
						<td><input class="formButtons" id="loadConfBtn" type="button" value="Load Active Configuration" name="load" style="border:1px solid black;background:white;" /></td>
						<td><input disabled="disabled" class="formButtons" id="upload_button" type="button" value="Upload Configuration File" name="upload" style="border:1px solid black;background:white;" /></td>
						<td><input class="formButtons" type="reset" value="Reset" name="Reset" style="border:1px solid black;background:white;" /></td>
						<td><input class="formButtons" id="upload_process" type="button" value="Upload Process" name="UploadProcess" style="border:1px solid black;background:white;" /></td>
						<!--td><input class="formButtons" id="manage_rem_repos" type="button" value="Update Remote Repositories" name="ManageRemoteRepositories" style="border:1px solid black;background:white;" /></td-->
                        <c:if test="${wps:hasR()}">
                            <td><input class="formButtons" id="upload_r_script" type="button" value="Upload R Script" name="UploadRScript" style="border:1px solid black;background:white;" /></td>
                        </c:if>
					</tr>
				</table>
				<div id="sections">
					<div class="section">
						<div class="accHeader" style="text-indent: 40px">Server Settings</div>

						<div class="sectionContent">
							<div id="Server_Settings">
								<div id="editSave" style="float:right;"><img id="editImg" src="../static/images/edit.png" onClick="editServerSettings()" style="cursor:pointer;" /></div>
								<p>
									<label for="Server-hostname">Server Host Name:</label><div id="editWarn" style="float: left;display: none; padding-right: 10px;"><img src="../static/images/warn.png" /> Changes only after restart</div>
									<input type="text" name="Server-hostname" value="testValue" readonly/>
									<br style="clear:left;" />
								</p>
								<p>
									<label for="Server-hostport">Server Host Port:</label><div id="editWarn" style="float: left;display: none; padding-right: 10px;"><img src="../static/images/warn.png" /> Changes only after restart</div>
									<input type="text" name="Server-hostport" value="testValue" readonly/>
									<br style="clear:left;" />
								</p>
								<p>
									<label for="Server-includeDataInputsInResponse">Include Datainput:</label>
									<input type="text" name="Server-includeDataInputsInResponse" value="boolean" readonly/>
								</p>
								<p>
									<label for="Server-computationTimeoutMilliSeconds">Computation Timeout:</label>
									<input type="text" name="Server-computationTimeoutMilliSeconds" value="testValue" readonly/>
								</p>
								<p>
									<label for="Server-cacheCapabilites">Cache Capabilities:</label>
									<input type="text" name="Server-cacheCapabilites" value="boolean" readonly/>
								</p>
								<p>
									<label for="Server-webappPath">Web app Path:</label><div id="editWarn" style="float: left;display: none; padding-right: 10px;"><img src="../static/images/warn.png" /> Changes only after restart</div>
									<input type="text" name="Server-webappPath" value="testValue" readonly/>
									<br style="clear:left;" />
								</p>
								<p>
									<label for="Server-repoReloadInterval">Repository Reload Interval: <br/> (In hours. 0 = No Auto Reload)</label><div id="editWarn" style="float: left;display: none; padding-right: 10px;"><img src="../static/images/warn.png" /> Changes only after restart</div>
									<input type="text" name="Server-repoReloadInterval" value="0" readonly/>
									<br style="clear:left;" />
								</p>
								<p></p>
							</div>
						</div>
					</div>
					<div class="section">
						<div class="accHeader" style="text-indent: 40px">Algorithm Repositories</div>
						<div class="sectionContent">
							<input type="hidden" id="id" value="1">
							<div class="lists" id="Repository_List"></div>
							<p class="addListItem">
								<input type="button" value="Add Repository" name="addRepositoryButton" onClick="addNewListItem(itemListTypes[itemListTypeNr.Repository]); return false;" style="border:1px solid black;background:white;" />
							</p>
						</div>
					</div>
					<div class="section">
						<div class="accHeader" style="text-indent: 40px">Parsers</div>
						<div class="sectionContent">
							<div class="lists" id="Parser_List"></div>
							<p class="addListItem">
								<input type="button" value="Add Parser" name="addParserButton" onClick="addNewListItem(itemListTypes[itemListTypeNr.Parser]); return false;" style="border:1px solid black;background:white;" />
							</p>
						</div>
					</div>
					<div class="section">
						<div class="accHeader" style="text-indent: 40px">Generators</div>
						<div class="sectionContent">
							<div class="lists" id="Generator_List"></div>
							<p class="addListItem">
								<input type="button" value="Add Generator" name="addGeneratorButton" onClick="addNewListItem(itemListTypes[itemListTypeNr.Generator]); return false;"  style="border:1px solid black;background:white;" />
							</p>
						</div>
					</div>
					<div class="section">
						<div class="accHeader" style="text-indent: 40px">Remote Repositories</div>
						<div class="sectionContent">
							<div class="lists" id="RemoteRepository_List"></div>
							<p class="addListItem">
								<input type="button" value="Add Remote Repository" name="addRemoteRepositoryButton" onClick="addNewListItem(itemListTypes[itemListTypeNr.RemoteRepository]); return false;"  style="border:1px solid black;background:white;" />
							</p>
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>

	<!-- upload form -->

	<div id="filter"></div>
	<div id="box">
		<span id="boxtitle"></span>
		<form method="post" action="index.jsp" enctype="multipart/form-data" onsubmit="return uploadFiles()">
			<input type="hidden" name="uploadProcess" />
			<p>
				Please enter the fuly qualified name of the java class implementing IAlgorithm:<br>
				<input type="text" name="processName" size="30" id="processNameId">
			</p>
			<p>
				Please specify the .java file for the process:<br>
				<input type="file" name="processFile" id="processFile" size="40">
			</p>
			<p>
				Please specify the associated ProcessDescription .xml file
				(optional):<br>
				<input type="file" name="processDescriptionFile" id="processDescriptionFile" size="40" accept="text/xml">
			</p>
			<p>
				<input type="submit" name="submit">
				<input type="reset" name="cancel" value="Cancel" onclick="closebox('box')">
			</p>
		</form>
	</div>
    <c:if test="${wps:hasR()}">
        <div id="box2">
            <span id="boxtitle"></span>
            <form method="post" action="index.jsp" enctype="multipart/form-data" onsubmit="return uploadRFiles()">
                <input type="hidden" name="uploadRscript" />
                <p>
                    Please enter the process name:<br>
                    (only if process name should be unlike filename)<br><br>
                    <input type="text" name="rProcessName" size="30" id="rProcessNameId">
                </p>
                <p>
                    Please enter the location of an annotated R script<br>
                    <br>
                    <input type="file" name="rProcessFile" id="rProcessFile" size="40">
                </p>

                <!--processDescriptionFile currently has no meaning, so it's just hidden-->
                <input type="hidden" name=""rProcessDescriptionFile" id="rProcessDescriptionFile" size="40" accept="text/xml">
                <!--<p>
                    Please specify the associated ProcessDescription .xml file
                    (optional, not yet implemented):<br>
                    <br>
                    <input type="file" name=""rProcessDescriptionFile" id="rProcessDescriptionFile" size="40" accept="text/xml">

                </p>-->
                <p>
                    <input type="submit" name="submit">
                    <input type="reset" name="cancel" value="Cancel" onclick="closebox('box2')">
                    <br>
                    <br>
                    <I>Process id will be org.n52.wps.server.r.[filename]
                        or org.n52.wps.server.r.[process name]</I>
                </p>
            </form>
        </div>

        <!-- <div id="RSessionInfoBox" style="display:none;">
            <span id="boxtitle"></span>
            <iframe width="600px" height="400px" src="../not_available"></iframe><br><br>
            <input type="button" name="OK" value="OK" onclick="closebox('RSessionInfoBox')">
        </div>-->
    </c:if>
</body>
</html>