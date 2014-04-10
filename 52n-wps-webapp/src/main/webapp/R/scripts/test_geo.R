###############################################################################
#wps.des: id = test.geo, title = Test script for geospatial data output, 
# abstract = returns the Meuse test dataset and returns it as shapefile and 
# GeoTIFF;

#wps.in: id = filename, title = file name for the output, abstract = dummy
# variable because we need input - will be prepended to the generated files,
# type = string, value = test_geo, minOccurs = 0, maxOccurs = 1;

# wps.off;
filename <- "test_geo"
setwd("D:/TEMP")
# wps.on;

myLog <- function(...) {
	cat(paste0("[test.geo] ", Sys.time(), " > ", ..., "\n"))
}
myLog("Start script... wd: ", getwd())

library("sp")
library("rgdal")

# load data
data("meuse")
coordinates(meuse) <- ~	x+y

###############################################################################
# shapefile output
# http://spatial-analyst.net/book/system/files/GstatIntro.pdf
writeOGR(meuse, ".", "meuse", "ESRI Shapefile")
meuse_vector <- "meuse.shp"
#wps.out: id = meuse_vector, type = application/x-zipped-shp, title = shapefile 
# of the meuse dataset;

myLog("Wrote shapefile meuse.shp")

###############################################################################
# raster output
data(meuse.grid)
coordinates(meuse.grid) <- ~x+y
proj4string(meuse.grid) <- CRS("+init=epsg:28992")
gridded(meuse.grid) <- TRUE
#spplot(meuse.grid)

raster_filename <- paste0(filename, "_raster.tif")
meuse_raster <- writeGDAL(meuse.grid["dist"], fn = raster_filename, drivername = "GTiff")
#meuse_raster <- paste(getwd(), raster, sep="/")
#wps.out: id = meuse_raster, type = geotiff, title = gridded meuse dataset,
# abstract = gridded meuse dataset (variable 'dist') in GeoTIFF format;

myLog("Wrote raster ", raster_filename)

meuse_summary <- "meuse_summary.txt"
capture.output(summary(meuse), file = meuse_summary)
#wps.out: id = meuse_summary, type = text, title = statistical summary of the
# dataset;
