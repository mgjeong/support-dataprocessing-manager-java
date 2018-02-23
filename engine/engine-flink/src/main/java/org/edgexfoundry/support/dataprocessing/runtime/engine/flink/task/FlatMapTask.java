/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatMapTask extends RichFlatMapFunction<DataSet, DataSet> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlatMapTask.class);

  private TaskModel task;

  private Map<String, Object> properties;

  public FlatMapTask(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    // Create task using TaskFactory which is made up by factory pattern.
    if (!properties.containsKey("jar") || !properties.containsKey("className")) {
      throw new IllegalStateException("Unable to load class for flatMap function");
    }
    String jarPath = (String) properties.get("jar");
    String targetClass = (String) properties.get("className");
    ClassLoader classLoader = getRuntimeContext().getUserCodeClassLoader();

    task = new ModelLoader(jarPath, classLoader).newInstance(targetClass);

    TaskModelParam taskModelParam = makeTaskModelParam();

    task.setParam(taskModelParam);
    task.setInRecordKeys((List<String>) properties.get("inrecord"));
    task.setOutRecordKeys((List<String>) properties.get("outrecord"));

    LOGGER.debug("{} is loaded as a FlatMap task ", this.task.getName());
  }

  private TaskModelParam makeTaskModelParam() {
    TaskModelParam nestedParams = new TaskModelParam();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      createTaskModelParam(nestedParams, entry.getKey(), entry.getValue());
    }
    return nestedParams;
  }

  private void createTaskModelParam(TaskModelParam parent, String key, Object value) {
    String[] tokens = key.split("/", 2);
    if (tokens.length == 1) { // terminal case
      parent.put(key, value);
      return;
    }
    // recurse
    TaskModelParam child = new TaskModelParam();
    parent.put(tokens[0], child);
    createTaskModelParam(child, tokens[1], value);
  }

  @Override
  public void flatMap(DataSet dataSet, Collector<DataSet> collector) throws Exception {
    dataSet = task.calculate(dataSet);
    if (dataSet != null) {
      collector.collect(dataSet);
    }
  }
}
