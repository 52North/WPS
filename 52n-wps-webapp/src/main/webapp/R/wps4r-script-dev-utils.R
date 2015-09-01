################################################################################
# some helper functions for developing scripts for WPS4R
#
# http://52north.org/wps4r
# Author: Daniel Nüst <d.nuest@52north.org>
################################################################################

log <- function(...) {
    cat(paste0("[wps4r.utils] ", Sys.time(), " | ", ..., "\n"))
}

################################################################################
# server-side validation of annotations

.testScript <- '# wps.des: test.validation, title = A minimal process, abstract = Example Calculation with R;
    # wps.in: input, integer;
	# wps.out: output, double;
	output = runif(1)*input'

validateScriptfileOnServer <- function(serverEndpoint, scriptfile, encoding = "UTF-8", debug = FALSE) {
    # load file
    if(file.exists(scriptfile)) {
        if(debug) log("validating file ", scriptfile, " at ", serverEndpoint)
        .text <- readLines(scriptfile, encoding = encoding)
        .text <- paste(.text, collapse = "\n")
        .validationResult <- validateScriptOnServer(serverEndpoint = serverEndpoint, script = .text,
                                                    encoding = encoding, debug = debug)
        return(.validationResult)
    }
    return("FILE DOES NOT EXIST")
}

validateScriptOnServer <- function(serverEndpoint, script, encoding = "UTF-8", debug = FALSE) {
    require(httr)
    require(stringr)
    
    .payloadPattern <- "INPUT_SCRIPT"
    
    .templateForValidationProcess <- paste0('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">
    	<ows:Identifier>org.n52.wps.server.algorithm.r.AnnotationValidation</ows:Identifier>
    	<wps:DataInputs>
    		<wps:Input>
    			<ows:Identifier>script</ows:Identifier>
    			<wps:Data>
    				<wps:ComplexData><![CDATA[', .payloadPattern, ']]>
    				</wps:ComplexData>
    			</wps:Data>
    		</wps:Input>
    	</wps:DataInputs>
    	<wps:ResponseForm>
    		<wps:ResponseDocument storeExecuteResponse="false">
    			<wps:Output asReference="false">
    				<ows:Identifier>validationResultString</ows:Identifier>
    			</wps:Output>
    			<wps:Output asReference="false">
    				<ows:Identifier>validationResultBool</ows:Identifier>
    			</wps:Output>
    			<wps:Output asReference="false">
    				<ows:Identifier>annotations</ows:Identifier>
    			</wps:Output>
    		</wps:ResponseDocument>
    	</wps:ResponseForm>
    </wps:Execute>')
    
    # create request
    .request <- str_replace(.templateForValidationProcess, .payloadPattern, script)
    if(debug) log("WPS request vor validation:\n", .request)
    
    # send it
    .response <- POST(url = serverEndpoint, content_type_xml(), body = .request)
    if(debug) log("WPS response:\n", .response)
    .responseString <- toString(.response)
    
    # print validation output
    .validationResult <- str_sub(.responseString,
                                 str_locate(.responseString, "<wps:ProcessOutputs>")[, "start"],
                                 str_locate(.responseString, "</wps:ProcessOutputs>")[, "end"])
    .validationResult <- str_trim(.validationResult)
    
    if(debug) log("retrieved validation result:\n", .validationResult)
    
    return(toString(.validationResult))
}

# testing
#validateScriptOnServer("http://localhost:8080/52n-wps-webapp/WebProcessingService", .testScript, debug = TRUE)
cat(validateScriptfileOnServer(serverEndpoint = "http://localhost:8080/wps/WebProcessingService", scriptfile = "C:/Users/Daniel/dev/git/glues-wps/WPS4R/system-archetypes-original/full-process.R")) #, debug = TRUE))
cat(validateScriptfileOnServer(serverEndpoint = "http://localhost:8080/wps/WebProcessingService", scriptfile = "C:/Users/Daniel/dev/git/glues-wps/WPS4R/system-archetypes-original/output-test.R")) #, debug = TRUE))
