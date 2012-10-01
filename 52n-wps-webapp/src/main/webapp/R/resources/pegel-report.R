# Copyright (C) 2012< by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

#wps.des: pegelReport, Creates a pdf report for pegel analyse;
#wps.in: dummy, integer, value = 0;
#wps.out: report, pdf, pegel-analyse report;
#wps.resource: pegel-report.Rnw, Sweave.sty;

#wps.off;
setwd("C:/Users/Matthias/Documents/R_reporting")
#wps.on;

Sweave("pegel-report.Rnw")
system("pdfLatex \"pegel-report.tex\"") #proplem: doesn't run without interaction