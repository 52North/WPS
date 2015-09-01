#wps.des: id = test.session, title = Test script for session variables, 
# abstract = simply returns the session variables that should have been loaded 
# by the WPS into the R session;

#wps.resource: test/dummy1.txt, test/dummy2.png;

#wps.in: id = dummy, title = dummy input, abstract = dummy input - not used,
# type = string, value = 52N, minOccurs = 0, maxOccurs = 1;

wps <- wpsServer
#wps.out: wps, type = boolean, title = server flag,
# abstract = a flag that is true if the process is executed within a WPS server;

processdescription <- wpsProcessDescription
#wps.out: processdescription, type = string, title = process description,
# abstract = the link to the process description of this process;

servername <- wpsServerName
#wps.out: servername, type = string, title = server name,
# abstract = a name for the executing server of this process;

resourceurl <- wpsResourceEndpoint
#wps.out: resourceurl, type = string, title = resource base url,
# abstract = the base URL to access the resources of this process;

resources <- toString(wpsScriptResources)
#wps.out: resources, type = string, title = list of resources,
# abstract = a string listing the resources of this process;

scripturl <- wpsScriptUrl
#wps.out: scripturl, type = string, title = script url,
# abstract = the URL to access the original script of this process;