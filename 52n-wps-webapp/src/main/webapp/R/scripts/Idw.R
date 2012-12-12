	#input / output variables equally named to describeprocess document
	#input should be initialized before running this script
	
	options(digits=12)
	options(error = expression(NULL))
	options(warn = -1)
	#wps.des: title = Inverse Distance Interpolation in R, 
	#abstract = Calculates Inverse Distance Interpolation for 
	#given point values on a specified grid;
	
	#wps.in: points, type = application/x-zipped-shp, abstract = Points for IDW,
	#minOccurs = 0,maxOccurs=1;

	#wps.in: raster, type = application/img, abstract = Raster defining the output space;

	#wps.in: maxdist, type = double, value = Inf,
	#abstract = Only observations within a distance of maxdist 
	#from the prediction location are used for prediction;

	#wps.in: nmax, type = integer, value = Inf,
	#abstract = Maximum number of nearest observations that should be used for prediction;

	#wps.in: attributename, string;

	points=readOGR(points,sub(".shp","",points))
	
	raster=readGDAL(raster)
	
	if(proj4string(points)!=proj4string(raster))
		points=spTransform(points,CRS(proj4string(raster)))
		
	#runs interpolation and builds data frame:
	form=formula(paste(attributename,"~ 1"))
	idw=gstat::idw(form,points,raster,maxdist=maxdist,nmax=nmax)
	idw@data=data.frame(idw@data$var1.pred)
	
	#writing result file and define absolute filepath as output
	output=writeGDAL(idw,"output.img", drivername="HFA")
	
	
	result=paste(getwd(),output,sep="/")
	#wps.out: result, type = application/img, Abstract = Raster defining the output space;


