#input / output variables equally named to describeprocess document
#input should be initialized before running this script

library("sp")
library("gstat")
library("rgdal")
library("intamap")

###############################################################################
# create a test input dataset based on the meuse dataset
#wps.off;
data("meuse")
coordinates(meuse) <- ~	x+y
proj4string(meuse) <- CRS("+init=epsg:28992")

data("meuse.grid")
coordinates(meuse.grid) <- ~x+y
proj4string(meuse.grid) <- CRS("+init=epsg:28992")
gridded(meuse.grid) <- TRUE

setwd("d:/"); getwd()
# http://spatial-analyst.net/book/system/files/GstatIntro.pdf
writeOGR(meuse, ".", "meuse", "ESRI Shapefile")
#wps.on;

###############################################################################
# log function
myLog <- function(...) {
	cat(paste0("[demo.idw] ", Sys.time(), " > ", ..., "\n"))
}

myLog("Start script... ")

###############################################################################
#wps.des: id = demo.idw, title = Inverse Distance Interpolation in R, 
# abstract = Calculates Inverse Distance Interpolation for 
# given point values on a specified grid;

#wps.in: points, type = application/x-zipped-shp, title = measurement points,
# abstract = Points for IDW, minOccurs = 0, maxOccurs=1;

#wps.in: maxdist, type = double, value = Inf, title = maximum distance
# abstract = Only observations within a distance of maxdist 
# from the prediction location are used for prediction;

#wps.in: nmax, type = integer, value = Inf, title = number of observations
# abstract = Maximum number of nearest observations that should be used for prediction;

#wps.in: attributename, string;

#wps.off;
attributename <- "zinc"
ogrInfo("meuse.shp", layer = "meuse")
#points <- readOGR("meuse.shp", layer = "meuse")
points <- "meuse.shp"
nmax <- 23
maxdist <- Inf
#wps.on;

layername <- sub(".shp","", points) # just use the file name as the layer name
inputPoints <- readOGR(points, layer = layername)
summary(inputPoints)

f <- formula(paste(attributename, "~ 1"))
myLog("Using this formula: ", toString(f))

gridpoints = SpatialPoints(makegrid(inputPoints),
													 proj4string = CRS(proj4string(inputPoints)))
grid = SpatialPixels(gridpoints)
myLog("Interpolation output grid:")
summary(grid)

idw <- idw(formula = f, locations = inputPoints, newdata = grid,
									maxdist = maxdist, nmax = nmax)
summary(idw)

idwImage <- writeGDAL(idw, fn = "output.tiff")
#wps.out: idwImage, type = geotiff, title = the interpolated raster,
# abstract = interpolation output as rasterfile in GeoTIFF format;
