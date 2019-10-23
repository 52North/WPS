# 52°North Web Processing Service [![Build Status](https://travis-ci.org/52North/WPS.png?branch=master)](https://travis-ci.org/52North/WPS)[![Gitter](https://badges.gitter.im/52North/WPS.svg)](https://gitter.im/52North/WPS?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

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

```
git clone https://github.com/52North/WPS.git
```

Then just run `mvn clean install` on the repositories root directory.

### GeoTools

Due to licensing issues all [GeoTools](http://www.geotools.org/) based input/output handlers and algorithms are not included by default. If you want to use them (or any backend relying on them), you have to explicitly enable them with the `with-geotools` profile:

```
$ mvn clean install -P with-geotools
```

To run your project in Eclipse with geotools support using the WTP plug-in (52n-wps-webapp -> Run As -> Run on Server) add the profile to the Active Maven profiles in the project properties of 52n-wps-webapp (right click on the project, select "Maven", add `with-geotools` to the text field). 

### Non-default configuration file
There are several ways to supply a `wps_config.xml` file:

#### Configure at build time

##### With a path:

The supplied path will be written to the `web.xml` and will be used at runtime. For this to work, the path should be absolute.

```
$ mvn install -Dwps.config.file=/path/to/external/file/that/will/be/used
```
##### With a file:

The supplied file will be copied to the WAR file and will be used at runtime.

```
$ mvn install -Dinclude.wps.config.file=/path/to/external/file/that/will/be/copied
```

#### Configure at runtime
##### With a system property

The supplied value will override every other configuration.

```
$ java [...] -Dwps.config.file=/path/to/external/file/that/will/be/used
```

This works well with a server configuration in Eclipse WTP. Open the server editor, click "Open launch configuration" and add the property to the VM arguments. 

##### Using JNDI:

The supplied value will override every other configuration except a possible system property. See the [Apache Tomcat documentation](https://tomcat.apache.org/tomcat-7.0-doc/config/context.html#Environment Entries):

```xml
<Context ...>
  ...
  <Environment name="wps.config.file"  value="/path/to/file"
               type="java.lang.String" override="false"/>
  ...
</Context>
```

##### Using the servlet config

You can edit the `web.xml` after creation and substitute another path:
```xml
<servlet>
    <servlet-name>WPS</servlet-name>
    <servlet-class>org.n52.wps.server.WebProcessingService</servlet-class>
    <init-param>
        <param-name>wps.config.file</param-name>
        <param-value>/path/to/file</param-value>
    </init-param>
</servlet>
  ```
  
##### Using the user home directory

Create a file named `wps_config.xml` in the home directory of the user that executes the servlet container.

## Integration Testing

The WPS comes with a variety of integration tests which are performed using Jetty.
In order to execute integration tests in a maven build, activate the dedicated profile
through `mvn clean install -Pwith-geotools,integration-test`.

## Contributing

You can find information about how to contribute to this project in the [Geoprocessing Wiki](https://wiki.52north.org/bin/view/Geoprocessing/StructuresAndProcedures#Git_Procedures).

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
