# wps.des: test.warnings, "Warnings-Tester", abstract="A test script to demonstrate how warnings are derived from R", author = "Matthias Hinz";
# wps.in: inputDummy, string, "Input-Dummy", value="Dummy input value";
warning("Test warning 1 ...")
warning("Test warning 2: This is a warning with some more text.")
warning("This process is only for testing purposes and contains no valid output.")
warning("Test warning 4: This is the LAST warning.")

dummyOutput = paste0("Dummy output value ", inputDummy)
# wps.out: dummyOutput, string, "Dummy-Output";