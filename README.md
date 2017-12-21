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

- Apache Flink
  - Version : 1.3
  - [How to execute Apache Flink Docker Image](engine/engine-flink/README.md)
  - Please visit [Apache Flink webiste](https://flink.apache.org) for detailed instructions on how to build and run Apache Flink.

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

- Kapacitor
  - Version : 1.3
  - [How to execute Kapacitor Docker Image](engine/engine-kapacitor/README.md)
  - For more information, please visit [Kapacitor website.](https://docs.influxdata.com/kapacitor/v1.4/introduction/installation/)

Remember, you must configure proxies for git and maven accordingly if necessary.

- Setting up proxy for git
```shell
git config --global http.proxy http://proxyuser:proxypwd@proxyserver.com:8080
```
- [Setting up proxy for maven](https://maven.apache.org/guides/mini/guide-proxies.html)


### Build Runtime ###
- Build Methods
   - Using MVN
    ```shell
    mvn clean package -DskipTests
    ```

    - Using Build Script
    ```shell
    cd tool/
    ./build_local
    ```
- Built Binaries
  - Data Processing Runtime is composed of four submodules.
    1. Runtime
       - Executable : runtime-0.1.0-SNAPSHOT.jar
       - Includes : RESTful APIs
       - Features : Abstracts interfaces for those open-source data processing engines
    2. Engine
       - Library : engine-flink-0.1.0-SNAPSHOT-jar-with-dependencies.jar
       - Features : Flink-App. which will execute runtime task(s).
                    Data ingest (zmq) & deliver(zmq, file, websocket) functionality included
       - Note : Kapacitor related codes is UDF(User defined Function) which will be merged into Kapacitor binary
                Therefore, no binary for the Kapacitor will be created.
    3. Runtime-common
       - Library : runtime-common-0.1.0-SNAPSHOT.jar
       - Features : runtime-task or Custom task (jar) loader
    4. Runtime-task
       - Libraries
         - [Task Model](runtime-task/TaskModel/readme.md) : task-model-0.1.0-SNAPSHOT.jar
         - [Regression Model](runtime-task/Regression/readme.md) : regression-0.1.0-SNAPSHOT.jar
         - [SVM Model](runtime-task/SVMModel/readme.md) : svm-0.1.0-SNAPSHOT.jar
         - [Query Model](runtime-task/QueryModel/readme.md) : query-model-0.1.0-SNAPSHOT.jar
       - Features : Data pre-processing & analytic task
  - The Data Processing Runtime is divided into submodules to minimize dependency relations.
    - Dependencies
      - `Runtime`,`Engine-Flink` ---- dependent on ---> `Runtime-common` module.
      - `Runtime-common`         ---- dependent on ---> `Runtime-task/task-model` module.

### Execute Runtime ###

- Create a shared resource directory for runtime and Apache Flink
  - Create `/runtime/ha` directory in system
  - If necessary, change directory ownership to user
  `chown -R user:user /runtime`

- Execute Apache Flink & Kapacitor ()
  - [Start Flink](#Apache Flink)
  - [Start Kapacitor](#Kapacitor)

- Execute Runtime
```shell
cd runtime/target/
java -jar ./runtime-0.1.0-SNAPSHOT.jar
```

### Test Runtime ###
- Now you should be able to make RESTful requests to http://localhost:8082/analytics
- Swagger UI interface is available at: http://localhost:8082/analytics/swagger-ui.html
  - Data Processing with Algorithm (ex: regression)
    - Data Processing Job Registration
      1. Open & Copy the contents inside the "regression_sample.json" in tools/sample_request directory
      2. Access Swagger (localhost:8082/analytics/swagger-ui.html) with browser
      3. Goto POST /v1/job & extend the menu
      4. Paste the contents into "json box of the Parameter slot" and click "Try it out!"
      5. Check the response : Success(200) or Fail(400)
    - Data Processing Job Execution
      6. Copy the "jobId" from the Response Body
      7. Goto POST /v1/job/{id}/execute & extend the menu
      8. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      9. Check the response : Success(200) or Fail(400)
    - Data Processing job Stop
      10. Copy the "jobId" from the Response Body
      11. Goto POST /v1/job/{id}/stop & extend the menu
      12. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      13. Check the response : Success(200) or Fail(400)
    - Data Processing job Delete
      14. Copy the "jobId" from the Response Body
      15. Goto DELETE /v1/job/{id} & extend the menu
      16. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      17. Check the response : Success(200) or Fail(400)
  - How to stream data to executing Data Processing job
    - You will need to create a sample app which streams the data thru one of the protocol
      which engine supports (Flink : zmq/ezMQ, Kapacitor : ezMQ)
    - Sample App will be provided later
  - How to receive result from executing Data Processing job
    - You will need to create a sample app which listens the stream thru one of the protocol
      which engine-flink supports (Flink : ezMQ/file/webSocket, Kapacitor : exMQ)
    - Sample App will be provided later
