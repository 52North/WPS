library("jsonlite")
library("sp")
library("intamap")
library("lattice")

################################################################################
# test input dataset
#wps.off;
data <- "test_data.json"
testData <- '{

    "values": [
{
"coords": [
7.2044419705086735,
51.266086785330224
],
"lastValue": {
"timestamp": 1371064950000,
"value": 20.3
}
},
{
"coords": [
7.365665095102475,
51.14334954184367
],
"lastValue": {
"timestamp": 1371064050000,
"value": 19.6
}
},
{
"coords": [
7.100552165082708,
51.087732979584395
],
"lastValue": {
"timestamp": 1370921850000,
"value": 10
}
},
{
"coords": [
7.320281676860971,
51.22548556899834
],
"lastValue": {
"timestamp": 1372980150000,
"value": 16.1
}
},
{
"coords": [
7.40096795458942,
51.17103846368366
],
"lastValue": {
"timestamp": 1372720950000,
"value": 13.1
}
},
{
"coords": [
7.0602992201626185,
51.0966135017852
],
"lastValue": {
"timestamp": 1372979250000,
"value": 16.7
}
},
{
"coords": [
7.107294690952992,
51.225894610660866
],
"lastValue": {
"timestamp": 1371065850000,
"value": 21.4
}
},
{
"coords": [
7.299968716891246,
51.200402890202945
],
"lastValue": {
"timestamp": 1372994550000,
"value": 15.8
}
},
{
"coords": [
7.186580962308821,
51.06958203108305
],
"lastValue": {
"timestamp": 1372909500000,
"value": 13.9
}
},
{
"coords": [
7.530828579490191,
51.09836102312226
],
"lastValue": {
"timestamp": 1379323350000,
"value": 9.9
}
},
{
"coords": [
7.179744487634198,
51.06765508608393
],
"lastValue": {
"timestamp": 1372923450000,
"value": 16.1
}
},
{
"coords": [
7.399410121794452,
51.17167137941307
],
"lastValue": {
"timestamp": 1379324250000,
"value": 11.5
}
},
{
"coords": [
7.283090752705482,
51.090121367969026
],
"lastValue": {
"timestamp": 1372995450000,
"value": 15.3
}
},
{
"coords": [
7.184148868629406,
51.218726535948775
],
"lastValue": {
"timestamp": 1379326950000,
"value": 10
}
},
{
"coords": [
7.226317896922862,
51.19657532705112
],
"lastValue": {
"timestamp": 1372908600000,
"value": 14.1
}
},
{
"coords": [
7.430406594086035,
51.13625558025584
],
"lastValue": {
"timestamp": 1372981950000,
"value": 15.5
}
},
{
"coords": [
7.305941659811078,
51.063698821453556
],
"lastValue": {
"timestamp": 1379327400000,
"value": 11.4
}
},
{
"coords": [
7.239976260754382,
51.075121231895494
],
"lastValue": {
"timestamp": 1379247750000,
"value": 14.3
}
},
{
"coords": [
7.557398313117449,
51.08136930185558
],
"lastValue": {
"timestamp": 1379320650000,
"value": 9.7
}
},
{
"coords": [
7.557398313111111,
51.08136930188888
],
"lastValue": {
"timestamp": 1379320650000,
"value": 9.42
}
}
],
"phenomenon": "3",
"bounds": {
"_southWest": {
"lat": 50.77033932897995,
"lng": 6.87744140625
},
"_northEast": {
"lat": 51.55572834577049,
"lng": 7.738494873046875
}
},
"pixelBounds": {
"min": {
"x": 136080,
"y": 87113
},
"max": {
"x": 136707,
"y": 88025
}
}
}'
write(x = testData, file = data)
#wps.on;

################################################################################
# log function
myLog <- function(...) {
	cat(paste0("[demo.jsclient] ", Sys.time(), " | ", ..., "\n"))
}
myLog("Start script... ")

################################################################################
#wps.des: id = demo.interpolation.jsclient, title = Interpolation, 
# abstract = Interpolation of environmental observatoins from Javascript client;

################################################################################
# input

#wps.in: data, type = application/json, title = measurement points and metadata,
# abstract = Locations and values for interpolation as well as the name of the 
# observed property and the bounding box, minOccurs = 0, maxOccurs=1;

#wps.in: type, type = string, title = plot type,
# abstract = set whether 'mean' or 'variance' of the interpolation is plotted,
# value = mean, minOccurs = 0, maxOccurs=1;
#wps.off;
type <- "mean"
#wps.on;

plotSwitch <- 1
if(type == "variance")
	plotSwitch <- 2
myLog("Plotting ", type, " - so switch is set to ", plotSwitch)

#wps.in: cellNumber, type = integer, title = number of prediction cells,
# abstract = the number of grid cells used for the output grid,
# value = 20000, minOccurs = 0, maxOccurs=1;
#wps.off;
cellNumber <- 12000
#wps.on;

##wps.in: observedProperty, type = string, title = observed property name, 
## abstract = the name of the observed property;

##wps.in: bounds, type = json, title = bounding box for the interpolation,
## abstract = the corner coordinates of the bounding box to be used for 
## interpolation's prediction locations;


# read the json and store the data in R data structures
inputData <- fromJSON(data)
myLog("Input data: \n ", toString(inputData))

phenomenon <- inputData$phenomenon
myLog("Phenomenon: ", phenomenon)

