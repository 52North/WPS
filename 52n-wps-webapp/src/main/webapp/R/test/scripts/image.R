# Test process for image output, 
# 
# 
# Author: Matthias Hinz
###############################################################################

#wps.des: highlight, title = test image process;
#wps.in: dummy, integer, just a dummy, abstract = Process contains a dummy-variable because process descriptions require at least one input to be valid, value = 0;

# FIXME: Meuse spplot does not work somehow (empty plot returned)...
#data(meuse)
#coordinates(meuse) <- ~x+y
#spplot(meuse, "zinc")
image = "output.png"
png(file=image)

plot(c(1,1),c(2,1))
graphics.off()
# wps.out: image, png;
