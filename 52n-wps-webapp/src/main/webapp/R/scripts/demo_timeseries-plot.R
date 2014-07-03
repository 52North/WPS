# Copyright (C) 2011 by 52 North Initiative for Geospatial Open Source Software GmbH, Contact: info@52north.org
# This program is free software; you can redistribute and/or modify it under the terms of the GNU General Public License version 2 as published by the Free Software Foundation. This program is distributed WITHOUT ANY WARRANTY; even without the implied WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program (see gpl-2.0.txt). If not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit the Free Software Foundation web page, http://www.fsf.org.
# Author: Daniel Nuest (daniel.nuest@uni-muenster.de)
# Project: sos4R - visit the project web page, http://www.nordholmen.net/sos4r
library("sos4R")
library("xts")

myLog <- function(...) {
	cat(paste0("[timeseriesPlot] ", ..., "\n"))
}

myLog("Start script... ", Sys.time())

# wps.off;

# wps.des: id = demo.timeseriesPlot, title = Plot SOS Time Series,
# abstract = Accesses a SOS with sos4R and creates a plot with a fitted 
# regression line;

# wps.in: sos_url, string, title = SOS service URL,
# abstract = SOS URL endpoint,
# minOccurs = 1, maxOccurs = 1;
sos_url <- "http://sensorweb.demo.52north.org/PegelOnlineSOSv2.1/sos"

# wps.in: offering_id, type = string, title = identifier for the used offering,
# minOccurs = 1, maxOccurs = 1;
offering_id <- "WASSERSTAND_ROHDATEN"

# wps.in: offering_stationname, type = string, 
# title = string contained in identifier for the used offering,
# minOccurs = 1, maxOccurs = 1;
offering_stationname <- "Bake"

# wps.in: offering_hours, integer, temporal extent,
# the number of hours the plot spans to the past,
# value = 24, minOccurs = 0, maxOccurs = 1;
offering_hours <- 24

# wps.in: image_width, type = integer, 
# title = width of the generated image in pixels,
# value = 800, minOccurs = 0, maxOccurs = 1;
# wps.in: image_height, type = integer, 
# title = height of the generated image in pixels,
# value = 500, minOccurs = 0, maxOccurs = 1;
image_width = 800;
image_height = 500;

# wps.in: loess_span, type = double, title = local regression span parameter,
# value = 0.75,
# minOccurs = 0, maxOccurs = 1;
loess_span <- 1

# wps.on;

################################################################################
# SOS and time series analysis

converters <- SosDataFieldConvertingFunctions(
	"WASSERSTAND_ROHDATEN" = sosConvertDouble,
	"LUFTTEMPERATUR" = sosConvertDouble)

myLog("Creating SOS connection to ", sos_url)
# establish a connection to a SOS instance with default settings
sos <- SOS(url = sos_url, dataFieldConverters = converters)

# wps.off;
names(sosOfferings(sos))
# wps.on;

# set up request parameters
offering <- sosOfferings(sos)[[offering_id]]
myLog("Requesting for offering:\n", toString(offering))

offering_station_idxs <- grep(pattern = offering_stationname,
															sosProcedures(offering))
# select on station at random
stationFilter <- sosProcedures(offering)[
	offering_station_idxs[sample(1:length(offering_station_idxs), 1)]]
myLog("Requesting data for station ", stationFilter)

observedPropertyFilter <- sosObservedProperties(offering)[1]
myLog("Requesting data for observed property ", observedPropertyFilter)
timeFilter <- sosCreateEventTimeList(sosCreateTimePeriod(sos = sos,
		begin = (Sys.time() - 3600 * offering_hours), end = Sys.time()))
myLog("Requesting data for time ", toString(timeFilter))

# make the request
myLog("Send request...")
observation <- getObservation(sos = sos,# verbose = TRUE,
		#inspect = TRUE,	
		observedProperty = observedPropertyFilter,
		procedure = stationFilter,
		eventTime = timeFilter,
		offering = offering)
data <- sosResult(observation)
# summary(data)
# str(data)

myLog("Request finished!"); myLog(toString(str(data)))

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
									 span = loess_span)

# create plot ##################################################################
timeseries_plot <- "output.jpg"
jpeg(file = timeseries_plot, width = image_width, height = image_height,
		 units = "px", quality = 90, bg = "#f3f3f3")

.title <- paste0("Dynamic Time Series Plot for ", toString(stationFilter))
p <- plot(timeSeries, main = .title,
		sub = paste0(toString(unique(data[["feature"]])), "\n", sosUrl(sos), " @ ",
								 toString(Sys.time())),
		xlab = attr(data[[timeField]], "name"),
		ylab = paste0(attr(values, "name"), 
				" [", attr(values, "unit of measurement"), "]"),
		major.ticks = "days")
lines(data[[timeField]], regression$fitted, col = 'red', lwd = 3)

graphics.off()

myLog("Created image: ", timeseries_plot)
myLog("Working directory: ", getwd())

# wps.out: timeseries_plot, type = jpeg, title = time series plot, 
# abstract = the output image as a graphic in jpeg format;


# test plot ####################################################################
# wps.off;
plot(timeSeries, main = "Test plot",
		 sub = paste0(toString(unique(data[["feature"]])), "\n", sosUrl(sos)),
		 xlab = attr(data[[timeField]], "name"),
		 ylab = paste0(attr(values, "name"), 
		 							" [", attr(values, "unit of measurement"), "]"),
		 major.ticks = "days")
lines(data[[timeField]], regression$fitted, col = 'red', lwd = 3)

# wps.on;
