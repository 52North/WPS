# 52°North WPS [![OpenHUB](https://www.openhub.net/p/n52-wps/widgets/project_thin_badge.gif)](https://www.openhub.net/p/n52-wps)



## Description
Standardized web-based geo-processing.
The 52°North Web Processing Service enables the deployment of geo-processes on the web in a standardized way. It features a pluggable architecture for processes and data encodings.

The 52°North WPS implements the [OGC WPS specification](http://www.opengeospatial.org/standards/wps).

## Build Status
* Master: [![Master Build Status](https://travis-ci.org/52North/WPS.png?branch=master)](https://travis-ci.org/52North/WPS)
* Develop: [![Develop Build Status](https://travis-ci.org/52North/WPS.png?branch=dev)](https://travis-ci.org/52North/WPS)

## Features
* General Features
  * Full java-based Open Source implementation.
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

## License

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

## Changelog

  * Changes since last release
    * New features
      * Improved parsing of GML 3.2
  
    * Changes
  
    * Fixed issues
      * #228 ExecutionContext.getTempDirectoryPath() misses to put an "/" after the java.io.tmpdir property
      * #235 Use connect URL in describeProcess
      * #236 Replace describeProcess identifier

## Quick Start

Get the latest Version here [52°North WPS 3.6.1](http://52north.org/downloads/send/15-wps/504-52n-wps-webapp-3-6-1), [additional GeoTools Package](http://52north.org/downloads/send/15-wps/505-wps-3-6-1-geotools-package)
* deploy the war-file of the client in your favorite web container (e.g. tomcat)
* Access the WPS admin console via: http://yourhost:yourport/yourwebapp-path/webAdmin/index.jsp
    * Default credentials: wps, wps (to change this, edit the users.xml located in WPS_WEBAPP_ROOT/WEB-INF/classes)


## User guide/tutorial

See here : [Geoprocessing Tutorials](https://wiki.52north.org/bin/view/Geoprocessing/GeoprocessingTutorials)

## Demo

Try out the latest WPS release with GeoTools package (GRASS 7 and SEXTANTE processes enabled) or get a sneak preview of the 4.0 version on our [Geoprocessing Demo Server](http://geoprocessing.demo.52north.org/).

## References

* [GLUES WPS](http://wps1.glues.geo.tu-dresden.de/wps/WebProcessingService?request=GetCapabilities&service=WPS) - WPS service deployed for the GLUES project.
* [USGS WPS](http://cida.usgs.gov/gdp/process/WebProcessingService?Service=WPS&Request=GetCapabilities) - WPS service deployed by the Center for Integrated Data Analysis of the United States Geological Survey.

## Contact

Benjamin Pross (b.pross (at) 52north.org)

## Support

You can get support in the community mailing list and forums:
http://52north.org/resources/mailing-lists-and-forums/

## Contribute

Are you are interesting in contributing to the 52°North WPS and you want to pull your changes to the 52°North repository to make it available to all?
In that case we need your official permission and for this purpose we have a so called contributors license agreement (CLA) in place. With this agreement you grant us the rights to use and publish your code under an open source license.
A link to the contributors license agreement and further explanations are available here:
http://52north.org/about/licensing/cla-guideline

## Credits

 * Benjamin Pross, @bpross-52n
 * Daniel Nüst, @nuest
 * Matthias Müller, @matthias-mueller
 * Theodor Foerster, @theodorfoerster
 * Matthes Rieke, @matthesrieke
 * Christian Autermann, @autermann
 * Tom Kunicki, @tkunicki
 * Bastian Baranski, @bbaranski
 * Ivan Suftin, @isuftin
 * Matthias Hinz, @MatthiasHinz
 * Jordan Walker, @jiwalker-usgs
 * Phil Russom, @prusso-sse
 * Steffen Reichel, @grmpfhmbl
 * Hadrien Tulipe, @htulipe
 * German Carillo, @gacarrillor

## Contributing Organizations

### Funding organizations

 * U.S. Geological Survey
 * Faculty of Geo-Information Science and Earth Observation (ITC), University of Twente
 * Chair of Geoinformatics, TU Dresden
 * Institute for Geoinformatics, University of Münster

### Funding projects

 * Google Summer of Code
 * GLUES
 * TaMIS
 * OGC Testbeds
