###############################################################################
#wps.des: id = test.rdata, title = Test script for rData download, 
# abstract = returns a ;

#wps.in: id = filename, title = file name for the output, abstract = dummy
# variable because we need input - will be prepended to the generated files,
# type = string, value = test_rdata, minOccurs = 0, maxOccurs = 1;

# wps.off;
filename <- "test_rdata_"
setwd(tempdir())
# wps.on;

myLog <- function(...) {
	cat(paste0("[test.rdata] ", Sys.time(), " > ", ..., "\n"))
}
myLog("Start script... wd: ", getwd())

#wps.out: id = output.file, type = rdata, title = 52 random values;
output <- runif(52)
output.file <- paste0(filename, ".RData")
save(output, file = output.file)
myLog("Done, save rdata to file ", data, " in ", getwd())


#wps.out: spatial.rdata, rdata+Spatial, Spatial grid data from meuse;
library(sp)
data(meuse)
spatial.rdata <- paste0(filename, ".grid.RData")
save(meuse.grid, file = spatial.rdata)

