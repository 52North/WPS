# Function to unzip input files and rename them in R
# Files which are not zipped will be just renamed
# 
# Author: Matthias Hinz
###############################################################################

unzipRename = function(file, name, ext){
	t=unzip(file)
	baseFileName = paste(name,ext,sep=".")
	
	if(length(t)==0){
		file.rename(file, baseFileName)
		return(baseFileName)
	}
	
	for(i in t){
		suffix = gsub("./","",i)
		suffix = unlist(strsplit(i,"\\."))
		if(length(suffix)>1){
			suffix = suffix[length(suffix)]
			suffix = paste(".",suffix,sep="")
		}else suffix = ""
		newName = paste(name,suffix,sep="")
		file.rename(i, newName)
	}
	
	return(baseFileName)
}
