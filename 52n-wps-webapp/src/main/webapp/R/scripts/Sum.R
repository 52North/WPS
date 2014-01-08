library(sp); library(maptools)
# wps.des: id=Sum, title = Sum of attributes for Polygons, abstract = Calculates the sum of a numeric
# attribute variable for given Polygon files
# given by execute request: variables with identifiers "data" and "attributename";
# wps.in: data, application/x-zipped-shp;
# wps.in: attributename, string;
input=readShapePoly(data)
sum = sum(input@data[attributename])

#other output functions may be: 
#mean = mean(input@data[attributename])
#median = median((input@data[attribute])[!is.na(input@data[attributname])])
#max = max(input@data[attributename])
#min = min(input@data[attributename])
#quList = quantile(input@data[attributename], probs = seq(0, 1, 0.25), na.rm=T)

#output variable - shall be always named "result":
result = sum
# wps.out: result, double;