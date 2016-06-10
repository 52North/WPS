# 52°North WPS [![OpenHUB](https://www.openhub.net/p/n52-wps/widgets/project_thin_badge.gif)](https://www.openhub.net/p/n52-wps)

## Build Status
* Master: [![Master Build Status](https://travis-ci.org/52North/WPS.png?branch=master)](https://travis-ci.org/52North/WPS)
* Develop: [![Develop Build Status](https://travis-ci.org/52North/WPS.png?branch=dev)](https://travis-ci.org/52North/WPS)

# Description
The 52°North Web Processing Service enables the deployment of geo-processes on the web in a standardized way. It features a pluggable architecture for processes and data encodings.

Current features
* General Features and Compliance
  * Full java-based Open Source implementation.
  * Implements OGC WPS specification version 1.0.0 (document 05-007r7)
  * Pluggable framework for algorithms and XML data handling and processing frameworks
  * Build up on robust libraries (JTS, geotools, xmlBeans, servlet API, derby)
  * Supports full logging of service activity
    * Supports exception handling according to the spec
    * Storing of execution results
    * Full maven support
* Clients
  * Basic client implementation for accessing the WPS (including the complete XML encoding)
* WPS Invocation
  * Synchronous/Asynchronous invocation
  * Raw data support
  * Supports HTTP-GET
  * Supports HTTP-POST
* Supported WPS Datatypes (selection)
  * GeoTiff Support
  * ArcGrid Support
  * Full GML2 support for ComplexData
  * Full GML3 support for ComplexData
  * Shapefiles
  * KML
  * WKT
  * (Geo-)JSON
* Extensions
  * WPS4R - R Backend
  * GRASS out of the box extension
  * 220+ SEXTANTE Processes
  * Web GUI to maintain the service
  * ArcGIS Server Connector
* Result Storage
  * All Results can be stored as simple web accessible resource with an URL
  * Raster/Vector results can be stored directly as WMS layer
  * Vector results can be stored directly as WFS layer
  * Raster results can be stored directly as WCS layer

# Getting Started and configuration

* Get the latest Version here [52°North WPS 3.4.0](http://52north.org/downloads/send/15-wps/489-52n-wps-webapp-3-4-0), [additional GeoTools Package](http://52north.org/downloads/send/15-wps/488-wps-3-4-0-geotools-package)
    * deploy the war-file of the client in your favorite web container (e.g. tomcat)
    * Access the WPS admin console via: http://yourhost:yourport/yourwebapp-path/webAdmin/index.jsp
      * Default credentials: wps, wps (to change this, edit the users.xml located in WPS_WEBAPP_ROOT/WEB-INF/classes)

# License

This project consists of modules which are published under different licenses.

* **API**: The internal API is published under The Apache Software License, Version 2.0. If you want to build your own algorithms you can depend on these modules alone and release it under any compatible open source license. The API consists of the following modules:
  * 52n-wps-algorithm
  * 52n-wps-commons
  * 52n-wps-io
* **Web service**: The implementation of the internal API allows to publish algorithms online as a web service. The web service implementation is published under the GNU General Public License Version 2. The following modules make up the web service implementation:
  * 52n-wps-ags
  * 52n-wps-algorithm-geotools (with exceptions to EPL libraries)
  * 52n-wps-algorithm-impl
  * 52n-wps-client-lib
  * 52n-wps-database
  * 52n-wps-grass
  * 52n-wps-io-geotools (with exceptions to EPL libraries)
  * 52n-wps-io-impl
  * 52n-wps-mc
  * 52n-wps-python
  * 52n-wps-r
  * 52n-wps-server
  * 52n-wps-server-soap
  * 52n-wps-sextante
  * 52n-wps-transactional
  * 52n-wps-webadmin
  * 52n-wps-webapp
  
For details see the LICENSE and NOTICE files. Be aware that some modules contain their own LICENSE and NOTICE files.

# User guide/tutorial

See here : [Geoprocessing Tutorials](https://wiki.52north.org/bin/view/Geoprocessing/GeoprocessingTutorials)

# Demo

* [Geoprocessing Demo Server](http://geoprocessing.demo.52north.org/)

# Changelog

  * Changes since last release
    * New features
      * Add mechanism to update the status of a WPS4R process 
      * Add a self-cleaning file input stream implementation for Postgres database
      * Raise an exception if an annotated Algorithm has no @Execute annotation
      * Empty port and webapp path allowed for WPS URL
  
    * Changes
      * Use moving code packages version 1.1  
      * Removed outdated python module.
      * Switch to Rserve from maven central
      * GRASS backend works with current GRASS 7 release
  
    * Fixed issues
      * Issue #123: Admin console not working when using Tomcat 6
      * Issue #173: Databinding issue with WPS4R
      * Issue #222: Save configuration with active R backend results in duplicate algorithm entries

# References

* [GLUES WPS](http://wps1.glues.geo.tu-dresden.de/wps/WebProcessingService?request=GetCapabilities&service=WPS) - WPS service deployed for the GLUES project.
* [USGS WPS](http://cida.usgs.gov/gdp/process/WebProcessingService?Service=WPS&Request=GetCapabilities) - WPS service deployed by the Center for Integrated Data Analysis of the United States Geological Survey.

# Contact

Benjamin Pross

b.pross (at) 52north.org

# Credits

 * USGS
 * ITC
 * Institute for Geoinformatics
 * TU Dresden
 * GSoC
 * GLUES
 * TaMIS
 * OGC Testbeds
