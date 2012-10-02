# Copyright (C) 2012< by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

#wps.des: pegelReport, Creates a pdf report for water gauge analysis;
#wps.in: dummy, integer, value = 0;
#wps.out: report, pdf, pegel-analyse report;
#wps.resource: pegel-report.Rnw, Sweave.sty;

# can be inputs later:
tPeriod.days <- 1
offering_name <- "WASSERSTAND_ROHDATEN"
procedure_filter <- "*Wasserstand-Bake*"

cat(wpsResourceURL, "\n")
cat(wpsProcessDescription, "\n")
process_description_url <- wpsProcessDescription
resource_url_rnw_file <- paste0(wpsResourceURL, "/", "pegel-report.Rnw")
resource_url_script_file <- "url skript kommt noch"

# download file from resources - alternative TODO: server copies resources to workdir
download.file(resource_url_rnw_file, "pegel-report.Rnw")

# generate report
Sweave("pegel-report.Rnw")
system("pdfLatex \"pegel-report.tex\"") #proplem: doesn't run without interaction
report="pegel-report.pdf"