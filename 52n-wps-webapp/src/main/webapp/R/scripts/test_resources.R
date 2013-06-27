# wps.des: test_resources, "Resources-Tester", abstract="A test script to demonstrate how resources are handled by wps4r", author = "Matthias Hinz";
# wps.in: inputDummy, string, "Input-Dummy", value="Dummy input value";
#wps.resource: test/dummy1.txt, test/dummy2.png;
library(rgdal)

raster = readGDAL("dummy2.png")
dummyOutput1 = readLines("dummy1.txt")
dummyOutput2 = bbox(raster)["x","max"]

warning("This process is only for testing purposes and contains no valid output")

# wps.out: dummyOutput1, string, "Dummy-Output","Content of the dummy-txt file";
# wps.out: dummyOutput2, integer, "Dummy-Output","With of the dummy-png in px (480)";