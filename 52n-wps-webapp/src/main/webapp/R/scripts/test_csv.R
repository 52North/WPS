###############################################################################
#wps.des: id = test.csv, title = Test script for csv output, 
# abstract = returns the data of the Meuse test dataset as csv;

#wps.in: id = filename, title = file name for the output, abstract = dummy
# variable because we need input - will be prepended to the generated files,
# type = string, value = test_csv, minOccurs = 0, maxOccurs = 1;

# wps.off;
filename <- "test_csv_"
setwd(tempdir())
# wps.on;

myLog <- function(...) {
	cat(paste0("[test.csv] ", Sys.time(), " > ", ..., "\n"))
}
myLog("Start script... wd: ", getwd())

library("sp")

# load data
data("meuse")
coordinates(meuse) <- ~	x+y

data <- paste0(filename, "meuse.csv")
write.csv(x = meuse@data, file = data)
#wps.out: id = data, type = text/csv, title = meuse data;

myLog("Done, save csv to file ", data, " in ", getwd())

