# Cookbook

- Basic usage is in [Setup](setup-standalone.md)
- _*.sh_ files need to be added **Execute** privilege. e.g. ```chmod 744 *.sh```
- For following usage description, please replace suffix from _.sh_ to _.bat_ when in Windows

### Start/Stop Service COLD cache
1. Run cmd ```./startup.sh``` to start up a Service COLD cache instance.
 * Default no arguments, and the Service COLD cache could be launched normally.
 * ```-s```: specify alternative user configuration file instead of default.
2. Run cmd ```./stop.sh``` to stop a Service COLD cache instance.
 * Default no arguments, and the Service COLD cache could be stopped normally.

### Disable/Enable Cache Feature of Service COLD cache in Runtime
1. When Service COLD cache finished speeding up startup/warmup, developers who want to get all realtime service data instead of cached data in Service COLD cache to develop projects, they can run command ```./serviceCache.sh disableCache``` to do so.
2. Once developers want to re-enable using cache data in Service COLD cache, they can run command ```./serviceCache.sh enableCache``` to do so.

### Clean Up Cache in Service COLD cache
When cache data is out of date, or developers want to get latest service data as cache data, they can run command ```./serviceCache.sh cleanupCache``` to clean up all caches in Service COLD cache.

### Customzie Exclude Rules for Cache
Developers who want to speed up their application's startup/warmup from Service COLD cache's cache, but also want to get realtime service data from certain kinds of services, then they can edit the user config file at ```{SERVICE_COLD_CACHE}/config.json```, add exclusion config to exclude certain URI patterns for cache even cache feature is on. e.g.
```json
"excludes": [
  {"uri": "http://www.ebay.com/v1/*", "method":"post"}, 
  {"uri": "http://www.ebay.com/v2/*"}
]
```
Details about configuration **"excludes"** see [Configurations][config] reference.

[config]: configurations.md

### Enable Proxy for HTTPS Requests
Some applications' startup depends on a couple of HTTPS service calls, Service COLD cache can also be used to cache HTTPS calls.

#### For Java
1. Developers need to install a pseudo root certificate ```{SERVICE_COLD_CACHE}/littleproxy-mitm.pem``` into JDK's keystore file. ```littleproxy-mitm.pem``` will be auto generated after Service COLD cache first running(**_Note: The different JDK run Service COLD cache could generate different key files, which could cause problems. it's better using same JDK to run Service COLD cache & web server_**).
  1. In command line, go to path ```{JDK_HOME}/jre/lib/security```. 
    * Be aware that {JDK_HOME} probably is not environment variable {JAVA_HOME}, it's the JDK on which these apps running.
    * For OS X, the ```security``` folder is under ```{JDK_HOME}/Contents/Home/jre/lib/security```.
  2. Run command ```keytool -import -alias cacheproxyservercert -keystore cacerts -file {SERVICE_COLD_CACHE}/littleproxy-mitm.pem```, the default password of keystore is ```changeit```.
    * Be aware if developers install multiple Service COLD cache distributions, choose the generated pem file from the distribution will be used.
    * ```keytool``` is a tool command under ```{JDK_HOME}/bin```.
2. Then add following JVM arguments to your application's launch configuration or commmand: ```-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=32876```. The proxy port for HTTPS requests is same with HTTP's. 
