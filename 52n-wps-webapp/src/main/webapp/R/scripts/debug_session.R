#wps.des: id = debug.session, title = Debug WPS installation and configuration, 
# abstract = a script providing access to an extended session information that
# can be useful debugging service installations;

#wps.in: id = configuration, title = configuation name,
# abstract = give a name to the different executions of this script,
# type = string, value = 52N, minOccurs = 0, maxOccurs = 1;
#wps.off;
configuration <- "develop"
#wps.on;

# wpsSession variables:
#wps.out: wpsServer, type = boolean, title = server flag,
# abstract = a flag that is true if the process is executed within a WPS server;
#wps.out: wpsProcessDescription, type = string, title = process description,
# abstract = the link to the process description of this process;
#wps.out: wpsServerName, type = string, title = server name,
# abstract = a name for the executing server of this process;
#wps.out: wpsResourceEndpoint, type = string, title = resource base url,
# abstract = the base URL to access the resources of this process;

# capture function
mycapt <- function(x) {
    capturedOutput <- capture.output(x)
    return(paste(capturedOutput, collapse = "\n"))
}

# user information #############################################################
session.sessionInfo <- mycapt(sessionInfo())

# the library locations
library.variable <- mycapt(.Library)
library.variableSite <- mycapt(.Library.site)
library.paths <- mycapt(.libPaths())
#wps.out: id = library.variable, type = string, title = library info;
#wps.out: id = library.variableSite, type = string, title = library info;
#wps.out: id = library.paths, type = string, title = library info;

# system information ###########################################################
simpleList <- function(x) {
    x.names <- names(x)
    out <- NULL
    for(i in 1:length(x)) {
        out <- paste0(out, x.names[[i]], " = ", 
                      x[[i]], "; \n")
    }
    return(out)
}

system.env <- simpleList(Sys.getenv())
system.info <- simpleList(Sys.info())
system.pid <- mycapt(Sys.getpid())
system.locale <- mycapt(Sys.getlocale())
#wps.out: id = system.info, type = string, title = system info;
#wps.out: id = system.pid, type = string, title = system info;
#wps.out: id = system.locale, type = string, title = system info;
#wps.out: id = system.env, type = string, title = system info;

# all these variables all in one file ##########################################
.div <- "\n\n##################################################################"

debugfile <- "debug.md"
#file.remove(debugfile)
fileConn <- file(debugfile, encoding = "UTF-8")
cat("# WPS4R DEBUG SCRTIPT OUTPUT - ", configuration,
    "\n", date(), "\n",
    .div, "\n## Session\n", session.sessionInfo,
    .div, "\n## Libraries\n", 
    "\n### .Library\n", library.variable,
    "\n### .Library.site\n", library.variableSite,
    "\n### .libPaths()\n", library.paths,
    .div, "\n## System", 
    "\n### Sys.getenv()\n", system.env,
    "\n### Sys.info()\n", system.info,
    "\n### Sys.getpid()\n", system.pid,
    "\n### Sys.getlocale()\n", system.locale,
    sep = "",
    file = fileConn, fill = 80)
close(fileConn)

#wps.out: id = debugfile, type = text/x-markdown, title = debug information
# abstract = all debug information in one file;

