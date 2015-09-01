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

zipShp = function(file){
    base = unlist(strsplit(file,"\\."))[1]
    shp = paste(base,"shp", sep=".")
    shx = paste(base,"shx", sep=".")
    dbf = paste(base,"dbf", sep=".")
    prj = paste(base,"prj", sep=".")
    
    zip = paste(base,"zip", sep=".")
    zip(zip, c(shp,shx,dbf,prj))
    
    if(zip %in% dir())
        return(zip)
    else return(NULL)
}