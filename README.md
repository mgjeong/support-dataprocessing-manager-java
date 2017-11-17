Data Processing Runtime
================================

Data Processing Runtime, as name suggests, is a data processing Runtime for IoT.
 
This Runtime is currently under active development.

## Developing Runtime ##
### Prerequisites ###
- Java 8
```shell
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```
- Maven
```shell
cd /opt/
wget http://www-eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
sudo tar -xvzf apache-maven-3.3.9-bin.tar.gz
sudo mv apache-maven-3.3.9 maven
```
- IDE
  - IntelliJ Community Edition (recommended)
- Web Server
  - We recommend you use IntelliJ Community Edition with Jetty Runner plugin installed.
- [Apache Flink 1.3](https://flink.apache.org)
  - Chcke the "Build"


Remember, you must configure proxies for git and maven accordingly if necessary.

- Setting up proxy for git
```shell
git config --global http.proxy http://proxyuser:proxypwd@proxyserver.com:8080
```
- [Setting up proxy for maven](https://maven.apache.org/guides/mini/guide-proxies.html)


### Build and Run Apache Flink ###
Please visit [Apache Flink webiste](https://flink.apache.org) for detailed instructions on how to build and run Apache Flink.

Snippet below should give you a grasp of how to build Apache Flink from source.
```
wget http://archive.apache.org/dist/flink/flink-1.3.0/flink-1.3.0-src.tgz
tar -xvf ./flink-1.3.0-src.tgz ./
cd flink-1.3.0-src
mvn clean package -DskipTests
cd build-target
./bin/start-cluster.sh # Execute Apache Flink
```
For more information, please visit [Apache Flink website.](https://flink.apache.org)


### Build source ###

```shell
mvn clean package -DskipTests 
```

### Setting up IntelliJ Community Edition ###

- Importing project as Maven project
  - Select `Import Project` and select project directory 
  - From `Import project from external model`, select `Maven`
  - Check `Import Maven projects automatically`
  - If IntelliJ fails to download and import maven dependencies, please do it manually from console.
    (To use terminal within IntelliJ, go to View -> Tool Windows -> Terminal)

### Running Runtime ###

- Create a shared resource directory for runtime and Apache Flink
  - Create `/runtime/ha` directory in system
  - If necessary, change directory ownership to user
  `chown -R user:user /runtime`

- Update the path information of the Flink in the "tools/path.prefs"
  -flink_bin="{FLINK_PATH}"
     
- Execute Runtime with Flink
  - Shell script below will build the codebase and execute the runtime & Flink automatically
  - Terminate the Flink if it's already executed
 ```shell
cd tool
./start_local.sh
```

### Check Runtime ###

- Now you should be able to make RESTful requests to http://localhost:8082/analytics
- Swagger UI interface is available at: http://localhost:8082/analytics/swagger-ui.html


## Modules ##

Data Processing Runtime is composed of four submodules.
1. Runtime
   - RESTful APIs
   - Abstraction layer for open-source data processing engines
2. Engine-Flink
   - Flink-app. which will execute run-time task(s) 
3. Runtime-common
   - runtime-task or Custom task (jar) loader
   - Processing job/task managing modules
4. Runtime-task
   - Data pre-processing task
   - Data analytic task

The Data Processing Runtime is divided into submodules to minimize dependency relations.
 -Dependencies
  - `Runtime`,`Engine-Flink` ---- dependent on ---> `Runtime-common` module.
  - `Runtime-common`         ---- dependent on ---> `Runtime-task/task-model` module.


