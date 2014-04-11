# wps.des: demo.image, title = demo image process generating a plot of the Meuse dataset;

library("sp")
data(meuse)
coordinates(meuse) <- ~x+y

# wps.in: parameter, string, data variable, 
# abstract = the data variable to plot: one of {copper / lead / zinc / elev},
# value = zinc;

#wps.off;
parameter <- "zinc"
setwd(tempdir())
#wps.on;

image <- "output.png"
png(file = image)
spplot(meuse, parameter, main = paste0("Meuse dataset, variable: ", parameter), sub = toString(Sys.time()))
graphics.off()
cat("Saved image ", image, " in ", getwd())

# wps.out: image, png;
