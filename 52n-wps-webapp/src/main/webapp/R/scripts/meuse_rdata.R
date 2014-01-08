#wps.des: meuse_rdata, title=Script that returns meuse data as rdata-files, abstract=The meuse river data set is contained in the sp-package of R - see package information;
#wps.in: input, integer, dummy input, value=0;

require(sp)
loadMeuse(river=TRUE)
summary(meuse)
summary(meuse.grid)
summary(meuse.riv)

#wps.out: meuse.grid.rdata, rdata+Spatial, Spatial grid data from meuse;
meuse.grid.rdata = "meuse.grid.RData"
save(meuse.grid, file=meuse.grid.rdata)

#wps.out: meuse.riv.rdata, rdata+SpatialPolygons;
meuse.riv.rdata="meuse.riv.RData"
save(meuse.riv.rdata, file=meuse.riv.rdata)

#wps.out: meuse.rdata, rdata+SpatialPoints, The meuse data samples;
meuse.rdata="meuse.RData"
save(meuse, file=meuse.rdata)

#wps.out: workspace, rdata;
workspace="workspace.RData"
save.image(workspace)

#wps.out: meuse.grid.rdata, rdata+Spatial, Spatial grid data from meuse;
meuse.grid.rdata = "meuse.grid.RData"
save(meuse.grid, file=meuse.grid.rdata)

#wps.out: meuse.riv.rdata, rdata+SpatialPolygons;
meuse.riv.rdata="meuse.riv.RData"
save(meuse.riv.rdata, file=meuse.riv.rdata)

#wps.out: meuse.rdata, rdata+SpatialPoints, The meuse data samples;
meuse.rdata="meuse.RData"
save(meuse, file=meuse.rdata)

#wps.out: workspace, rdata;
workspace="workspace.RData"
save.image(workspace)




