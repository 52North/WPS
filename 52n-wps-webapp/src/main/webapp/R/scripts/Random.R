# wps.des: id = R_andom, title = Random number generator, 
# abstract = Generates random numbers for uniform distribution; 
# wps.in: min, double, Minimum, All outcomes are larger than min, value = 0; 
# wps.in: max, double, Maximum, All outcomes are smaller than max, value = 1;
# wps.in: n, integer, ammount of random numbers, value = 100;  
# random number: 
x = runif(n, min=min, max=max)

# wps.out: output, text, Random number list, Textfile containing n random numbers in one column;
output = "random_out"
write.table(x, output)