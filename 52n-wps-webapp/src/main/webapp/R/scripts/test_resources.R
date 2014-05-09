# wps.des: test.resources, "Resources-Tester", abstract="A test script to demonstrate how resources are handled by wps4r", author = "Matthias Hinz";
# wps.in: inputDummy, string, "Input-Dummy", value="Dummy input value";

#wps.resource: test/dummy1.txt, test/dummy2.png;
library(rgdal)

raster = readGDAL("dummy2.png")
textResourceContent = readLines("dummy1.txt", warn=F)
imageResourceWidth = bbox(raster)["x","max"]

warning("This process is only for testing purposes and contains no valid output")

# wps.out: textResourceContent, string, "Dummy-Output", "Content of the dummy-txt file";
# wps.out: imageResourceWidth, integer, "Dummy-Output", "Width of the test resource image in px (480)";

if(!is.element("dummy1.txt", list.files(getwd())))
	warn("File resources directory was not correctly copied")

###############################################################################
# directory as resource
#wps.resource: test/dir;

directoryResourceContentSize <- length(list.files(pattern = "dummy"))
# wps.out: directoryResourceContentSize, integer, "Dummy-Output",
# "The numer of files in the test directory (3)";

if(!is.element("dummy2.txt", list.files(getwd())))
	warn("File from directory in resources directory was not correctly copied")