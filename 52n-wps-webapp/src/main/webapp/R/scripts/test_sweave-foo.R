# Copyright (C) 2012< by 52°North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This document is licensed under Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0), see http://creativecommons.org/licenses/by-sa/3.0/ for details.
# Author: Daniel Nuest (d.nuest@52north.org)

# Based on Sweave file from http://users.stat.umn.edu/~geyer/Sweave/#exam

#wps.des: test.sweaveFoo, Creates a pdf report based on a simple Sweave file;
#wps.in: dummy, integer, value = 0;
#wps.out: report, pdf, Sweave output file;
#wps.resource: sweave-foo.Rnw, Sweave.sty;

rnw_file <- "sweave-foo.Rnw"

# generate report
Sweave(rnw_file)

library(tools)
texi2dvi("sweave-foo.tex", pdf = TRUE)
report <- "sweave-foo.pdf"