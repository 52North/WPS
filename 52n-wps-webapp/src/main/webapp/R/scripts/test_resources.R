# wps.des: test.resources, "Resources-Tester", abstract="A test script to demonstrate how resources are handled by wps4r", author = "Matthias Hinz";
# wps.in: inputDummy, string, title = "Input-Dummy",
# abstract = unused input value,
# value = "Dummy input value";

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

subdirSize <- length(list.dirs(recursive = FALSE))
# wps.out: subdirSize, integer, "Dummy-Output",
# "The number of directories in the test directory (1)";

directoryResourceDir <- "dir"
recursiveSubdirSize <- length(list.dirs(path = directoryResourceDir))
# wps.out: recursiveSubdirSize, integer, "Dummy-Output",
# "The number of directories recursively counted in the test directory (3)";

if(!is.element("dummy2.txt", list.files(directoryResourceDir)))
	warn("File from directory in resources directory was not copied to wd subdir")

directoryResourceContentSize <- length(list.files(path = directoryResourceDir, 
				pattern = "dummy"))
# wps.out: directoryResourceContentSize, integer, "Dummy-Output",
# "The number of files in the test directory (1)";

subdirTextContent <- as.double(
		read.table(paste0(directoryResourceDir, "/dummy2.txt"))[1,1])
# wps.out: subdirTextContent, double, "Dummy-Output",
# "The number in the dummy file in the test directory (42)";

subdirSubfolderTextContent <- as.double(
		read.table(paste0(directoryResourceDir, "/folder/subfolder/dummy3.txt"))[1,1])
# wps.out: subdirSubfolderTextContent, integer, "Dummy-Output",
# "The number in the dummy file in the test directory (17)";

if(is.element("dummy2.txt", list.files(getwd())))
	warn("File from directory in resources directory was incorrectly copied to base wd")