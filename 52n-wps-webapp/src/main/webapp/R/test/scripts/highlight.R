#
# Author: Matthias Hinz
###############################################################################

# wps.des: highlight, "Transforms an R script into HTML/CSS with syntax highlights";
# wps.in: input, type=text, abstract = "R script to highlight";
library(highlight)

output = "output.html"
highlight(file = input, output = output, detective = simple_detective, 
		renderer = renderer_html( document = TRUE ) ,parser.output = parser(input, encoding = "UTF-8"))

# wps.out: output, type= text, abstract = "highlighted html text"; 