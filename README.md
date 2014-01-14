# 52°North Web Processing Service [![Build Status](https://travis-ci.org/52North/WPS.png?branch=master)](https://travis-ci.org/52North/WPS)

The 52°North Web Processing Service (WPS) enables the deployment of geo-processes on the web in
a standardized way. It features a pluggable architecture for processes and data encodings.
The implementation is based on the current OpenGIS specification: 05-007r7.

Its focus was the creation of an extensible framework to provide algorithms for generalization on the web.

More information available at the [52°North Geoprocessing Community](http://52north.org/geoprocessing).

## Features

* Java-based Open Source implementation.
* Supports all features and operations of WPS specification version 1.0.0 (document 05-007r7)
* Pluggable framework for algorithms and XML data handling and processing frameworks
* Build up on robust libraries (JTS, geotools, XMLBeans, servlet API, derby)
* Experimental transactional profile (WPS-T)
* Web GUI to maintain the service

## Supported Backends

The 52°North WPS provides wrappers to well-established (geographical) computation backends.

* WPS4R - R Backend
* GRASS out of the box extension
* 220+ SEXTANTE Processes
* ArcGIS Server Connector
* Moving Code backend, including Python support

## Development

Use git to clone the WPS repository:

`git clone https://github.com/52North/WPS.git`

Then just run `mvn clean install` on the repositories root directory.

## Integration Testing

The WPS comes with a variety of integration tests which are performed using Jetty.
In order to execute integration tests in a maven build, activate the dedicated profile
through `mvn clean install -Pintegration-test`.

