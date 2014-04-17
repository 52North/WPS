# wps.des: test.wpsOff, dummy process for testing wps.off annotations;

# wps.off;
a = 1
b = 2
c = 3
# wps.on;

# wps.in: id = a, type = integer, minOccurs = 1, maxOccurs = 1;
# wps.in: id = b, type = integer, minOccurs = 1, maxOccurs = 1;
# wps.in: id = c, type = integer, minOccurs = 1, maxOccurs = 1;

out <- a + b + c

#wps.off;
out <- 17
#wps.on;

#wps.out: id = out, type = integer, title = sum of inputs;