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

## Integration Testing

The WPS comes with a variety of integration tests which are performed using Jetty.
In order to execute integration tests in a maven build, activate the dedicated profile
through `mvn clean install -Pwith-geotools,integration-test`.

