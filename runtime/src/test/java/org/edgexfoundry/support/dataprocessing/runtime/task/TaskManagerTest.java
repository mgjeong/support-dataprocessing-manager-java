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

import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.commons.io.FileUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TestModel;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.internal.WhiteboxImpl;

public class TaskManagerTest {

  private static final File testDir = new File("./testDir/");
  private static final TaskManager taskManager = TaskManager.getInstance();

  @BeforeClass
  public static void generateTestJar() throws Exception {
    if (testDir.exists()) {
      FileUtils.deleteDirectory(testDir);
    }
    if (!testDir.mkdirs()) {
      throw new Exception("Failed to create test directory: " + testDir.getAbsolutePath());
    }

    // Make sample task model
    File testTaskModelJar = new File(testDir, "test.jar");
    makeSampleTaskModel(testTaskModelJar);

    // Initialize task manager
    taskManager.initialize(testDir.getAbsolutePath());
    WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
    WhiteboxImpl.setInternalState(taskManager, "workflowTableManager", workflowTableManager);
  }

  private static void makeSampleTaskModel(File testTaskModelJar)
      throws IOException, ClassNotFoundException {
    File tmp = new File(testTaskModelJar.getAbsolutePath() + ".tmp");
    tmp.deleteOnExit();
    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(tmp),
        new Manifest())) {
      String entry = TestModel.class.getName().replace('.', '/') + ".class";
      jos.putNextEntry(new JarEntry(entry));

      Class<?> testClass = Class.forName(TestModel.class.getName());
      final byte[] buf = new byte[128];
      try (InputStream classInputStream = testClass
          .getResourceAsStream(testClass.getSimpleName() + ".class")) {
        int readLength = classInputStream.read(buf);
        while (readLength != -1) {
          jos.write(buf, 0, readLength);
          readLength = classInputStream.read(buf);
        }
        classInputStream.close();
      }

      jos.closeEntry();
      jos.close();
    }
    tmp.renameTo(testTaskModelJar);
    testTaskModelJar.deleteOnExit();
    tmp.delete();
  }

  @Test
  public void testScanDir() throws Exception {
    taskManager.scanBuiltinTaskModel(testDir.getAbsolutePath());
  }

  @Test
  public void testCustomTask() throws Exception {
    File custom = new File(testDir, "custom.jar");
    makeSampleTaskModel(custom);
    int added = taskManager.uploadCustomTask("custom.jar", new FileInputStream(custom));
    Assert.assertTrue(added > 0);

    // invalid
    File invalidFile = new File(testDir, "invalid.txt");
    invalidFile.deleteOnExit();
    invalidFile.createNewFile();
    try {
      taskManager.uploadCustomTask("invalid.txt", new FileInputStream(invalidFile));
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    taskManager.terminate();
  }

  @Test
  public void testInvalidInitialization() throws Exception {
    try {
      taskManager.initialize(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      taskManager.uploadCustomTask(null, null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      taskManager.scanBuiltinTaskModel(null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    FileUtils.deleteDirectory(testDir);
  }
}
