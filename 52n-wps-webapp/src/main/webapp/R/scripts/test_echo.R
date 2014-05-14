# wps.des: test.echo, title = dummy echo process, abstract = you get what you put in;

# wps.in: id = inputVariable, type = string, title = input variable, minOccurs = 1, maxOccurs = 1;

# wps.off;
inputVariable <- "The quick brown fox jumps over the lazy dog"
# wps.on;

# test that the renaming measures do not affect the script
quitter <- inputVariable

uuunlinkkk <- quitter
evaluator <- uuunlinkkk
qevalq <- evaluator
systemo <- qevalq
setwdsetwdsetwd <- systemo

outputVariable <- setwdsetwdsetwd

if(inputVariable == "Hallo Echo!")
	outputVariable <- "Hallo Otto!"

#wps.out: id = outputVariable, type = string, title = returning input variable;

# wps.off;
outputVariable