package org.edgexfoundry.processing.runtime.task;

import org.edgexfoundry.processing.runtime.db.TaskTableManager;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest(TaskTableManager.class)


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
        regression1.put(TaskTableManager.Entry.classname.name(), "org.edgexfoundry.processing.runtime.task.model.SVMModel");

        Map<String, String> classification = new HashMap<String, String>();
        classification.put(TaskTableManager.Entry.type.name(), TaskType.CLASSIFICATION.toString());
        classification.put(TaskTableManager.Entry.name.name(), "test");
        classification.put(TaskTableManager.Entry.path.name(), "/runtime/ha/jar/task_user/test-model.jar");
        classification.put(TaskTableManager.Entry.classname.name(), "org.edgexfoundry.processing.runtime.task.model.Test");

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
                     ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            isCalled = true;
        }

        Assert.assertTrue(isCalled);
    }
}
