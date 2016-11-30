# Setup
##Pre-requisites
* _JDK 7 is required to run Service COLD cache.(IBM JDK isn't supported)_

## Steps
1. Clone the repo and run ```mvn clean install```.
2. Unzip the ```target/dist.zip``` to a SSD driver for better performance.
3. Add JVM arguments to your application's launch configuration or command (32876 is the default port Service COLD cache used):
   >```
   -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=32876
   ```

   - Adding JVM args in Eclipse:
   ![Eclipse JVM args](eclipes_jvmargs.png?raw=true)

   - Adding JVM args in IntelliJ:
   ![IntelliJ JVM args](intellij_jvmargs.png?raw=true)

   - Adding JVM args in Standalone Tomcat

      Developer needs to set these arguments in environment variable ```CATALINA_OPTS``` before launch tomcat server:
         * Windows: ```set CATALINA_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=32876```
         * OS X&Linux: ```CATALINA_OPTS="-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=32876"```

4. Start Service COLD cache by running the startup script.
   * Windows: ```startup.bat```
   * OS X&Linux: ```./startup.sh```(you need to add **Execute** privilege to _*.sh_ files, e.g. ```chmod 744 *.sh```")
5. Start up your application as usual.
6. To disable cache after application finished startup/warmup, use the following command:
   * ```serviceCache.bat disableCache``` 
   * ```./serviceCache.sh disableCache```
subsequent requests will get responses from destination directly instead of cached responses in Service COLD cache. 
 
**_For more details on usage of Service COLD cache please visit [Cookbook](cookbook.md)_**


