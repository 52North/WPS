# wps.des: off, dummy process for testing wps.off annotations;
# wps.resource: pegel-report.Rnw, sweave-foo.Rnw;
# wps.off:
a = 2
b = 3
c = 4
# wps.on;


# wps.in: a, integer, value=2;
# wps.in: b, integer, value=3;
# wps.in: c, integer, value=1;

out = a+b+c

#wps.out: out, double, test result;