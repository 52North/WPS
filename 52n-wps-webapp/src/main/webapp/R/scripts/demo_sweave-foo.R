# Copyright (C) 2012< by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

# Based on Sweave file from http://users.stat.umn.edu/~geyer/Sweave/#exam

#wps.des: demo.sweaveFoo, Creates a pdf report based on a simple Sweave file;
#wps.in: dummy, integer, value = 0;
#wps.out: report, pdf, Sweave output file;
#wps.resource: sweave-foo.Rnw, Sweave.sty;

rnw_file <- "sweave-foo.Rnw"

# generate report
Sweave(rnw_file)

library(tools)
texi2dvi("sweave-foo.tex", pdf = TRUE)
report <- "sweave-foo.pdf"

#wps.out: report_source, text, Sweave source file content;
report_source <- rnw_file
#wps.out: report_source_copy, text, just another copy of the Sweave file;
report_source_copy <- rnw_file

#wps.out: report_source_link, string, reference link to Sweave source file;
report_source_link <- "NA"
if(exists("wpsResourceEndpoint"))
	report_source_link <- paste0(wpsResourceEndpoint, rnw_file)

# directly run the process with
# http://localhost:8080/wps/WebProcessingService?Request=Execute&Service=WPS&version=1.0.0&identifier=org.n52.wps.server.r.demo.sweaveFoo&DataInputs=dummy%3D42