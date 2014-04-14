# wps.des: id = test.defaults, title = dummy process,
# abstract = test process for default value annotations;

# wps.in: id = a, type = integer, value = 4;
# wps.in: id = b, type = double, value = 2.5;
# wps.in: id = c, type = double, value = 32;

# wps.in: id = z, type = boolean, value = true;

# wps.in: id = y, type = string;

if(z == TRUE && is.na(y)) {
	out <- (a * b) + c
}

#wps.out: id = out, type = integer, title = sum of inputs;