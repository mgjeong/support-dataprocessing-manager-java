Data Processing Framework
================================

Data Processing Framework, as name suggests, is a data processing framework for IoT.
 
This framework is currently under active development.

## Building Framework from Source ##
_TODO_

## Developing Framework ##
### Prerequisites ###
- Git
- Java 8
- Maven
- IDE
  - IntelliJ Community Edition (recommended)
- Web Server
  - We recommend you use IntelliJ Community Edition with Jetty Runner plugin installed.
- [Apache Flink 1.3](https://flink.apache.org)


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
git clone https://github.sec.samsung.net/RS7-EdgeComputing/DataProcessingFW.git
cd ./DataProcessingFW/development
mvn clean package -DskipTests 
```

### Setting up IntelliJ Community Edition ###

- Importing project as Maven project
  - Select `Import Project` and select project directory (i.e. `development`)
  - From `Import project from external model`, select `Maven`
  - Check `Import Maven projects automatically`
  - If IntelliJ fails to download and import maven dependencies, please do it manually from console.
    (To use terminal within IntelliJ, go to View -> Tool Windows -> Terminal)

- Setting up CheckStyle
  - Run the following from the git repository directory:
  ```shell
  ./tools/githooks/setup.sh
  ``` 
  - You can also install CheckStyle plugin for IntelliJ.
    - From IntelliJ, go to File -> Settings -> Plugins -> Browse repositories -> CheckStyle-IDEA and install.
    - Restart IntelliJ then go to File -> Settings -> Other Settings -> CheckStyle -> '+' on Configuration File tab -> Select `./tools/githooks/checkstyle/sun_checks_customized.xml` from the repository.

### Running Framework ###

- Create a shared resource directory for framework and Apache Flink
  - Create `/framework/ha` directory in system
  - If necessary, change directory ownership to user
  `chown -R user:user /framework`
  
- Start Web Server using Jar 
  - From IntelliJ, Go to `Run->Edit Configuration`
  - Select `+` and enter JAR Application as follow:
    - Path: _projectDir_/framework/target/framework-0.1.0-SNAPSHOT.jar
    - Runs on Port: 8082
  
- Start Apache Flink
  - If Apache Flink is not running, please build and run Apache Flink


Now you should be able to make RESTful requests to http://localhost:8082/analytics

Swagger UI interface is available at: http://localhost:8082/analytics/swagger-ui.html


## Modules ##

Data Processing Framework is composed of four submodules.
1. Framework
  - RESTful APIs
  - Abstraction layer for open-source data processing engines
2. Engine-Flink
  - Flink-specific implementations
3. Framework-common
  - Framework-task jar & Custom task jar loader
  - Processing job/task managing modules
4. Framework-task
  - Machine learning task
  - Data models

The framework is divided into submodules to minimize dependency relations.
 -Dependencies
  -- `Framework`,`Engine-Flink` ---- dependent on ---> `Framework-common` module.
  -- `Framework-common`         ---- dependent on ---> `Framework-task/task-model` module.


