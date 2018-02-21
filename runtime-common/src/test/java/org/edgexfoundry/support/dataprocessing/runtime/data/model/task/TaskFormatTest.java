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
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;

public class TaskFormatTest {

  @Test
  public void testSetterAndGetter() {
    TaskFormat taskFormat = makeSampleTaskFormat();

    Assert.assertEquals("name", taskFormat.getName());
    Assert.assertEquals(TaskType.REGRESSION, taskFormat.getType());
    Assert.assertEquals(1L, taskFormat.getId().longValue());
    Assert.assertEquals(0, taskFormat.getInrecord().size());
    Assert.assertEquals(0, taskFormat.getOutrecord().size());
    Assert.assertEquals("classname", taskFormat.getClassName());
    Assert.assertEquals("jar", taskFormat.getJar());
    Assert.assertEquals(1, taskFormat.getParams().size());
  }

  @Test
  public void testConstructor() {
    TaskFormat t1 = makeSampleTaskFormat();
    TaskFormat t2 = new TaskFormat(t1);
    Assert.assertEquals(t1.getName(), t2.getName());
    Assert.assertEquals(t1.getType(), t2.getType());

    TaskFormat t3 = new TaskFormat(t1.getType(), t1.getName(), t1.getParams().toString());
    Assert.assertEquals(t1.getName(), t3.getName());
    Assert.assertEquals(t1.getType(), t3.getType());
  }

  private TaskFormat makeSampleTaskFormat() {
    TaskFormat taskFormat = new TaskFormat();
    taskFormat.setName("name");
    taskFormat.setType(TaskType.REGRESSION);
    taskFormat.setId(1L);
    taskFormat.setInrecord(new ArrayList<>());
    taskFormat.setOutrecord(new ArrayList<>());
    taskFormat.setClassName("classname");
    taskFormat.setJar("jar");
    TaskModelParam param = new TaskModelParam();
    param.put("sampleKey", 1);
    taskFormat.setParams(param);
    return taskFormat;
  }
}
