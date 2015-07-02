# WPS4R Webapp directory

WPS4R heavily relies on scripts and configuration files that are stored in the webapp directory to be accessible and changeable in deployed WPS.
The sub-folders and their contents are as follows:

* ``/demo``: Browser demonstration clients (HTML, Javascript and image files).
* ``/R_Datatype.conf``: Configuration of data types to be used in R script annotations.
* ``/resources``: Resource files for the scripts. If a script defines names of resource files from this directory then these files are loaded to the R workspace before script execution.
* ``/scripts``: The WPS4R script repository, see seperate README.md file.
* ``/utils``: R scripts that are loaded into every R session. These scripts may contain configuration, session wide utility functions, or server-wide variables.
* ``wps4r-script-dev-utils.R``: A script file with functions to be used while developing scripts for WPS4R.