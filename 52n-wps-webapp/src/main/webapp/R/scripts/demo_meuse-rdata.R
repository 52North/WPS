# wps.des: demo.meuse.rdata, title = Script that returns meuse data as rdata-files,
# abstract=The meuse river data set is contained in the sp-package of R - see package information;

# wps.in: filename, string, the base name of the generated files, value = meuse;

# wps.off;
filename <- "meuse"
setwd(tempdir())
cat("wd: ", getwd(), "\n")
# wps.on;

library(sp)
data(meuse)
data(meuse.grid)
data(meuse.riv)

summary(meuse)
summary(meuse.grid)
summary(meuse.riv)

#wps.out: meuse.grid.rdata, rdata+Spatial, Spatial grid data from meuse;
meuse.grid.rdata <- paste0(filename, ".grid.RData")
save(meuse.grid, file=meuse.grid.rdata)

#wps.out: meuse.riv.rdata, rdata+SpatialPolygons;
meuse.riv.rdata <- paste0(filename, ".riv.RData")
save(meuse.riv.rdata, file=meuse.riv.rdata)

#wps.out: meuse.rdata, rdata+SpatialPoints, The meuse data samples;
meuse.rdata <- paste0(filename, "meuse.RData")
save(meuse, file=meuse.rdata)

#wps.out: workspace, rdata;
workspace="workspace.RData"
save.image(workspace)

# you can also directly execute this request and load the whole workspace in R via a GET request:
#con <- url("http://localhost:8080/wps/WebProcessingService?Request=Execute&Service=WPS&version=1.0.0&identifier=org.n52.wps.server.r.demo.meuse.rdata&DataInputs=filename%3D42&RawDataOutput=workspace")
#load(con)
