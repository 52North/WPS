## Function to import enviroCar trajectories
################################################################################
## Code modified from Edzer Pebesma and Nikolai Korte                         ##  
################################################################################

#
# import function for enviroCar data
#
importEnviroCar = function(file) {
  require(rjson) # fromJSON
  require(maptools) # spCbind
  require(rgdal) #readOGR
  require(RCurl) #getURL
  require(stringr) #str_replace_all
  
  # read data as spatial object:
  layer = readOGR(getURL(file,ssl.verifypeer = FALSE), layer = "OGRGeoJSON")
  
  # convert time from text to POSIXct:
  layer$time = as.POSIXct(layer$time, format="%Y-%m-%dT%H:%M:%SZ")
  # the third column is JSON, we want it in a table (data.frame) form:
  # 1. form a list of lists
  l1 = lapply(as.character(layer[[3]]), fromJSON)
  # 2. parse the $value elements in the sublist:
  l2 = lapply(l1,function(x) as.data.frame(lapply(x, function(X) X$value)))
  # dynamic parsing of phenomenon names and units
  phenomenonsUrl = "https://www.envirocar.org/api/stable/phenomenons"
  phenomenons = fromJSON(getURL(phenomenonsUrl,ssl.verifypeer = FALSE))
  
  colNames <- c("GPS.Bearing", "GPS.HDOP", "GPS.Speed")
  if (!all(colNames %in% names(l2[[1]]))) 
    stop("Trajectory does not contain all the necessary data (GPS.Bearing, GPS.HDOP, GPS.Speed)")
  else
    colNames <- names(l2[[1]])
  
  
  resultMatrix = matrix(nrow = length(l2),ncol = length(colNames))
  dimnames(resultMatrix)[[2]] = colNames
  for (i in seq(along = l2))
    resultMatrix[i,colNames] = as.numeric(l2[[i]])[match(colNames, names(l2[[i]]))]
  result = as.data.frame(resultMatrix)
  
  # set the units:
  units <- sapply(phenomenons[[1]], "[[", "unit")
  names(units)=colNames
  
  # add a units attribute to layer
  layer[[3]] = NULL
  # add the table as attributes to the spatial object
  if (length(layer) == nrow(result)) {
    layer = spCbind(layer, result)
    attr(layer, "units") = units
    layer
  } else
    NULL
}

myLog <- function(...) {
	cat(paste0("[enviroCar MM] ", ..., "\n"))
}


# process description on localhost:
# http://localhost:8080/wps/WebProcessingService?Request=DescribeProcess&service=WPS&version=1.0.0&identifier=org.n52.wps.server.r.enviroCar_osmMatching

################################################################################
# process inputs

# testdata defined inline
# wps.off;

# wps.des: id = enviroCar_osmMatching,
#		title = envirocar track to OSM streets matching,
# 	abstract = Match an enviroCar track to the OpenStreetMap network with fuzzy matching algorithm;

# wps.in: trackId, string, title = trackIdentifier,
# abstract = enviroCar track identifier,
# minOccurs = 1, maxOccurs = 1;
trackId <- "52f3836be4b0d8e8c27ed6f0"

# wps.in: envirocarApiEndpoint, string, title = envicoCar API,
# abstract = enviroCar API endpoint for GET and POST requests,
# value = https://envirocar.org/api/stable,
# minOccurs = 0, maxOccurs = 1;
envirocarApiEndpoint <- "https://envirocar.org/api/stable"

# wps.in: image_width, type = integer, title = width of the generated image in pixels,
# value = 800, minOccurs = 0, maxOccurs = 1;
# wps.in: image_height, type = integer, title = height of the generated image in pixels,
# value = 500, minOccurs = 0, maxOccurs = 1;
image_width = 800;
image_height = 500;

# wps.on;

myLog("inputs: ", toString(paste(ls())), "")

################################################################################
# process

myLog("working directory: ", getwd(), "\n")
## URL of the trajectory
trackUrl = paste0(envirocarApiEndpoint, "/tracks/", trackId)

myLog("Starting process for ", trackUrl, "\n")

## Import the trajectory
traj = importEnviroCar(trackUrl)

# install fuzzyMM package from source file beforehand!
require(fuzzyMM)

## Do the map matching
matched_traj <- mm(traj, plot = FALSE)

# wps.off;
str(matched_traj)
# wps.on;

myLog("DONE! environment objects: ", toString(paste(ls())), "\n")

################################################################################
# process outputs

# wps.out: matched_traj_data, type = rdata,
# title = the trajectories, 
# abstract = the matched and original trajectory as RData;
matched_traj_data <- paste0("matched_traj_", trackId, ".RData")
save(traj, matched_traj, file = matched_traj_data)
myLog("Saved matched track data: ", getwd(), "/", matched_traj_data)

## wps.out: matched_traj_shp, type = shp_x,
## title = matched trajectory as SHP, 
## abstract = the matched and trajectory as a zipped shapefile;
#matched_traj_shp <- paste0("matched_traj_shp_", trackId)
#writeOGR(matched_traj, getwd(), matched_traj_shp, driver="ESRI Shapefile")
#myLog("Saved matched track shapefile: ", getwd(), "/", matched_traj_shp)

# wps.out: orig_traj_json, type = text,
# title = original trajectory as GeoJSON, 
# abstract = the original trajectory in Javascript Object Notation (JSON);
orig_traj_json <- paste0("orig_traj_", trackId, ".json")
writeOGR(traj, orig_traj_json, "traj", driver='GeoJSON')
myLog("Saved original track GeoJSON: ", getwd(), "/", orig_traj_json)

# wps.out: matched_traj_json, type = text,
# title = matched trajectory as GeoJSON, 
# abstract = the matched and trajectory in Javascript Object Notation (JSON);
matched_traj_json <- paste0("matched_traj_", trackId, ".json")
writeOGR(matched_traj, matched_traj_json, "matched_traj", driver='GeoJSON')
myLog("Saved matched track GeoJSON: ", getwd(), "/", matched_traj_json)

# wps.out: output_image, type = image/png, title = The output plot, 
# abstract = On-the-fly generated plot showing the matched points and streets;
output_image <- "output.png"
png(file = output_image, width = image_width, height = image_height,
		units = "px")
p <- plot(traj$coords.x1, traj$coords.x2, pch = 16, col = "blue",
		 xlab = "longitude", ylab = "latitude")
title(main = paste0("Matched track for ", trackId), sub = trackUrl)
points(matched_traj$coords.x1, matched_traj$coords.x2,pch = 16, col = "red")
roads <- create_drn(bbox(traj))
lines(roads@sl)
graphics.off()

myLog("Created image: ", getwd(), output_image)
