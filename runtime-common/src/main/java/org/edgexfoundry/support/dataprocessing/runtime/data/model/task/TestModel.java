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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.task;

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;

/**
 * Class TestModel is provided only for unit tests.
 * Use this only when you want to ensure that your code works with arbitrary model.
 */
public final class TestModel implements TaskModel {

  @TaskParam(key = "age", uiType = UiFieldType.NUMBER, uiName = "Age")
  private int age;

  @TaskParam(key = "gender", uiType = UiFieldType.ENUMSTRING, uiName = "Gender")
  private Gender gender;

  @Override
  public TaskType getType() {
    return TaskType.INVALID;
  }

  @Override
  public String getName() {
    return TestModel.class.getSimpleName();
  }

  @Override
  public void setParam(TaskModelParam param) {

  }

  @Override
  public void setInRecordKeys(List<String> inRecordKeys) {

  }

  @Override
  public void setOutRecordKeys(List<String> outRecordKeys) {

  }

  @Override
  public DataSet calculate(DataSet in) {
    return in;
  }

  public enum Gender {
    Male, Female
  }
}
