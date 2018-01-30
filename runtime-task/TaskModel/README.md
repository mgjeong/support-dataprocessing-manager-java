Task Model SDK
================================

Task Model SDK enables you to implement a custom task model. 
A packaged jar file containing the custom task model can be uploaded to the existing Workflow Manager to be used in Workflow Designer.


## Creating a custom task model ##
Create a custom task model in following steps:
  1. Create a new maven project.
  2. To implement a custom task model, create a Java class and implement `org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel` interface.
  3. Mark task model parameters using `org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam` annotation.
  4. Package the source code to jar file with all dependencies.
  ```bash
  $ mvn clean package
  ```
  5. In the output directory, you should find a fat jar with all dependencies (ends with `jar-with-dependencies.jar`).
  6. Upload the fat jar to Workflow Manager from Workflow Designer or use an API that can be found at Workflow Manager's Swagger UI page.

## Tutorial with an example ##
In this tutorial, we will walk you through how to create a custom task model with a real running example.
Let's start by creating a `MinMaxTaskModel` that takes a list of numbers as input and outputs a minimum (or a maximum, depending on the user's choice) number.

### Creating `MinMaxTaskModel` maven project ####
1. Make a new maven project 

2. Edit `pom.xml` to add `TaskModel` SDK as a dependency
```xml
<dependency>
  <groupId>org.edgexfoundry.support.dataprocessing.runtime</groupId>
  <artifactId>task-model</artifactId>
  <version>${version}</version>
</dependency>
```
If maven fails to import `TaskModel` dependency, you could add it as a local dependency like this:
```xml
<dependency>
  <groupId>org.edgexfoundry.support.dataprocessing.runtime</groupId>
  <artifactId>task-model</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>system</scope>
  <systemPath>${project.basedir}/lib/task-model-0.1.0-SNAPSHOT.jar</systemPath>
</dependency>
```

3. Create a new Java class `MinMaxTaskModel.java` that implements `TaskModel` interface.
Depending on your needs, you could also extend `AbstractTaskModel`.
```java
public class MinMaxTaskModel extends AbstractTaskModel { 
  @Override
  public TaskType getType() {
    return TaskType.CUSTOM;
  }

  @Override
  public String getName() {
    return "MinMax";
  }

  @Override
  public void setParam(TaskModelParam taskModelParam) {
  }

  @Override
  public DataSet calculate(DataSet dataSet, List<String> inRecordKey, List<String> outRecordKey) {
    return null;
  }
}
```
Let's implement empty methods one by one.

4. `getType()` and `getName()` are, hopefully, self-explanatory.

5. To extract minimum(or maximum) from a list of numbers, our task model requires:
  - `Option option` that specifies whether to extract a minimum or a maximum value from the list.  
```java
@TaskParam(key = "option", uiName = "Option", uiType = UiFieldType.ENUMSTRING)
private Option option;

public enum Option {
  Minimum, Maximum
}
```

6. `setParam(TaskModelParam taskModelParam)` is called with task model parameter values entered by a user.
```java
public void setParam(TaskModelParam taskModelParam) {
  this.option = Option.valueOf((String) taskModelParam.get("option"));
}
```

7. Calculate minimum(or maximum)
```java
public DataSet calculate(DataSet dataSet) {
  String inRecord = inRecordKey.get(0); // input streaming data can be accessed using in-record keys
  String outRecord = outRecordKey.get(0); // processed data should be output using out-record keys
  List<Double> numbers = (List<Double>) dataSet.get(inRecord);
  Double answer = (option == Option.Minimum) ? numbers.stream().min(Double::compare).get()
      : numbers.stream().max(Double::compare).get();
  dataSet.setValue(outRecord, answer);
  return dataSet;
}
```

### Packaging into a jar file ###
Package the source code into a single jar with all dependencies.
Since no external library is used in this example, we are good to package it like this:
```bash
$ mvn clean package
```

Now we are ready to upload our custom task model to Workflow Manager!

### Uploading custom task jar ###
You can make a POST request to `http://{workflowManager}/api/v1/catalog/upload/task` .

The easiest way to upload custom task is to use [Swagger UI](http://localhost:8082/swagger-ui.html#!/Task_Manager/uploadCustomTaskUsingPOST) provided by the workflow manager.

Upload the packaged jar and we are done!

## Reminder ##
 - A packaged custom task model jar should not be larger than 8MB in size.
