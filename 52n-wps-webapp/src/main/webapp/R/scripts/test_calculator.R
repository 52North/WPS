# wps.des: test_calculator, process for misusing R as a calculator;

# wps.in: a, integer, value=1;
# wps.in: b, integer, value=1;
# wps.in: op, string, value=+;

# wps.off;
op <- "+"
a <- 23
b <- 42
# wps.on;

result <- do.call(op, list(a, b))

#wps.out: result, double, calculation result;

# wps.off;
result
# wps.on;