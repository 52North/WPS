# Copyright (C) 2014 52°North Initiative for Geospatial Open Source
# Software GmbH
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# If the program is linked with libraries which are licensed under one of
# the following licenses, the combination of the program with the linked
# library is not considered a "derivative work" of the program:
#		
#		• Apache License, version 2.0
#		• Apache Software License, version 1.0
#		• GNU Lesser General Public License, version 3
#		• Mozilla Public License, versions 1.0, 1.1 and 2.0
#		• Common Development and Distribution License (CDDL), version 1.0
#
# Therefore the distribution of the program linked with libraries licensed
# under the aforementioned licenses, is permitted by the copyright holders
# if the distribution is compliant with both the GNU General Public
# License version 2 and the aforementioned licenses.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.		
###############################################################################		

wpsProgressVariable <- "wpsProgress"
wpsProgress <- 0
wpsProgressRange <- c(0, 100)

wpsProgressLogging <- TRUE

wpsProgressEnv <- new.env()

isWpsProgressLogSupported <- function() {
	return(exists(wpsProgressVariable));
}

wpsProgressLog <- function(...) {
	if(wpsProgressLogging) cat("[wps progress]", toString(Sys.time()), " > ",
														 ..., "\n")
}

wpsSetProgress <- function(progress) {
	assign(wpsProgressVariable, progress, envir = wpsProgressEnv)
	wpsProgressLog("set to ", progress)
}

wpsGetProgress <- function(progress) {
	.p <- get(wpsProgressVariable, envir = wpsProgressEnv)
	return(.p)
}

wpsIncreaseProgress <- function(increase = 1) {
	.p <- wpsGetProgress()
	.p <- .p + increase
	wpsSetProgress(.p)
	return(.p)
}

wpsResetProgress <- function() {
	wpsSetProgress(wpsProgressRange[1])
	return(wpsGetProgress())
}

wpsGetProgressPercentage <- function() {
	.p <- wpsGetProgress() / wpsProgressRange[2]
	return(.p)
}

###############################################################################
# testing

# wps.off;
is.environment(wpsProgressEnv)

wpsResetProgress()

wpsSetProgress(50)
wpsGetProgress()
wpsGetProgressPercentage()

wpsSetProgress(42)
wpsGetProgress()
wpsGetProgressPercentage()

wpsIncreaseProgress()
wpsIncreaseProgress()
wpsIncreaseProgress()
wpsIncreaseProgress()
wpsIncreaseProgress()
wpsIncreaseProgress()
wpsGetProgressPercentage()

wpsIncreaseProgress(17)
wpsIncreaseProgress(17)
wpsGetProgressPercentage()

wpsResetProgress()
# wps.on;