# wps.des: highlight, "Transforms an R script into HTML/CSS with syntax highlights using the highlight package";

# wps.in: rcodeurl, type = string, title = code location,
# abstract = "URL to the R code to highlight"
# minOccurs = 1, maxOccurs = 1;

library(highlight)

myLog <- function(...) {
	cat(paste0("[highlight] ", Sys.time(), " > ", ..., "\n"))
}

myLog("Start script... ")

tmpfile <- "rcode.txt"

download.file(url = rcodeurl, destfile = tempfile)

# wps.off;
rcode <- 'highlight(code = rcode, format = "html",
							 output = output, detective = simple_detective,
							 renderer = renderer_html( document = TRUE ),
							 parser.output = parser(input, encoding = "UTF-8"))'
write(rcode, file = tmpfile)
# wps.on;

myLog("Saved code to file ", tmpfile, " in ", getwd())

html <- "rcode.html"
h <- highlight(file = tmpfile, output = html, format = "html",
							 detective = simple_detective,
							 renderer = renderer_html( document = TRUE ),
							 parser.output = parser(input, encoding = "UTF-8"))

# wps.out: html, type = text,
# abstract = "highlighted html code"; 

myLog("Saved to file ", hcode, " in ", getwd())
