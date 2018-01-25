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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskManager.class, WorkflowTableManager.class})
public class TaskManagerTest {

  @InjectMocks
  private static TaskManager taskManager;

  private static List<Map<String, String>> mockDB;

  @BeforeClass
  public static void setupEnvironment() throws Exception {
    taskManager = TaskManager.getInstance();
    taskManager.initialize();
  }

  public static List<Map<String, String>> makeMockPayload() {
    List<Map<String, String>> payload = new ArrayList<>();

    Map<String, String> regression0 = new HashMap<String, String>();
    regression0.put("type", "REGRESSION");
    regression0.put("name", "reg");

    Map<String, String> regression1 = new HashMap<String, String>();
    regression1.put("type", "REGRESSION");
    regression1.put("name", "REG");

    Map<String, String> classification = new HashMap<String, String>();
    classification.put("type", "CLASSIFICATION");
    classification.put("name", "cla");

    payload.add(regression0);
    payload.add(regression1);
    payload.add(classification);

    return payload;
  }

  public static List<Map<String, String>> makeEmptyMockPayload() {
    List<Map<String, String>> payload = new ArrayList<>();
    return payload;
  }

  @AfterClass
  public static void finishTaskManager() {
    taskManager.terminate();
  }

  @Test
  public void testbFileCreatedEventReceiver() {
    taskManager.fileCreatedEventReceiver(null);
    taskManager.fileRemovedEventReceiver(null);

    File testFile = new File("./TaskManagerTest_testFileCreatedEventReceiver.jar");
    try {
      if (!testFile.exists()) {
        testFile.createNewFile();
      }
    } catch (IOException e) {
      System.out.println("Error: Failed to create test input file.");
      testFile.delete();
      Assert.fail();
    }
    taskManager.fileCreatedEventReceiver(testFile.toString());

    testFile.delete();
    taskManager.fileRemovedEventReceiver(testFile.toString());
  }

  @Test
  public void testcScanTaskModel() {
    taskManager.scanTaskModel(null);
    //taskManager.scanTaskModel(Settings.FW_JAR_PATH);
  }

  private byte[] getBytesFromFile(String path) {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
    if (is == null) {
      return null;
    }

    try {
      return IOUtils.toByteArray(is);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
