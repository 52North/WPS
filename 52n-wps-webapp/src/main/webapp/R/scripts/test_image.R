# wps.des: test.image, title = demo image process generating a plot of the Meuse dataset;

# wps.in: size, integer, title = image size, 
# abstract = the horizontal and vertical size of the image in pixels,
# value = 500;

#wps.off;
size <- 420
setwd(tempdir())
getwd()
#wps.on;

image <- "output.png"
png(file = image, width = size, height = size)
x <- c(1,2,3,4)
y <- c(1,7,4,2)
plot(x, y, main = "WPS4R test plot", sub = toString(Sys.time()))

graphics.off()
cat("Saved image ", image, " in ", getwd())

# wps.out: id = image, type = png, title = a simple plot;
