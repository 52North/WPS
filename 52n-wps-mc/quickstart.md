# Quickstart MC and WPS
This is a brief introduction to the MovingCode module.

## 1. Checkout the project from GIT, adjust configuration and build the WAR file
Attention: Currently, only the configuration in the dev branch is working
```
git clone https://github.com/52North/WPS.git
git checkout dev 
```

Edit ``.../WPS/52n-wps-webapp/src/main/resources/processors.json`` to match your system configuration. Most importantly, you want to set the temp folder at the end of the file, to e.g.:
```json
"tempWorkspace" : "/tmp" 
```
in a Linux environment or
```json
"tempWorkspace" : "C:\temp" 
```
on a Windows platform. Make sure these folders exist. If not, you should create them.

Now edit ``.../WPS/52n-wps-webapp/config/wps_config.xml`` and ``wps_config_geotools.xml`` to enable the moving code module in WPS (it is commented out by default):
```xml
<Repository name="MCProcessRepository" className="org.n52.wps.mc.MCProcessRepository" active="true">
  <Property name="LOCAL_REPOSITORY" active="true">/home/someuser/mc-packages</Property>
</Repository> 
```
Set the path of the ``LOCAL_REPOSITORY`` to a folder where you intend to put your packages. You must also create this folder if it does not yet exist.

Now compile your project with maven or export it with your IDE as a WAR file (e.g. in Eclipse).

## 2. Create a local folder for the code packages
e.g. ``/home/someuser/mc-packages`` on Linux or ``C:\MCPackages`` on Windows.

## 3. Deploy your WAR file (e.g. on a Tomcat application server)
Simply copy it to the webapps folder of your application server, e.g. ``/var/lib/tomcat8/webapps/`` and wait until is loaded. Alternatively, you may also restart your application server (e.g. Tomcat)

Note: You can of course change these configuration from step (1.) any time you want by editing the configuration files. To be sure restart the application server to let the changes take effect.

## 4. Deploy a sample package
Copy a sample package from [here](https://raw.githubusercontent.com/52North/movingcode/master/mc-runtime/src/test/resources/testpackages/py_copy.zip) to your local folder (e.g. ``/home/someuser/mc-packages``).
The WPS module will scan the folder every 20 seconds for updates, so just wait a few moments until your WPS instance is updated. You can easily observe this in your WPS logfile (``/var/log/tomcat8/52n-wps.log/52n-wps.log``):
```
2015-09-10 15:01:57,943 [Thread-4] INFO  org.n52.movingcode.runtime.coderepository.AbstractRepository: Scanning directory: /home/someuser/mc-packages
2015-09-10 15:01:57,966 [Thread-4] INFO  org.n52.wps.mc.MCProcessRepository: Moving Code repository content has changed. Capabilities update required.
2015-09-10 15:01:57,967 [Thread-4] INFO  org.n52.wps.mc.MCProcessRepository: Added MovingCode Repository: LOCAL_REPOSITORY - /home/someuser/mc-packages
2015-09-10 15:01:57,967 [Thread-4] INFO  org.n52.wps.mc.MCProcessRepository: The following repositories have been loaded:
[/home/someuser/mc-packages]

...

2015-09-10 15:03:37,961 [Timer-0] INFO  org.n52.movingcode.runtime.coderepository.AbstractRepository: Repository content has silently changed. Running update ...
2015-09-10 15:03:37,962 [Timer-0] INFO  org.n52.movingcode.runtime.coderepository.AbstractRepository: Scanning directory: /home/someuser/mc-packages
2015-09-10 15:03:38,153 [Timer-0] INFO  org.n52.wps.mc.MCProcessRepository: Moving Code repository content has changed. Capabilities update required.
2015-09-10 15:03:38,153 [Timer-0] INFO  org.n52.wps.server.WebProcessingService: org.n52.wps.server.WebProcessingService$1: Received Property Change Event: WPSCapabilitiesUpdate
```

## 5. Check capabilities
Type the GetCapabilities request in your browser: ``http://localhost:8080/52n-wps-webapp/WebProcessingService?Request=GetCapabilities&Service=WPS``

... or simply use curl in a linux shell:
``curl 'http://localhost:8080/52n-wps-webapp/WebProcessingService?Request=GetCapabilities&Service=WPS'``

Check the response of your WPS server and look for ``de.tu-dresden.geo.gis.algorithms.test.echo``. If it is there, you are done.
