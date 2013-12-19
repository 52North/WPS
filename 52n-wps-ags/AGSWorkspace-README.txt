In order to compile the AGSWorkspace classes, just run a "mvn install" on the subfolder
"52n-wps-ags-workspace". It is not included as a module as it would break WPS builds and
projects (e.g. eclipse workspaces). You could also import it as a Maven project into your
eclipse workspace.

For that project, you need that the arcobjects.jar is located in the folder

52n-wps-ags-workspace/lib-repository/com/esri/arcobjects/10.0

It must be named "arcobjects-10.0.jar" in order to allow maven to resolve it within 
the project build.


If you do NOT want to compile the AGSWorkspace classes, i.e. you will not use the
project 52n-wps-ags, and you get validation errors in Eclipse you can simply remove the
project 52n-wps-ags-workspace from your Eclipse workspace and refresh all Maven
projects.