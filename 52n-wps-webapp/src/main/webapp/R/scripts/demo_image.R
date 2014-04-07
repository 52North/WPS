# wps.des: demo.image, title = demo image process generating a plot of the Meuse dataset;

library("sp")
data(meuse)
coordinates(meuse) <- ~x+y

# wps.in: parameter, string, data variable, 
# abstract = the data variable to plot, one of {copper, lead, zinc, elev},
# value = zinc;

png(file = "output.png")
spplot(meuse, parameter)
graphics.off()

# wps.out: image, png;
