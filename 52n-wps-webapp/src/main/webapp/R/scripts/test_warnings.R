# wps.des: test_warnings, "Warnings-Tester", abstract="A test script to demonstrate how warnings are derived from R", author = "Matthias Hinz";
# wps.in: inputDummy, string, "Input-Dummy", value="Dummy input value";
warning("Test warning 1", immediate. = TRUE)
warning("Test warning 2")
warning("Test warning 3")
warning("Test warning 4: This is a warning with longer and comlex ")
warning("This process is only for testing purposes and contains no valid output")

dummyOutput = "Dummy output value"
# wps.out: dummyOutput, string, "Dummy-Output";