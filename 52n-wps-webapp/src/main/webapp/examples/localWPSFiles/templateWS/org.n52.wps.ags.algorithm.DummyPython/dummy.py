# dummy algorithm, just copies an input file

import shutil, sys 

inFile = sys.argv[1]
outFile = sys.argv[2]

shutil.copy (inFile, outFile)

