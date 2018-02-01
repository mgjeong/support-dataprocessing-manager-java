Data Processing Manager
================================

Data Processing Manager makes it easy to manage, deploy and monitor data workflow(s) to multiple data processing engines of your choice.
A data workflow can be designed using [Data Processing Designer](https://github.sec.samsung.net/RS7-EdgeComputing/support-dataprocessing-designer).

### Features
  - Deploy data workflow to data processing engines as one or more workflow jobs
  - Execute/Stop/Update/Delete workflow jobs
  - Monitor workflow job status
  - Create and upload custom task model [More...](./runtime-task/TaskModel/README.md)
  - Export/Import workflow in json format
 
## Prerequisites ##
- Remember, you must configure proxies if necessary.
  - Setting up proxy for git
```shell
$ git config --global http.proxy http://proxyuser:proxypwd@proxyserver.com:8080
```
- JDK
  - Version : 1.8
  - [How to install](https://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_jdk.html)
- Maven
  - Version : 3.5.2
  - [Where to download](https://maven.apache.org/download.cgi)
  - [How to install](https://maven.apache.org/install.html)
  - [Setting up proxy for maven](https://maven.apache.org/guides/mini/guide-proxies.html)
- Apache Flink
  - Version : 1.3
  - [How to build Apache Flink Docker Image](engine/engine-flink/README.md)
  - [For more information, please visit Apache Flink website.](https://flink.apache.org)
- Kapacitor
  - Version : 1.3
  - [How to build Kapacitor Docker Image](engine/engine-kapacitor/README.md)
  - [For more information, please visit Kapacitor website.](https://docs.influxdata.com/kapacitor/v1.4/introduction/installation/)
- docker-ce
  - Version: 17.09
  - [How to install](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)

## How to build  ##
#### 1. Executable binary ####
```shell
$ ./build.sh
```
Note that, you can find other build scripts, **build_arm.sh** and **build_arm64**, which can be used to build the codes for ARM and ARM64 machines, respectively.

###### Binaries ######
- Data Processing Manager is composed of four submodules.
  - Manager
     - Executable : manager-0.1.0-SNAPSHOT.jar
     - Includes : RESTful APIs
     - Features : Abstracts interfaces for those open-source data processing engines
  - Manager-common
     - Library : manager-common-0.1.0-SNAPSHOT.jar
     - Features : runtime-task or Custom task (jar) loader
  - Engine
     - Library : engine-flink-0.1.0-SNAPSHOT-jar-with-dependencies.jar
     - Features : Flink-App. which will execute data processing task(s).
                  Data ingest (zmq) & deliver(zmq, file, websocket) functionality included
     - Note : The UDF(User defined Function) for the Kapacitor will be merged into Kapacitor binary
              Therefore, no binary for the Kapacitor will be created.
  - Runtime-task
     - Libraries
       - Task Model : task-model-0.1.0-SNAPSHOT.jar
       - [Regression Model](runtime-task/Regression/readme.md) : regression-0.1.0-SNAPSHOT.jar
       - [SVM Model](runtime-task/SVMModel/readme.md) : svm-0.1.0-SNAPSHOT.jar
       - Query Model : query-model-0.1.0-SNAPSHOT.jar
     - Features : Data pre-processing & analytic task
- The Data Processing Runtime is divided into submodules to minimize dependency relations.
  - Dependencies
    - `Manager`,`Engine-Flink` ---- dependent on ---> `Manager-common` module.
    - `Manager-common`         ---- dependent on ---> `Runtime-task/task-model` module.


#### 2. Docker Image ####
Next, you can create it to a Docker image.
```shell   
$ sudo docker build -t support-dataprocessing-manager -f Dockerfile .
```
If it succeeds, you can see the built image as follows:
```shell
$ sudo docker images
REPOSITORY                   TAG        IMAGE ID        CREATED           SIZE
support-dataprocessing-manager   latest     fcbbd4c401c2    SS seconds ago    XXX MB
```
Note that, you can find other Dockerfiles, **Dockerfile_arm** and **Dockerfile_arm64**, which can be used to dockerize for ARM and ARM64 machines, respectively.

## How to run  ##
#### Prerequisites ####
a. Create a shared resource directory for Manager and Engine(Apache Flink)
  - Create `/runtime/ha` directory in system
  - If necessary, change directory ownership to user `chown -R user:user /runtime`

b. Execute Apache Flink & Kapacitor
  - [How to execute Apache Flink Docker Image](engine/engine-flink/README.md)
  - [How to execute Kapacitor Docker Image](engine/engine-kapacitor/README.md)  

#### With Executable binary ####
```shell
$ run.sh    
```

#### With Docker Image ####
```shell
$ sudo docker run -it -p 8082:8082 support-dataprocessing-manager
```

#### APIs ####
Data Processing Manager provides RESTful APIs to manage and monitor workflow jobs.
A list of available APIs can be found at http://localhost:8082/swagger-ui.html.
- Example Scenario: Deploying a regression data workflow.
  - _TBA_

<!-- DEPRECATED
- Now you should be able to make RESTful requests to http://localhost:8082/
- Swagger UI interface is available at: http://localhost:8082/analytics/swagger-ui.html
  - Usecase : Data Processing with Algorithm (ex: regression)
    - Data Processing Job Registration
      1. Open and Copy the contents inside the "regression_sample.json" in tools/sample_request directory
      2. Access Swagger (localhost:8082/analytics/swagger-ui.html) with browser
      3. Goto POST /v1/job & extend the menu
      4. Paste the contents into "json box of the Parameter slot" and click "Try it out!"
      5. Check the response : Success(200) or Fail(400)
    - Data Processing Job Execution
      1. Copy the "jobId" from the Response Body
      2. Goto POST /v1/job/{id}/execute & extend the menu
      3. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      4. Check the response : Success(200) or Fail(400)
    - Data Processing job Stop
      1. Copy the "jobId" from the Response Body
      2. Goto POST /v1/job/{id}/stop & extend the menu
      3. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      4. Check the response : Success(200) or Fail(400)
    - Data Processing job Delete
      1. Copy the "jobId" from the Response Body
      2. Goto DELETE /v1/job/{id} & extend the menu
      3. Paste the "jobId" into "id box of the Parameter slot" and click "Try it out!"
      4. Check the response : Success(200) or Fail(400)
  - How to stream data to executing Data Processing job
    - You will need to create a sample app which streams the data thru one of the protocol
      which engine supports (Flink : zmq/ezMQ, Kapacitor : ezMQ)
    - Sample App will be provided later
  - How to receive result from executing Data Processing job
    - You will need to create a sample app which listens the stream thru one of the protocol
      which engine-flink supports (Flink : ezMQ/file/webSocket, Kapacitor : ezMQ)
    - Sample App will be provided later
-->
- Ports Information
  - Data Processing Manager : 8082 (default)
  - Flink : 8081 (default)
  - Kapacitor : 9092 (default)
  
## How to export/import a workflow ##
A workflow can be exported to, and imported from, JSON.

### Exporting workflow ###
There are three ways to make a workflow JSON.
1. Using [Data Processing Designer](https://github.sec.samsung.net/RS7-EdgeComputing/support-dataprocessing-designer)
  <br/>From 'My Workflows' page, select `Export` from upper right corner menu of a workflow.
  
2. Using REST API
  <br/>Refer to `Export workflow` REST API guide in [Swagger UI page](http://localhost:8082/swagger-ui.html).
  
3. Making JSON from scratch
  <br/>You could create your own JSON from scratch by referencing samples available in `tools/sample_request`.

The recommended way to export a workflow is to use Data Processing Designer.

### Importing workflow ###
There are two ways to import workflow from JSON.
1. Using [Data Processing Designer](https://github.sec.samsung.net/RS7-EdgeComputing/support-dataprocessing-designer)
  <br/>From 'My Workflows' page, select `Import Workflow` from upper right corner of the page.

2. Using REST API
  <br/>Refer to `Import workflow` REST API guide in [Swagger UI page](http://localhost:8082/swagger-ui.html).

### Sample workflow JSON ###
Sample workflow JSON files are available in `tools/sample_request`.

