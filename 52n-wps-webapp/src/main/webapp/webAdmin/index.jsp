<%@ page import = "org.n52.wps.webadmin.ConfigUploadBean" %>
<%@ page import = "org.n52.wps.webadmin.ChangeConfigurationBean" %>

<jsp:useBean id="fileUpload" class="org.n52.wps.webadmin.ConfigUploadBean" scope="session"/>
<jsp:useBean id="changeConfiguration" class="org.n52.wps.webadmin.ChangeConfigurationBean" scope="session" />
<jsp:setProperty name="changeConfiguration" property="*"/>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE  PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%
fileUpload.doUpload(request);
%>

<html>
    <head>
        <link rel="stylesheet" href="css/ui.all.css" type="text/css" media="screen">
        <link type="text/css" rel="stylesheet" href="css/lightbox-form.css">
		
		<script src="resources/lightbox-form.js" type="text/javascript"></script>
        <script type="text/javascript" src="resources/jquery.js"></script>
        <script type="text/javascript" src="resources/jquery-ui.js"></script>
        <script type="text/javascript" src="resources/jquery.ajax_upload.js"></script>

        <script type="text/javascript"><!--
            // constants
            var itemListTypes = new Array("Generator","Parser","Repository");
            var itemListTypeNr = {"Generator":0,"Parser":1,"Repository":2};
            var relativeConfigPath = "../config/";
            var configurationFileName = "wps_config.xml";
            
            // upload req
            var uploadId = "";

            // at page load
            $(document).ready(function(){
                
                $("#Tabs > ul").tabs();
                $("#sections").accordion({
                    header: "div.accHeader",
                    fillSpace: false
                });

                new Ajax_upload('#upload_button', {
                  // Location of the server-side upload script
                  action: 'index.jsp',
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
                    openbox('Upload a WPS process', 1)
                   
                });

                $("#loadConfBtn").click(function(){
                    loadConfiguration(relativeConfigPath + configurationFileName);
                });

                $("#saveConfBtn").click(function(){
                    if (confirm("Save and Activate Configuration?")) {
                        $("input[name='serializedWPSConfiguraton']:first").val($("#form1").serialize());
                        $("#saveConfogurationForm").submit();
                    }
                });
                setTimeout
                loadConfiguration(relativeConfigPath + configurationFileName);
            });
            
            
            function uploadFiles() {
          	var uploadCheck = new Boolean(false);
            var extA = document.getElementById("processFile").value;
            var extB = document.getElementById("processDescriptionFile").value;
  			extA = extA.substring(extA.length-3,extA.length);
  			extA = extA.toLowerCase();
  			extB = extB.substring(extB.length-3,extB.length);
  			extB = extB.toLowerCase();
 			
			if(extA != 'ava' & extA != 'zip' | extB != 'xml' & extB != '')
  			{if (extA != 'ava' & extA != 'zip')
  				{alert('You selected a .'+extA+ ' file containing the process; please select a .java or .zip file instead!');
  				if (extB != 'xml' & extB != '') alert('You also selected a .'+extB+ ' file containing the process description; please select a .xml file instead!');
  				}
  				else{alert('You selected a .'+extB+ ' file containing the process description; please select a .xml file instead!');}
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
                var confFile = configFile + "?" + 1*new Date();

                $.get(confFile,{},function(xml){
                    var hostname = $("Server:first",xml).attr("hostname");
                    var hostport = $("Server:first",xml).attr("hostport");
                    var includeDataInputsInResponse = $("Server:first",xml).attr("includeDataInputsInResponse");
                    var computationTimeoutMilliSeconds = $("Server:first",xml).attr("computationTimeoutMilliSeconds");
                    var cacheCapabilites = $("Server:first",xml).attr("cacheCapabilites");
                    var webappPath = $("Server:first",xml).attr("webappPath");
                    
                    $("#Server_Settings input[name='Server-hostname']:first").val(hostname);
                    $("#Server_Settings input[name='Server-hostport']:first").val(hostport);
                    $("#Server_Settings input[name='Server-includeDataInputsInResponse']:first").val(includeDataInputsInResponse);
                    $("#Server_Settings input[name='Server-computationTimeoutMilliSeconds']:first").val(computationTimeoutMilliSeconds);
                    $("#Server_Settings input[name='Server-cacheCapabilites']:first").val(cacheCapabilites);
                    $("#Server_Settings input[name='Server-webappPath']:first").val(webappPath);
                    
                    for (itemType in itemListTypes ){
                        var listType = itemListTypes[itemType]
                        $("#"+listType+"_List").empty();
                        $(listType,xml).each(function(i) {
                            nameEntry = $(this).attr("name");
                            className = $(this).attr("className");
                            var itemID = addListItem(listType);
                            if (nameEntry == "UploadedAlgorithmRepository"){setUploadId(itemID);}
                            
                            $("#" + listType + "-" + itemID + "_NameEntry").val(nameEntry);
                            $("#" + listType + "-" + itemID + "_ClassEntry").val(className);
                            $('Property',this).each(function(j) {
                                propertyName = $(this).attr("name");
                                propertyValue = $(this).text();
                                var propID = addPropItem(listType + "-" + itemID + '_Property');
                                $("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_Name").val(propertyName);
                                $("#" + listType + "-" + itemID + "_Property" + "-" + propID + "_Value").val(propertyValue);
                            });
                        });
                    }
                });
            }

            function addListItem(itemType) {
          
                var id = document.getElementById("id").value;
                $("#"+itemType+"_List").append
                (
                "<p class=\"listItem\" id=\"" + itemType + "-" + id + "\">" +
                "<label for=\"" + itemType + "-" + id + "_NameEntry\">Name</label>"+
                "<label for=\"" + itemType + "-" + id + "_ClassEntry\">Class</label><br>" +
                "<input type=\"text\" name=\"" + itemType + "-" + id + "_Name\" id=\"" + itemType + "-" + id + "_NameEntry\" />" +
                "<input type=\"text\" name=\"" + itemType + "-" + id + "_Class\" id=\"" + itemType + "-" + id + "_ClassEntry\" />" +
                "<img onClick=\"removeItemList('" + itemType + "','" + id + "'); return false;\" src=\"images/min_icon.png\" width=\"14\" height=\"18\" alt=\"Remove\"/>"+
                "<br>" +
                "<div class=\"propList\" id=\""+ itemType + "-" + id +"_Property_List\">" +
                "<div class=\"propListHeader\">" +
                "<label class=\"propertyNameLabel\">Name</label>" +
                "<label class=\"propertyValueLabel\">Class</label>" +
                "<input Style=\"margin-left:16em\" type=\"button\" value=\"Add Property\" name=\"addPropButton\" onClick=\"addPropItem('" + itemType + "-" + id + "_Property'); return false;\"/>" +
                "</div>" +
                "</div>" +
                "</p>"
                );
                var newId = (id - 1) + 2;
                document.getElementById("id").value = newId;
                return id;
            }

            function addPropItem(itemType) {
                var id = document.getElementById("id").value;
                $("#" + itemType + "_List").append
                (
                "<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    "<input class=\"propertyName\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" />"+
                    "<input class=\"propertyValue\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" />"+
                    "<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"images/min_icon.png\" width=\"14\" height=\"18\" alt=\"Remove\"/>"+
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
            
            
                      
			function appendProcessToList() {                            			

			 itemType= "Repository-" + uploadId + "_Property";
			 listName= "Repository-" + uploadId + "_Property_List";
			 var id = document.getElementById("id").value;
			 processNameId = document.getElementById("processNameId").value;
			 algorithmName = "Algorithm";
             
             $("#"+listName).append("<div class=\"propItem\" id=\"" + itemType + "-" + id + "\">"+
                    	"<input class=\"propertyName\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Name\" id=\"" + itemType + "-" + id + "_Name\" value=\"" + algorithmName +"\" />"+
                    	"<input class=\"propertyValue\" type=\"text\" size=\"15\" name=\""+ itemType + "-" + id +"_Value\" id=\""+ itemType + "-" + id + "_Value\" value=\"" + processNameId + "\" />"+
                   	 	"<img onClick=\"removeItem('#"+ itemType + "-" + id + "'); return false;\" src=\"images/min_icon.png\" width=\"14\" height=\"18\" alt=\"Remove\"/>"+
                		"</div>");

                
            var newId = (id - 1) + 2;
            document.getElementById("id").value = newId;
            return id;
            }

	
        --></script>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>WPS Web Admin</title>
    </head>
    <body>
        <div style="height: 75px"><img style="float:left" src="images/52northlogo_small.png" alt="52northlogo_small"/><h1 Style="padding-left:3em; color:#4297d7; font-family: Lucida Grande, Lucida Sans, Arial, sans-serif; font-size: 3em;">Web Admin Console</h1></div>
        <div id="Tabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
            <ul>
                <li><a href="#tab-1"><span>WPS Config Configuration</span></a></li>
                <li><a href="#tab-2"><span>WPS Test Client</span></a></li>
            </ul>
            <div id="tab-1">
                <form action="#" method="post" id="saveConfogurationForm">
                    <input type="hidden" name="serializedWPSConfiguraton"/>
                </form>
                <form action="#" method="get" id="form1" onreset="return resetLisings()">
                    <table border="0" cellspacing="0">
                        <tr>
                            <td>
                                <input class="formButtons" id="saveConfBtn" type="button" value="Save and Activate Configuration" name="save"/>
                            </td>
                            <td>
                                <input class="formButtons" id="loadConfBtn" type="button" value="Load Active Configuration" name="load"/>
                            </td>
                            <td>
                                <input class="formButtons" id="upload_button" type="button" value="Upload Configuration File" name="upload"/>
                            </td>
                            <td>
                                <input class="formButtons" type="reset" value="Reset" name="Reset"/>
                            </td>
                            <td>
                                <input class="formButtons" id="upload_process" type="button" value="Upload Process" name="UploadProcess"/>
                            </td>
                        </tr>
                    </table>
                    <div id="sections">
                        <div class="section">
                            <div class="accHeader" style="text-indent: 40px">Server Settings</div>
                            <div class="sectionContent">
                                <div id="Server_Settings">
                                    <p><label for="Server-hostname" >Server Host Name:</label><input type="text" name="Server-hostname" value="testValue" /></p>
                                    <p><label for="Server-hostport" >Server Host Port:</label><input type="text" name="Server-hostport" value="testValue" /></p>
                                    <p><label for="Server-includeDataInputsInResponse" >Include Datainput:</label><input type="text" name="Server-includeDataInputsInResponse" value="boolean" /></p>
                                    <p><label for="Server-computationTimeoutMilliSeconds" >Computation Timeout:</label><input type="text" name="Server-computationTimeoutMilliSeconds" value="testValue" /></p>
                                    <p><label for="Server-cacheCapabilites" >Cache Capabilities:</label><input type="text" name="Server-cacheCapabilites" value="boolean" /></p>
                                    <p><label for="Server-webappPath" >Web app Path:</label><input type="text" name="Server-webappPath" value="testValue" /></p>
                                    <p></p>
                                </div>
                            </div>
                        </div>
                        <div class="section">
                            <div class="accHeader" style="text-indent: 40px">Algorithm Repositories</div>
                            <div class="sectionContent">
                                <input type="hidden" id="id" value="1">
                                <div class="lists" id="Repository_List"></div>
                                <p class="addListItem"><input type="button" value="Add Repository" name="addRepositoryButton" onClick="addListItem(itemListTypes[itemListTypeNr.Repository]); return false;"/></p>
                            </div>
                        </div>
                        <div class="section">
                            <div class="accHeader" style="text-indent: 40px">Parsers</div>
                            <div class="sectionContent">
                                <div class="lists" id="Parser_List"></div>
                                <p class="addListItem"><input type="button" value="Add Paser" name="addPaserButton" onClick="addListItem(itemListTypes[itemListTypeNr.Parser]); return false;"/></p>
                            </div>
                        </div>
                        <div class="section">
                            <div class="accHeader" style="text-indent: 40px">Generators</div>
                            <div class="sectionContent">
                                <div class="lists" id="Generator_List"></div>
                                <p class="addListItem"><input type="button" value="Add Generator" name="addGeneratorButton" onClick="addListItem(itemListTypes[itemListTypeNr.Generator]); return false;"/></p>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="tab-2">
                <div style="height: 400px">
                    Try the the request examples:
                    <a target="_blank" href="../WebProcessingService?Request=GetCapabilities&Service=WPS">GetCapabilities request</a>
                    <br><br>
                    WPS TestClient:
                    <br>
                    <table>
                        <tr>
                            <td>
                                <b>
                                    Server:
                                </b>
                            </td>
                            <td >
                                <form name="form1" method="post" action="">
                                    <div>
                                        <input name="url" value="../WebProcessingService" size="90" type="text">
                                    </div>
                                </form>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <b>
                                    Request:
                                </b>
                            </td>
                            <td>
                                <form name="form2" method="post" action="" enctype="text/plain">
                                    <div>
                                        <textarea name="request" cols="88" rows="15"></textarea>
                                    </div>
                                        <input value="   Clear    " name="reset" type="reset">
                                        <input value="   Send    " onclick="form2.action = form1.url.value" type="submit">
                                </form>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
        <!-- upload form -->
        <div id="filter"></div>
<div id="box">
  <span id="boxtitle"></span>
  <form method="post" action="index.jsp" enctype="multipart/form-data" onsubmit="return uploadFiles()">
       <input type="hidden" name="uploadProcess"/>
    <p>
		Please enter the fuly qualified name of the java class implementing IAlgorithm:<br>
		<input type="text" name="processName" size="30" id="processNameId" >
	</p>
	<p>
		Please specify the .java file for the process:<br>
		<input type="file" name="processFile" id="processFile" size="40" >
	</p>
	<p>
		Please specify the associated ProcessDescription .xml file (optional):<br>
		<input type="file" name="processDescriptionFile" id="processDescriptionFile" size="40" accept="text/xml">
	</p>
 	<p> 
      <input type="submit" name="submit" >
      <input type="reset" name="cancel" value="Cancel" onclick="closebox()" >
    </p>
    </form>
</div>

        
    </body>
</html>

