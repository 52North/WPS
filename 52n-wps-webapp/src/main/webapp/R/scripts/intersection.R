# Author: Matthias Hinz
###############################################################################

# wps.des: intersection;
# wps.in: r1, shp_x, Polygon1;
# wps.in: r2, shp_x, Polygon2;
library(rgeos); library(maptools); library(rgdal);

poly1 = readShapePoly(r1)
poly2 = readShapePoly(r2)

polyint = gIntersection(poly1,poly2)

poly = as(polyint,"SpatialPolygonsDataFrame")

out="out.shp"
writeOGR(poly,out,"data","ESRI Shapefile")

# wps.out: out, shp_x, Intersection Polygon;