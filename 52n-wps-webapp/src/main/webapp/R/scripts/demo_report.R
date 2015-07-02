# Copyright (C) 2012 by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

myLog <- function(...) {
	cat(paste0("[pegel-report] ", Sys.time(), " > ", ..., "\n"))
}

myLog("Start script... ")

################################################################################
# About
#
# This R script creates a Sweave report about water gauge stations in Germany
# in a WPS.

################################################################################
# define metadata, resources, inputs, and outputs

#wps.des: id = demo.pegelReport, title = Gauge Report,
# abstract = create a pdf report for a water gauge analysis;
#wps.resource: pegel-report.Rnw, Sweave.sty;

#wps.in: id = station_name, type = string, title = Station Name
# abstract = Discover gauge station names here: http://pegelonline.wsv.de/gast/karte/standard_mini, 
# minOccurs = 1, maxOccurs = 1;

#wps.in: id = days, type = integer, title = Report duration
# abstract = The number of days the reports goes back in time, 
# value = 1,
# minOccurs = 0, maxOccurs = 1;

#wps.out: id = report, type = pdf, title = pegel-analysis report;
#wps.out: id = report_source, type = text, title = report source file,
# abstract = The source file to generate the report for reproducibility;

################################################################################
# constants and settings

report_file <- "pegel-report.Rnw"

process_description_url <- "N/A"
resource_url_report_file <- "N/A"

#print(get("lasttry"))
#print(bar)
#cat(get("wpsServer"), "\n")
#print(get("wpsProcessDescription"))

if(exists("wpsServer") && wpsServer) {
	myLog("Running in a WPS...")
	# get metadata when running in the server
	# cat(wpsResourceEndpoint, "\n")
	# cat(wpsProcessDescription, "\n")
	process_description_url <- wpsProcessDescription
	resource_url_report_file <- paste0(wpsResourceEndpoint, "/", report_file)
	
	myLog("wps.description: ", wpsProcessDescription,
				" | wps.resource: ", wpsResourceEndpoint)
}
else {
	myLog("NOT RUNNING ON SERVER!")
}


myLog("process description: ", process_description_url,
			" | report: ", report_file, 
			" | public URL: ", resource_url_report_file);

################################################################################
# save input variables for Rnw file

tPeriod_days <- days
procedure_filter <- station_name
myLog("tiem filter: ", tPeriod_days, " | procedures: ", procedure_filter)

################################################################################
# generate report

# wps.off; for local testing
files <- paste0(dirname(getwd()), "/resources/", c(report_file, "Sweave.sty"))
setwd(tempdir())
lapply(FUN = file.copy, X = files, to = getwd())
myLog(" LOCAL TESTING in wd ", getwd())
# wps.on;

myLog("Creating report with file ", report_file, " in ", getwd())
Sweave(report_file)
system("pdfLatex \"pegel-report.tex\"") #problem: doesn't run without interaction

report <- "pegel-report.pdf"
report_source <- resource_url_report_file

myLog("report file: ", report,
			" | report source: ", report_source, 
			" | public URL: ", resource_url_report_file);


myLog("Done!")