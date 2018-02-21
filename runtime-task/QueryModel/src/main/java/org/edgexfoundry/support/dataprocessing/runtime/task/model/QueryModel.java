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
package org.edgexfoundry.support.dataprocessing.runtime.task.model;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryModel extends AbstractTaskModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryModel.class);
  @TaskParam(key = "request", uiName = "Query", uiType = UiFieldType.STRING, tooltip = "Enter query string")
  private String request = null;

  @Override
  public TaskType getType() {
    return TaskType.QUERY;
  }

  @Override
  public String getName() {
    return "query";
  }

  @Override
  public void setParam(TaskModelParam params) {
    request = params.get("request").toString();
  }

  @Override
  public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
    LOGGER.debug(
        "[Query] query is not supported in this engine, and therefore, data will be bypassed");
    return in;
  }
}
