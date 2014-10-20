# wps.des: id = import, title = Test script for import annotation; 

# wps.in: inputstuff, integer, value = 42;

# wps.import: imported.R, dir/alsoImported.R;

# wps.out: outputstuff, integer;
outputstuff <- myImportedFunction(inputstuff)
outputstuff <- myOtherImportedFunction(outputstuff)
