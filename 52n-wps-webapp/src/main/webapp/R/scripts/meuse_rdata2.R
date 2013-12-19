#wps.des: id=meuse_rdata2, title=Script that returns meuse data as rdata-files, abstract=The meuse river data set is contained in the sp-package of R - see package information;
#wps.in: input, integer, dummy input, value=0;

require(sp)
loadMeuse(river=TRUE)
summary(meuse)
summary(meuse.grid)
summary(meuse.riv)


#wps.out: workspace, rdata;
workspace="workspace.RData"
save.image(workspace)




