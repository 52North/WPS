# TODO: Add comment
# 
# Author: Matthias Hinz
###############################################################################

#wps.des: title = test image process;

data(meuse)
coordinates(meuse) <- ~x+y
image = "output.png"
png(file=image)
#plot(meuse, "zinc")
plot(c(1,1),c(2,1))
graphics.off()
# wps.out: image, png;
