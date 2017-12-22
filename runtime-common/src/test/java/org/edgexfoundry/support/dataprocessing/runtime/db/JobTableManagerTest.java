package org.edgexfoundry.support.dataprocessing.runtime.db;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SqliteConnector.class)
public class JobTableManagerTest {
    @InjectMocks
    private static JobTableManager jobTableManager = null;

    @Mock
    private static SqliteConnector dbConn = null;

    private static List<Map<String, String>> mockDB;

    private String testJobId = "ef91c5b3-b5bb-4e8b-8145-e271ac16ce30";

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        mockStatic(SqliteConnector.class);
        dbConn = mock(SqliteConnector.class);
        when(SqliteConnector.getInstance()).thenReturn(dbConn);
        jobTableManager = JobTableManager.getInstance();
    }

    @AfterClass
    public static void destroyTest() {
        if (null != jobTableManager) {
            jobTableManager.close();
            jobTableManager = null;
        }
    }

    private static List<Map<String, String>> makeMockPayload() {
        List<Map<String, String>> payload = new ArrayList<>();
        Map<String, String> p = new HashMap<>();

        // invalid param
        // Input
        JsonArray input = new JsonArray();
        JsonObject inputZMQ = new JsonObject();
        inputZMQ.addProperty("dataSource", "localhost:5555:topic");
        inputZMQ.addProperty("dataType", "ZMQ");
        input.add(inputZMQ);
        p.put(JobTableManager.Entry.input.name(), input.toString());

        // Output
        JsonArray output = new JsonArray();
        JsonObject outputZMQ = new JsonObject();
        outputZMQ.addProperty("dataSource", "localhost:5555:topic");
        outputZMQ.addProperty("dataType", "ZMQ");
        output.add(outputZMQ);
        p.put(JobTableManager.Entry.output.name(), output.toString());

        // Task
        JsonArray tasks = new JsonArray();
        JsonObject taskA = new JsonObject();
        taskA.addProperty("type", "PREPROCESSING");
        taskA.addProperty("name", "CsvParser");
        JsonObject taskAParam = new JsonObject();
        taskAParam.addProperty("delimiter", "\t");
        taskAParam.addProperty("index", "0");
        taskA.add("params", taskAParam);
        tasks.add(taskA);
        p.put(JobTableManager.Entry.taskinfo.name(), tasks.toString());
        payload.add(p);

        // valid param - create
        Map<String, String> p2 = new HashMap<>(p);
        p2.put(JobTableManager.Entry.jid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce11");
        p2.put(JobTableManager.Entry.gid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce10");
        p2.put(JobTableManager.Entry.state.name(), JobState.CREATE.toString());
        payload.add(p2);

        // valid param - running
        Map<String, String> p3 = new HashMap<>(p2);
        p3.put(JobTableManager.Entry.jid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce21");
        p3.put(JobTableManager.Entry.gid.name(), "ef91c5b3-b5bb-4e8b-8145-e271ac16ce20");
        p3.put(JobTableManager.Entry.state.name(), JobState.RUNNING.toString());
        payload.add(p3);
        return payload;
    }

    @Test
    public void insertJobTest() throws Exception {
        boolean isCalled = false;
        try {
            jobTableManager.insertJob(null, null, null, null, null, null, null, null, null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        jobTableManager.insertJob(testJobId, "gid_abcdefg", "Created", "zmq:111:111:111", "zmq:222:222:222",
                "type, name, params", "flinkid_abcdefg", "", "localhost:8082", "localhost:8082");
        jobTableManager.insertJob(testJobId, "gid_abcdefg", "Running", "zmq:111:111:111", "zmq:222:222:222",
                "type, name, params", "flinkid_abcdefg", "", "localhost:8082", "localhost:8082");
    }

    @Test
    public void updateJobTest() throws Exception {
        jobTableManager.updateGroupId(testJobId, "gid_abcdefg_update");
        jobTableManager.updateJob(testJobId, "gid_abcdefg_update", "Stop", "zmq:111:111:111", "zmq:222:222:222",
                "type, name, params, update", "flinkid_abcdefg_update");
        jobTableManager.updatePayload(testJobId, "zmq:123:xxx:333", "zmq:321:321:321",
                "type1, name1, params11");
        jobTableManager.updateState(testJobId, JobState.RUNNING.toString());
        jobTableManager.updateEngineId(testJobId, "flinkid_abcdefg_update");
    }

    @Test
    public void updateJobNativeTest() throws Exception {
        boolean isCalled = false;
        try {
            jobTableManager.updateJob("1",
                "RUNNING",
                "{}",
                null,
                "",
                "",
                "");
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.updateState("", null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.updateEngineId(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.updateGroupId(null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.updatePayload(null, null, null, null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);
    }

    @Test
    public void getJobTask() throws SQLException {
        mockDB = makeMockPayload();
        when(dbConn.executeSelect(anyString())).thenReturn(mockDB);

        Assert.assertTrue(jobTableManager.getAllJobs().size() > 0);
        Assert.assertTrue(jobTableManager.getEngineIdById(testJobId).size() > 0);
        Assert.assertTrue(jobTableManager.getGroupIdById(testJobId).size() > 0);
        Assert.assertTrue(jobTableManager.getPayloadById(testJobId).size() > 0);
        Assert.assertTrue(jobTableManager.getRowById(testJobId).size() > 0);
        Assert.assertTrue(jobTableManager.getStateById(testJobId).size() > 0);
    }

    @Test
    public void getJobNegativeTest() {
        boolean isCalled = false;
        try {
            jobTableManager.getStateById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.getPayloadById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.getEngineIdById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.getRowById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

        isCalled = false;
        try {
            jobTableManager.getGroupIdById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);

    }

    @Test
    public void deleteJobTest() throws SQLException {
        jobTableManager.deleteAllJob();
        jobTableManager.deleteJobById(testJobId);
    }

    @Test
    public void deleteJobNegativeTest() {
        boolean isCalled = false;
        try {
            jobTableManager.deleteJobById(null);
        } catch (Exception e) {
            isCalled = true;
        }
        Assert.assertTrue(isCalled);
    }
}
