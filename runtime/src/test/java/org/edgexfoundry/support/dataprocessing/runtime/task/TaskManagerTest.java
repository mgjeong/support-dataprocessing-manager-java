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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import static org.powermock.api.mockito.PowerMockito.doReturn;
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
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
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
  public void testUpdateCustomTask() throws Exception {
    WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
    WhiteboxImpl.setInternalState(taskManager, "workflowTableManager", workflowTableManager);

    // Add
    File custom = new File(testDir, "custom_update.jar");
    makeSampleTaskModel(custom);
    int added = taskManager.addCustomTask("custom_update.jar", new FileInputStream(custom));
    Assert.assertTrue(added > 0);

    // Update
    WorkflowComponentBundle bundle = new WorkflowComponentBundle();
    bundle.setBuiltin(false);
    bundle.setTransformationClass(TestModel.class.getCanonicalName());
    bundle.setBundleJar(custom.getAbsolutePath());
    bundle.setName(TestModel.class.getSimpleName());
    bundle.setType(WorkflowComponentBundleType.PROCESSOR);
    bundle.setSubType(TaskType.INVALID.name());
    doReturn(bundle).when(workflowTableManager)
        .getWorkflowComponentBundle(TestModel.class.getSimpleName(), TaskType.INVALID.name());

    int updated = taskManager
        .updateCustomTask(TestModel.class.getSimpleName(), TaskType.INVALID, "custom.jar",
            new FileInputStream(custom));
    Assert.assertTrue(updated > 0);

    // invalid
    try {
      taskManager
          .updateCustomTask("invalid", TaskType.INVALID, "custom.jar", new FileInputStream(custom));
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testRemoveCustomTask() throws Exception {
    WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
    WhiteboxImpl.setInternalState(taskManager, "workflowTableManager", workflowTableManager);

    // Add
    File custom = new File(testDir, "custom_remove.jar");
    makeSampleTaskModel(custom);
    int added = taskManager.addCustomTask("custom_remove.jar", new FileInputStream(custom));
    Assert.assertTrue(added > 0);

    // Remove
    WorkflowComponentBundle bundle = new WorkflowComponentBundle();
    bundle.setBuiltin(false);
    bundle.setTransformationClass(TestModel.class.getCanonicalName());
    bundle.setBundleJar(custom.getAbsolutePath());
    bundle.setName(TestModel.class.getSimpleName());
    bundle.setType(WorkflowComponentBundleType.PROCESSOR);
    bundle.setSubType(TaskType.INVALID.name());
    doReturn(bundle).when(workflowTableManager)
        .getWorkflowComponentBundle(TestModel.class.getSimpleName(), TaskType.INVALID.name());

    taskManager.removeCustomTask(TestModel.class.getSimpleName(), TaskType.INVALID);

    // invalid
    try {
      taskManager.removeCustomTask("invalid", TaskType.INVALID);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testAddCustomTask() throws Exception {
    WorkflowTableManager workflowTableManager = mock(WorkflowTableManager.class);
    WhiteboxImpl.setInternalState(taskManager, "workflowTableManager", workflowTableManager);

    // Add
    File custom = new File(testDir, "custom_add.jar");
    makeSampleTaskModel(custom);
    int added = taskManager.addCustomTask("custom_add.jar", new FileInputStream(custom));
    Assert.assertTrue(added > 0);

    // invalid
    File invalidFile = new File(testDir, "invalid.txt");
    invalidFile.deleteOnExit();
    invalidFile.createNewFile();
    try {
      taskManager.addCustomTask("invalid.txt", new FileInputStream(invalidFile));
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
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
      taskManager.addCustomTask(null, null);
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
