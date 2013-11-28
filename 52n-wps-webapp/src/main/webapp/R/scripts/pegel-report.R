# Copyright (C) 2012 by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

#wps.des: pegelReport, Creates a pdf report for water gauge analysis;
#wps.in: dummy, integer, value = 0;
#wps.out: report, pdf, pegel-analyse report;
#wps.out: report_source, string, report source file;
#wps.resource: pegel-report.Rnw, Sweave.sty;

# for local testing use 
# wpsServer <- FALSE

if(exists("wpsServer") && wpsServer) {
	# get metadata when running if the server
	# cat(wpsResourceURL, "\n")
	# cat(wpsProcessDescription, "\n")
	process_description_url <- wpsProcessDescription
	resource_url_rnw_file <- paste0(wpsResourceURL, "/", "pegel-report.Rnw")
	
	# download file from resources - alternative TODO: server copies resources to workdir
	sweave_input_file <- "pegel-report.Rnw"
	download.file(, sweave_input_file)
} else {
	process_description_url <- "N/A"
	resource_url_rnw_file <- "N/A"
	sweave_input_file <- "D:\\workspace-wps\\maven.1336393192191\\WPS\\52n-wps-webapp\\src\\main\\webapp\\R\\resources\\pegel-report.Rnw"
}

# generate report
Sweave(sweave_input_file)
system("pdfLatex \"pegel-report.tex\"") #problem: doesn't run without interaction
report="pegel-report.pdf"
report_source <- resource_url_rnw_file