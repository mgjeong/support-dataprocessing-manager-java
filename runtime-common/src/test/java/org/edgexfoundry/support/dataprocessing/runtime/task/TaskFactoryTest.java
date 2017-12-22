package org.edgexfoundry.support.dataprocessing.runtime.task;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.db.TaskTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskTableManager.class, HTTP.class, TaskFactory.class, TaskModelLoader.class})
public class TaskFactoryTest {
    @InjectMocks
    private static TaskFactory taskFactory;

    @Mock
    private static TaskTableManager taskTableManager;

    private static List<Map<String, String>> mockDB;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        mockStatic(TaskTableManager.class);
        taskTableManager = Mockito.mock(TaskTableManager.class);
        when(TaskTableManager.getInstance()).thenReturn(taskTableManager);
    }
    public static List<Map<String, String>> makeEmptyMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        return payload;
    }

    public static List<Map<String, String>> makeMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();

        Map<String, String> regression1 = new HashMap<String, String>();
        regression1.put(TaskTableManager.Entry.type.name(), TaskType.CLASSIFICATION.toString());
        regression1.put(TaskTableManager.Entry.name.name(), "svm");
        regression1.put(TaskTableManager.Entry.path.name(), "/runtime/ha/jar/task_user/test-model.jar");
        regression1.put(TaskTableManager.Entry.classname.name(), "org.edgexfoundry.support.dataprocessing.runtime.task.model.SVMModel");

        Map<String, String> classification = new HashMap<String, String>();
        classification.put(TaskTableManager.Entry.type.name(), TaskType.CLASSIFICATION.toString());
        classification.put(TaskTableManager.Entry.name.name(), "test");
        classification.put(TaskTableManager.Entry.path.name(), "/runtime/ha/jar/task_user/test-model.jar");
        classification.put(TaskTableManager.Entry.classname.name(), "org.edgexfoundry.support.dataprocessing.runtime.task.model.Test");

        payload.add(regression1);
        payload.add(classification);
        return payload;
    }

    @Test
    public void getInstanceTest() {
        TaskFactory tf = TaskFactory.getInstance();
        Assert.assertNotNull(tf);

        TaskFactory tf2 = TaskFactory.getInstance();
        Assert.assertNotNull(tf);

        Assert.assertEquals(tf, tf2);
    }

    @Test
    public void createTaskModelInstNativeTest() throws Exception {
        mockDB = makeEmptyMockPayload();
        when(taskTableManager.getTaskByName(anyString())).thenReturn(mockDB);

        TaskFactory tf = TaskFactory.getInstance();

        Assert.assertNotNull(tf);

        boolean isCalled = false;
        try {
            tf.createTaskModelInst(TaskType.CLASSIFICATION,
                    "unknown",
                     ClassLoader.getSystemClassLoader(),
                    "localhost:8082");
        } catch (Exception e) {
            isCalled = true;
        }

        Assert.assertTrue(isCalled);
    }


    @Test
    public void getTaskJarInfo() {

        String json = "{" +
              "\"error\":{" +
                "\"errorCode\":\"DPFW_ERROR_NONE\"," +
                "\"errorMessage\":\"Success.\"" +
              "}," +
              "\"task\": [" +
                "{" +
                  "\"type\": \"TREND\"," +
                  "\"name\": \"sma\"," +
                  "\"inrecord\": []," +
                  "\"outrecord\": []," +
                  "\"jar\": \"moving-average-0.1.0-SNAPSHOT.jar\"," +
                  "\"className\": \"org.edgexfoundry.processing.runtime.task.model.SimpleMovingAverage\"" +
                "}" +
              "]" +
            "}";

        TaskFactory tf = TaskFactory.getInstance();
        Assert.assertNotNull(tf);

        ClassLoader classLoader = mock(ClassLoader.class);

        JsonElement element = new JsonParser().parse(json);

        try {
            mockStatic(HTTP.class, Mockito.RETURNS_DEEP_STUBS);
            mockStatic(TaskModelLoader.class, Mockito.RETURNS_DEEP_STUBS);

            HTTP httpClient = mock(HTTP.class);
            TaskModelLoader modelLoader = mock(TaskModelLoader.class);
            TaskModel model = mock(TaskModel.class);

            whenNew(HTTP.class).withAnyArguments().thenReturn(httpClient);
            whenNew(TaskModelLoader.class).withAnyArguments().thenReturn(modelLoader);

            when(httpClient.initialize(anyString(), any())).thenReturn(httpClient);
            when(httpClient.get(anyString(), any())).thenReturn(element);
            when(httpClient.get("/analytics/v1/task/jar/" + "moving-average-0.1.0-SNAPSHOT.jar", null,
              "/runtime/ha/", "moving-average-0.1.0-SNAPSHOT.jar")).thenReturn(true);
            when(modelLoader.newInstance(any())).thenReturn(model);

            TaskModel taskModel = tf.createTaskModelInst(TaskType.REGRESSION, "name", classLoader, "127.0.0.1:8082");
            Assert.assertNotNull(taskModel);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }


    @Test
    public void nagGetTaskJarInfo() {

        String json = "{" +
              "\"error\":{" +
                "\"errorCode\":\"DPFW_ERROR_NONE\"," +
                "\"errorMessage\":\"Success.\"" +
              "}," +
              "\"task\": [" +
                "{" +
                  "\"type\": \"TREND\"," +
                  "\"name\": \"sma\"," +
                  "\"inrecord\": []," +
                  "\"outrecord\": []," +
                  "\"jar\": \"moving-average-0.1.0-SNAPSHOT.jar\"," +
                  "\"className\": \"org.edgexfoundry.processing.runtime.task.model.SimpleMovingAverage\"" +
                "}" +
              "]" +
            "}";

        TaskFactory tf = TaskFactory.getInstance();
        Assert.assertNotNull(tf);

        ClassLoader classLoader = mock(ClassLoader.class);

        JsonElement element = new JsonParser().parse(json);

        try {
            mockStatic(HTTP.class, Mockito.RETURNS_DEEP_STUBS);
            mockStatic(TaskModelLoader.class, Mockito.RETURNS_DEEP_STUBS);

            HTTP httpClient = mock(HTTP.class);
            TaskModelLoader modelLoader = mock(TaskModelLoader.class);
            TaskModel model = mock(TaskModel.class);

            whenNew(HTTP.class).withAnyArguments().thenReturn(httpClient);
            whenNew(TaskModelLoader.class).withAnyArguments().thenReturn(modelLoader);

            when(httpClient.initialize(anyString(), any())).thenReturn(httpClient);
            when(httpClient.get(anyString(), any())).thenReturn(element);
            when(httpClient.get("/analytics/v1/task/jar/" + "moving-average-0.1.0-SNAPSHOT.jar", null,
              "/runtime/ha/", "moving-average-0.1.0-SNAPSHOT.jar")).thenReturn(true);
            when(modelLoader.newInstance(any())).thenReturn(model);

            TaskModel taskModel = tf.createTaskModelInst(TaskType.REGRESSION, "name", classLoader, null);
            Assert.fail();          // It doesn't need to call, because we want to occur the exception.
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Test
    public void nagGetTaskJarInfo2() {

        String json = "{" + "\"error\":{" + "\"errorCode\":\"DPFW_ERROR_NONE\"," + "\"errorMessage\":\"Success.\"" + "}," + "\"task\": [" + "{" + "\"type\": \"TREND\"," + "\"name\": \"sma\"," + "\"inrecord\": []," + "\"outrecord\": []," + "\"jar\": \"moving-average-0.1.0-SNAPSHOT.jar\"," + "\"className\": \"org.edgexfoundry.processing.runtime.task.model.SimpleMovingAverage\"" + "}" + "]" + "}";

        TaskFactory tf = TaskFactory.getInstance();
        Assert.assertNotNull(tf);

        ClassLoader classLoader = mock(ClassLoader.class);

        JsonElement element = new JsonParser().parse(json);

        try {
            mockStatic(HTTP.class, Mockito.RETURNS_DEEP_STUBS);
            mockStatic(TaskModelLoader.class, Mockito.RETURNS_DEEP_STUBS);

            TaskModel taskModel = tf.createTaskModelInst(TaskType.REGRESSION, "name", classLoader, "127.0.0.1:8082");
            Assert.assertNull(taskModel);
        } catch (Exception e) {
            // Ecpected Error.
            e.printStackTrace();
        }
    }
}
