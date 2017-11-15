package org.edgexfoundry.support.dataprocessing.runtime.db;


import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DBConnector.class)
public class TaskTableManagerTest {
    @InjectMocks
    private static TaskTableManager taskTableManager = null;

    @Mock
    private static DBConnector dbConn = null;

    private static List<Map<String, String>> mockDB;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        mockStatic(DBConnector.class);
        dbConn = mock(DBConnector.class);
        when(DBConnector.getInstance()).thenReturn(dbConn);
        taskTableManager = TaskTableManager.getInstance();
    }

    @AfterClass
    public static void destroyTest() {
        if (null != taskTableManager) {
            taskTableManager.close();
            taskTableManager = null;
        }
    }

    private static List<Map<String, String>> makeMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();

        Map<String, String> regression0 = new HashMap<String, String>();
        regression0.put("type", "REGRESSION");
        regression0.put("name", "reg");
        regression0.put("path", "./test.jar");

        Map<String, String> regression1 = new HashMap<String, String>();
        regression1.put("type", "REGRESSION");
        regression1.put("name", "REG");
        regression1.put("path", "./test.jar");

        Map<String, String> classification = new HashMap<String, String>();
        classification.put("type", "CLASSIFICATION");
        classification.put("name", "cla");
        classification.put("path", "./test.jar");

        payload.add(regression0);
        payload.add(regression1);
        payload.add(classification);

        return payload;
    }

    @Test
    public void insertTaskTest() {
        boolean isCalled = false;
        try {
            taskTableManager.insertTask(null,
                    null,
                    null,
                    null,
                    null,
                    1);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.insertTask("TEST_TASK1",
                    "TEST1",
                    "{}",
                    "/TEST/JAR/TEST.JAR",
                    "TESK",
                    1);
        } catch (Exception e) {
            isCalled = true;
        }

        Assert.assertFalse(isCalled);
    }

    @Test
    public void getTaskTest() throws Exception {
        mockDB = makeMockPayload();
        when(dbConn.executeSelect(anyString())).thenReturn(mockDB);

        Assert.assertTrue(taskTableManager.getAllTaskProperty().size() > 0);
        Assert.assertTrue(taskTableManager.getTasks("negative_test").size() > 0);
        Assert.assertTrue(taskTableManager.getTaskByType("TEST_TASK2").size() > 0);
        Assert.assertTrue(taskTableManager.getTaskByName("TEST2").size() > 0);
        Assert.assertTrue(taskTableManager.getTaskByTypeAndName("TEST_TASK2",
                "TEST2").size() > 0);
        Assert.assertTrue(taskTableManager.getJarReferenceCount("TEST_TASK2", "TEST2") > 0);
        Assert.assertTrue(taskTableManager.getTaskByPath("TEST_TASK2").size() > 0);
        Assert.assertTrue(taskTableManager.getJarReferenceCount("TEST_TASK2") > 0);
        Assert.assertNotNull(taskTableManager.getJarPathByTypeAndName("TEST_TASK2", "TEST2"));
    }

    @Test
    public void getTasksNegativeTest() {
        boolean isCalled = false;
        try {
            taskTableManager.getTasks(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getTaskByType(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getTaskByName(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getTaskByTypeAndName(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getJarReferenceCount(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getJarPathByTypeAndName(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getTaskByPath(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.getJarReferenceCount(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);
    }


    @Test
    public void deleteTaskTest() throws Exception {
        taskTableManager.deleteTaskByTypeAndName("TEST_TASK1", "TEST1");
        taskTableManager.deleteTasksByTypeAndName("TEST_TASK1", "TEST1");
        taskTableManager.deleteTaskByPath("/TEST/JAR/TEST.JAR");
    }

    @Test
    public void deleteTaskNegativeTest() throws Exception {
        boolean isCalled = false;
        try {
            taskTableManager.deleteTaskByTypeAndName(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.deleteTasksByTypeAndName(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            taskTableManager.deleteTaskByPath(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);
    }
}