# save the bounds
southWest <- list("lat" = inputData$bounds$lat[["_southWest"]],
									"lon" = inputData$bounds$lng[["_southWest"]])
northEast <- list("lat" = inputData$bounds$lat[["_northEast"]],
									"lon" = inputData$bounds$lng[["_northEast"]])

# image size
width <- as.numeric(inputData$pixelBounds[["y"]]["max"]) - as.numeric(inputData$pixelBounds[["y"]]["min"])
height <- as.numeric(inputData$pixelBounds[["x"]]["max"]) - as.numeric(inputData$pixelBounds[["x"]]["min"])
myLog("width: ", width, ", height = ", height)

# values
str(inputData$values)
names(inputData$values)

lat <- sapply(inputData$values["coords"][[1]], "[[", 1)
lon <- sapply(inputData$values["coords"][[1]], "[[", 2)
time <- as.POSIXct(inputData$value$lastValue$timestamp/1000, origin="1970-01-01")
value <- inputData$value$lastValue$value

pointDataFrame <- data.frame(lat, lon, time, value)
inCRS <- CRS("+proj=utm +zone=33 +datum=WGS84")

pointData <- SpatialPointsDataFrame(
	coords = pointDataFrame[,c("lat", "lon")],
	data = pointDataFrame[,c("time", "value")],
	proj4string = inCRS)
myLog("Got spatial data points with bbox ", toString(bbox(pointData)))
summary(pointData)

#wps.off; TESTPLOT
library("mapdata"); library(maptools)
germany_p <- pruneMap(map(database = "worldHires", region = "Germany",
													plot = FALSE))
germany_sp <- map2SpatialLines(germany_p, proj4string = inCRS)
proj4string(germany_sp) <- inCRS
plot(x = germany_sp, col = "grey")
plot(pointData, pch = 20, col = "blue", add = TRUE)
title("Testplot")
#wps.on;

################################################################################
# interpolation

# create sampling grid - TODO make sampling grid based on provided bounds
x <- c(southWest$lon, northEast$lon)
y <- c(southWest$lat, northEast$lat)
xy <- cbind(x,y)
grdBounds <- SpatialPoints(xy)
myLog("Creating grid for interpolation within bounds ", toString(bbox(grdBounds)))

grdpoints = SpatialPoints(makegrid(x = grdBounds, n = cellNumber),
													proj4string = inCRS)
grd = SpatialPixels(grdpoints)
myLog("Interpolation output grid:")
summary(grd)

#?interpolate
interpolatedData <- interpolate(observations = pointData, predictionLocations = grd)
myLog("Finished with interpolation: ", interpolatedData$processDescription)

#wps.off; INSPECT INTERPOLATION
plotIntamap(interpolatedData)
plot(interpolatedData$observations)
plot(interpolatedData$predictions)
plot(interpolatedData$predictions$var1.pred)

interpolationOut <- interpolatedData$predictions$var1.pred
str(interpolationOut)
#wps.on;

# project to UTM for interpolation
# if(proj4string(inputPoints) != proj4string(raster)) {
# 	myLog("projection of points and raster differ!\n", 
# 				proj4string(points), "\n", proj4string(raster))
# 	inputPoints <- spTransform(points, CRS(proj4string(raster)))
# }

################################################################################
# output

method <- interpolatedData$processDescription
# wps.out: method, type = string, title = process description,
# abstract = a textual description of the used interpolation method;

image <- "interpolated.png"
png(filename = image, width = width, height = height, units = "px")

trellis.par.set(axis.line = list(col=NA)) 
# plot without any borders
cut.val <- 0 # was -5 ### Just to force it.
theme.novpadding <-
	list(layout.heights =
			 	list(top.padding = cut.val,
			 			 main.key.padding = cut.val,
			 			 key.axis.padding = cut.val,
			 			 axis.xlab.padding = cut.val,
			 			 xlab.key.padding = cut.val,
			 			 key.sub.padding = cut.val,
			 			 bottom.padding = cut.val),
			 layout.widths =
			 	list(left.padding = cut.val,
			 			 key.ylab.padding = cut.val,
			 			 ylab.axis.padding = cut.val,
			 			 axis.key.padding = cut.val,
			 			 right.padding = cut.val))

spplot(interpolatedData$predictions[plotSwitch], col.regions = bpy.colors(),
			 colorkey = FALSE, border = NA, ann = FALSE, axes = FALSE,
			 par.settings = theme.novpadding)

graphics.off()
myLog("Saved image ", image, " in ", getwd())
# wps.out: image, type = png, title = the interpolated data,
# abstract = interpolation output in png format;

imageBounds <- "imageBounds.json"
# try to look like http://leafletjs.com/reference.html#imageoverlay
jsonData <- list(c(southWest$lat, southWest$lon), c(northEast$lat, northEast$lon))
myLog("Image bounds: ", toString(jsonData), " | bbox of interpolation data: ",
			toString(bbox(interpolatedData$predictions)))
#json <- serializeJSON(bbox(points), pretty = TRUE, digits = 8)
json <- toJSON(jsonData, pretty = TRUE, digits = 8)
wrappedJson <- paste0("{ ", json, "}")
write(x = json, file = imageBounds)
myLog("Saved bounds in file ", imageBounds, " to ", getwd())
# wps.out: imageBounds, type = json, title = image bounds,
# abstract = the bounds of the image encoded as json;
