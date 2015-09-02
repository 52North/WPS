# Matlab WPS

Extension for the [52°North WPS](https://github.com/52North/WPS) to offer [Matlab](http://www.mathworks.de/products/matlab/) functions and scripts as [OGC Web Processing Service](http://www.opengeospatial.org/standards/wps) processes.

## Dependencies

* Matlab (R2013b was tested)
* [matlab-connector](https://github.com/autermann/matlab-connector)

## Usage

Install Matlab and be sure it is in your `$PATH`. Start a socket server on the mashine running matlab:

```sh
java -jar matlab-connector-server-2.0-SNAPSHOT-jar-with-dependencies.jar -b /path/to/the/script/file
```


Create a [YAML](http://de.wikipedia.org/wiki/YAML) description of your function/script.

If you have a function like this:
```matlab
function result=add(a,b)
    result = a + b
```

Your configuration may look like this:

```yaml
---
title: add
function: add
abstract: adds two values
identifier: com.github.autermann.wps.matlab.add
inputs:
    - identifier: a
      title: first operand
      abstract: first operand
      type: double
    - identifier: b
      title: second operand
      abstract: second operand
      type: double
outputs:
    - identifier: result
      title: the result
      abstract: the result of the addition
      type: double
connection:
    host: localhost
    port: 7000
    attempts: 5
    timeout: 10000
...
```

The Matlab WPS configuration is currently hardcoded at `com.github.autermann.wps.Main`. A more convenient configuration is planned…

You can start the WPS with this command:
```
java -jar matlab-wps-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```



## License
```
Copyright (C) 2013 Christian Autermann

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
```