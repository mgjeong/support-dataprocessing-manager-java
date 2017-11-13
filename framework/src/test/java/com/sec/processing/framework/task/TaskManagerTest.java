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
package com.sec.processing.framework.task;

import com.sec.processing.framework.data.model.error.ErrorFormat;
import com.sec.processing.framework.data.model.task.TaskFormat;
import com.sec.processing.framework.db.TaskTableManager;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

// Try to make a class which extends TaskTableManager
// to handle all methods called in this test
// Then, inject that class into TaskManager member.
@PowerMockIgnore("javax.net.ssl.*")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest(TaskTableManager.class)
public class TaskManagerTest {

    @InjectMocks
    private static TaskManager taskManager;

    @Mock
    private static TaskTableManager taskTableManager;

    private static List<Map<String, String>> mockDB;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        mockStatic(TaskTableManager.class);
        taskTableManager = Mockito.mock(TaskTableManager.class);
        when(TaskTableManager.getInstance()).thenReturn(taskTableManager);
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

    public static List<Map<String, String>> makeNonRemovableMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> data = new HashMap<String, String>();
        data.put(TaskTableManager.Entry.removable.name(), "0");
        payload.add(data);
        return payload;
    }

    public static List<Map<String, String>> makeRemovableMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> data = new HashMap<String, String>();
        data.put(TaskTableManager.Entry.removable.name(), "1");
        data.put(TaskTableManager.Entry.path.name(), "/framework/ha/jar/task_user/test-model.jar");
        payload.add(data);
        return payload;
    }

    @AfterClass
    public static void finishTaskManager() {
        taskManager.terminate();
    }

    @Test
    public void testaGetTaskModel() throws Exception {
        mockDB = makeMockPayload();
        when(taskTableManager.getAllTaskProperty()).thenReturn(mockDB);

        List<TaskFormat> models = null;

        models = taskManager.getTaskModel("notexist");
        Assert.assertEquals(models.size(), 0);

        models = taskManager.getTaskModel("reg");
        Assert.assertEquals(models.size(), 2);

        models = taskManager.getTaskModel(TaskType.REGRESSION);
        Assert.assertEquals(models.size(), 2);

        TaskFormat model = taskManager.getTaskModel("cla", TaskType.CLASSIFICATION);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getName(), "cla");
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

    @Test
    public void testdUpdateTask() throws Exception {
        // Setup user jar
        // setupUserJar();
        byte[] testContent = "test content".getBytes();
        taskManager.addTask("test", testContent);

        taskManager.addTask("test.jar", testContent);

        testContent = getBytesFromFile("test-model.jar");
        taskManager.addTask("test.jar", testContent);

        taskManager.getTaskModelList();
        //taskManager.deleteTask(TaskType.INVALID, "test");
    }

    //FIXLATER: @Test
    public void testeDeleteTaskModel() throws Exception {
        List<TaskFormat> models = null;
        ErrorFormat result;
        // no data
        mockDB = makeEmptyMockPayload();
        when(taskTableManager.getTaskByTypeAndName(anyString(), anyString())).thenReturn(mockDB);
        result = taskManager.deleteTask(TaskType.REGRESSION, "reg");
        Assert.assertTrue(result.isError());

        // non removable
        mockDB = makeNonRemovableMockPayload();
        when(taskTableManager.getTaskByTypeAndName(anyString(), anyString())).thenReturn(mockDB);
        result = taskManager.deleteTask(TaskType.REGRESSION, "reg");
        Assert.assertTrue(result.isError());

        // removable
        mockDB = makeRemovableMockPayload();
        when(taskTableManager.getTaskByTypeAndName(anyString(), anyString())).thenReturn(mockDB);
        when(taskTableManager.getJarReferenceCount(anyString())).thenReturn(2);
        result = taskManager.deleteTask(TaskType.REGRESSION, "reg");
        Assert.assertTrue(result.isError());
        when(taskTableManager.getJarReferenceCount(anyString())).thenReturn(1);
        result = taskManager.deleteTask(TaskType.REGRESSION, "reg");
        Assert.assertTrue(result.isError());
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
