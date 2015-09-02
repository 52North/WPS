# Quickstart MC and WPS
This is a brief introduction to the MovingCode module.

## 1. Checkout the project from GIT and build the WAR file
NB: Currently, only the configuration in the dev branch is working

## 2. Deploy your WAR file (e.g. with Tomcat)

## 3. Create a local folder for the code packages
e.g. ``C:\MCPackages`` on Windows or ``/home/someuser/my-mc-packages`` on Linux

## 4. Enable Module in WPS configuration
Enable the MovingCode module in your ``wps_config.xml`` by searching for ``MCProcessRepository`` and enable this as follows:
```xml
<Repository name="MCProcessRepository" className="org.n52.wps.mc.MCProcessRepository" active="true">
  <Property name="LOCAL_REPOSITORY" active="true">C:\MCPackages</Property>
</Repository>
```

Note: If you compiled WPS with geotools support, you should update ``wps_config_geotools.xml`` accordingly.

## 5. Restart WPS
Restart your application server (e.g. Tomcat)

## 6. Deploy sample package
Copy a sample package from [from this repo](https://raw.githubusercontent.com/matthias-mueller/movingcode/master/mc-runtime/src/test/resources/testpackages/jar_copy.zip) to your local folder (e.g. ``C:\MCPackages``).
The WPS module will scan the folder every 20 seconds for updates, so just wait a few moments until your WPS is updated.

## 7. Check capabilities
Check the capabilities of your WPS server and look for ``de.tu-dresden.geo.gis.algorithms.test.echo``.
