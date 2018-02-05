/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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
package org.edgexfoundry.support.dataprocessing.runtime.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {TaskManager.class})
public class TaskControllerTest {

  @Test
  public void testConstructor() {
    TaskController taskController = new TaskController();
    Assert.assertNotNull(taskController);
  }

  @Test
  public void testAddCustomTask() throws Exception {
    TaskController taskController = spy(new TaskController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(taskController, "taskManager", taskManager);
    // test successful validation
    MultipartFile mocked = mock(MultipartFile.class);
    doReturn(false).when(mocked).isEmpty();
    doReturn("sample.jar").when(mocked).getOriginalFilename();
    doReturn("sample.jar").when(mocked).getName();
    doReturn("hello".getBytes()).when(mocked).getBytes();

    ResponseEntity responseEntity = taskController.addCustomTask(mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains(mocked.getOriginalFilename()));

    // test exception
    doThrow(new Exception("Mocked")).when(taskManager).addCustomTask(any(), any());
    responseEntity = taskController.addCustomTask(mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = taskController.addCustomTask(null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));
  }

  @Test
  public void testUpdateCustomTask() throws Exception {
    TaskController taskController = spy(new TaskController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(taskController, "taskManager", taskManager);
    // test successful validation
    MultipartFile mocked = mock(MultipartFile.class);
    doReturn(false).when(mocked).isEmpty();
    doReturn("sample.jar").when(mocked).getOriginalFilename();
    doReturn("sample.jar").when(mocked).getName();
    doReturn("hello".getBytes()).when(mocked).getBytes();

    ResponseEntity responseEntity = taskController.updateCustomTask("A", TaskType.INVALID, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("Success"));

    // test exception
    doThrow(new Exception("Mocked")).when(taskManager).updateCustomTask(any(), any(), any(), any());
    responseEntity = taskController.updateCustomTask("A", TaskType.INVALID, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = taskController.updateCustomTask(null, null, mocked);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = taskController.updateCustomTask("A", TaskType.INVALID, null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));
  }

  @Test
  public void testRemoveCustomTask() throws Exception {
    TaskController taskController = spy(new TaskController());
    TaskManager taskManager = mock(TaskManager.class);
    WhiteboxImpl.setInternalState(taskController, "taskManager", taskManager);
    // test successful validation

    ResponseEntity responseEntity = taskController.removeCustomTask("A", TaskType.INVALID);
    Assert.assertTrue(responseEntity.getBody().toString().contains("Success"));

    // test exception
    doThrow(new RuntimeException("Mocked")).when(taskManager).removeCustomTask(any(), any());
    responseEntity = taskController.removeCustomTask("A", TaskType.INVALID);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

    // test invalid param
    responseEntity = taskController.removeCustomTask(null, null);
    Assert.assertTrue(responseEntity.getBody().toString().contains("error"));

  }
}
