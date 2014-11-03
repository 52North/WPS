###############################################################################
#wps.des: id = test.csv, title = Test script for csv output, 
# abstract = returns the data of the Meuse test dataset as csv;

#wps.in: id = filename, title = file name for the output, abstract = dummy
# variable because we need input - will be prepended to the generated files,
# type = string, value = test_csv, minOccurs = 0, maxOccurs = 1;

#wps.in: id = inputDataComplex, title = CSV test data, abstract = CSV data which 
# will be returned in a randomized order,
# type = text, minOccurs = 0, maxOccurs = 1;

#wps.in: id = inputDataLiteral, title = CSV test data, abstract = CSV data which 
# will be returned in a randomized order,
# type = string, minOccurs = 0, maxOccurs = 1;

# wps.off;
filename <- "test_csv_"
setwd(tempdir())
inputDataComplex <- "Year,Make,Model,Length
1997,Ford,E350,2.34
2000,Mercury,Cougar,2.38"
# wps.on;

myLog <- function(...) {
	cat(paste0("[test.csv] ", Sys.time(), " > ", ..., "\n"))
}
myLog("Start script... wd: ", getwd())

outputData <- NULL
dataFilename <- NULL
directInput <- function(input) {
    myLog("randomizing given input data: '", input, "'")
    
    # used to read the csv from variable rather than file
    require(data.table)
    
    df <- fread(input)
    
    #randomize list so that something happens to the data
    df <- subset(df, select = sample(names(df)))
    df <- df[sample(nrow(df)), ]
    return(df)
}

if(exists("inputDataLiteral") && !is.null(inputDataLiteral) && !is.na(inputDataLiteral)) {
    myLog("literal input data")
    dataFilename <- paste0(filename, "rand.csv")
    outputData <- directInput(inputDataLiteral)
} else if(exists("inputDataComplex") && !is.null(inputDataComplex) && !is.na(inputDataComplex)) {
    myLog("complex input data")
    dataFilename <- paste0(filename, "rand.csv")
    outputData <- directInput(inputDataComplex)
} else {
    myLog("creating csv based on meuse dataset")
    
    require("sp")
    
    # load data
    data("meuse")
    coordinates(meuse) <- ~x+y
    
    dataFilename <- paste0(filename, "meuse.csv")
    outputData <- meuse@data
}

#wps.out: id = dataTable, type = text/csv, title = CSV output data;
dataTable <- dataFilename
myLog("outputting to file '", dataTable, "': ", toString(outputData))
write.csv(x = outputData, file = dataTable)

myLog("Done, save csv to file ", dataTable, " in ", getwd())

