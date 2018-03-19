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

import java.util.ArrayList;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TestModel.Gender;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;

public class TestModelTest {

  @Test
  public void testConstructor() {
    TestModel testModel = new TestModel();
    Assert.assertTrue(testModel instanceof TaskModel);
  }

  @Test
  public void testSetterAndGetter() {
    TestModel testModel = new TestModel();
    testModel.setParam(new TaskModelParam());
    testModel.setInRecordKeys(new ArrayList<>());
    testModel.setOutRecordKeys(new ArrayList<>());
    Assert.assertEquals(TaskType.INVALID, testModel.getType());
    Assert.assertEquals(TestModel.class.getSimpleName(), testModel.getName());
    testModel.calculate(DataSet.create());

    Assert.assertEquals(2, Gender.values().length);
  }
}
