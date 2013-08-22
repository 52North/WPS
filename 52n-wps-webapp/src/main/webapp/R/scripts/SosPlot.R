# Copyright (C) 2011 by 52 North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This program is free software; you can redistribute and/or modify it under the terms of the GNU General Public License version 2 as published by the Free Software Foundation. This program is distributed WITHOUT ANY WARRANTY; even without the implied WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program (see gpl-2.0.txt). If not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit the Free Software Foundation web page, http://www.fsf.org.
# Author: Daniel Nuest (daniel.nuest@uni-muenster.de)
# Project: sos4R - visit the project web page, http://www.nordholmen.net/sos4r
library("sos4R")
library("xts")

myLog <- function(...) {
	cat(paste0("[SosPlot] ", ..., "\n"))
}

myLog("Start script... ", Sys.time())

# wps.off;

# wps.des: id = PlotSOS, title = Plot SOS Time Series,
# abstract = Accesses a SOS with sos4R and creates a plot with a fitted
# regression line;

# wps.in: sos_url, string, title = SOS service URL,
# abstract = SOS URL endpoint,
# value = http://fluggs.wupperverband.de/sos/sos,
# minOccurs = 0, maxOccurs = 1;
sos_url <- "http://fluggs.wupperverband.de/sos/sos"

# wps.in: offering_id, type = string, title = identifier for the used offering,
# value = Luft;
offering_id <- "Luft"

## wps.in: offering_id, type = string, title = identifier for the used offering,
## value = Luft;
##offering_property <- "Lufttemperatur"

# wps.in: offering_days, integer, temporal extent,
# the number of days the plot spans to the past,
# value = 10,
# minOccurs = 0, maxOccurs = 1;
offering_days <- 7

# wps.in: offering_station, type = integer, title = identifier for the used offering,
# value = 38,
# minOccurs = 0, maxOccurs = 1;
offering_station <- c(42,46) #round(runif(n = 1, min = 30, max = 40))

# wps.in: image_width, type = integer, title = width of the generated image in pixels,
# value = 800, minOccurs = 0, maxOccurs = 1;
# wps.in: image_height, type = integer, title = height of the generated image in pixels,
# value = 500, minOccurs = 0, maxOccurs = 1;
image_width = 800;
image_height = 500;

# wps.on;

################################################################################
# SOS and time series analysis

converters <- SosDataFieldConvertingFunctions("Lufttemperatur" = sosConvertDouble,
																							"Luftfeuchte" = sosConvertDouble)

# establish a connection to a SOS instance with default settings
sos <- SOS(url = sos_url, dataFieldConverters = converters)

# set up request parameters
offering <- sosOfferings(sos)[[offering_id]]
myLog(toString(offering))

stationFilter <- sosProcedures(offering)[offering_station]
observedPropertyFilter <- sosObservedProperties(offering)[1]
timeFilter <- sosCreateEventTimeList(sosCreateTimePeriod(sos = sos,
		begin = (Sys.time() - 3600 * 24 * offering_days), end = Sys.time()))

# make the request
observation <- getObservation(sos = sos,# verbose = TRUE,
		#inspect = TRUE,	
		observedProperty = observedPropertyFilter,
		procedure = stationFilter,
		eventTime = timeFilter,
		offering = offering)
data <- sosResult(observation)
# summary(data)
# str(data)

myLog(toString(str(data)))

# create time series ###########################################################
timeField <- "SamplingTime"

valuesIndex <- 3
values <- data[[names(data)[[valuesIndex]]]]

# create time series from data and plot
timeSeries <- xts(x = values, order.by = data[[timeField]])

# calculate regression (polynomial fitting)
regressionValues <- data[[names(data)[[valuesIndex]]]]
regressionTime <- as.numeric(data[[timeField]])
regression = loess(regressionValues~regressionTime, na.omit(data),
		enp.target=10)

# create plot ##################################################################
## wps.out: output_image, type = image/jpeg, title = The output image, 
## abstract = On-the-fly generated plot for the requested time series;
output_image <- "output.jpg"
jpeg(file = output_image,
		width = image_width, height = image_height, units = "px")
p <- plot(timeSeries, main = "Dynamic Time Series Plot",
		sub = paste0(toString(unique(data[["feature"]])), "\n", sosUrl(sos)),
		xlab = attr(data[[timeField]], "name"),
		ylab = paste0(attr(values, "name"), 
				" [", attr(values, "unit of measurement"), "]"),
		major.ticks = "days")
lines(data[[timeField]], regression$fitted, col = 'red', lwd = 3)
graphics.off()

myLog("Created image: ", output_image)
myLog("Working directory: ", getwd())

# wps.out: output_image, type = jpeg, title = image plot, 
# abstract = the output image in jpeg format;
