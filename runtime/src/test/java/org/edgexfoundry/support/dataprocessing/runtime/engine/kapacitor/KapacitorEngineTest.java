package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import static org.mockito.ArgumentMatchers.any;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraphBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.StringUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KapacitorEngine.class, ScriptGraphBuilder.class, HTTP.class})
public class KapacitorEngineTest {

  private KapacitorEngine engine;
  private MockKapacitorRest server;
  private WorkflowData spec;

  private static final String HOST = "localhost";
  private static final int PORT = 9092;

  @Before
  public void setUp() throws Exception {
    server = new MockKapacitorRest();
    server.on();
    PowerMockito.mockStatic(HTTP.class);
    PowerMockito.whenNew(HTTP.class).withNoArguments().thenReturn(server);

    engine = new KapacitorEngine(HOST, PORT);
    spec = new WorkflowData();
    spec.setWorkflowId(51423L);
  }

  @Test
  public void testGetHostAndPort() {
    Assert.assertEquals(engine.getHost(), HOST);
    Assert.assertEquals(engine.getPort(), PORT);
  }

  @Test
  public void testLifeCycle() throws Exception {
    prepareTestJob();
    Job job = Job.create(spec);
    engine.create(job);
    Assert.assertEquals(job.getState().getState(), State.CREATED);

    engine.run(job);
    Assert.assertEquals(job.getState().getState(), State.RUNNING);

    engine.stop(job);
    Assert.assertEquals(job.getState().getState(), State.STOPPED);
  }

  @Test
  public void testCreateWithInvalidSpec() throws Exception {
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testRunWithInvalidJobs() throws Exception {
    try {
      engine.run(null);
    } catch (NullPointerException e) {
      try {
        engine.run(new Job(null, 1L));
      } catch (IllegalStateException e1) {
        try {
          Job job = Job.create(spec);
          engine.run(job);
        } catch (IllegalStateException e2) {
          return;
        }
      }
    }
    Assert.fail("Expected errors did not occur");
  }

  @Test
  public void testRunWithInvalidRequests() throws Exception {
    Job unregistered = Job.create("jobId", 51423L);
    Map<String, Object> config = new HashMap<>();
    config.put("script", "var test = stream|from()");
    unregistered.setConfig(config);

    engine.run(unregistered);
    Assert.assertEquals(unregistered.getState().getState(), State.ERROR);
    Assert.assertNotNull(unregistered.getState().getErrorMessage());
  }

  @Test
  public void testStopWithInvalidJobs() throws Exception {
    try {
      engine.stop(null);
    } catch (NullPointerException e) {
      try {
        engine.stop(Job.create("jobId", 51423L));
      } catch (IllegalStateException e1) {
        return;
      }
    }
    Assert.fail("Expected errors did not occur");
  }

  @Test
  public void testLifeCycleWithServerOff() throws Exception {
    prepareTestJob();
    server.off();
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);

    server.on();
    engine.create(job);
    Assert.assertEquals(job.getState().getState(), State.CREATED);
    server.off();
    try {
      engine.run(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);

    server.on();
    engine.run(job);
    Assert.assertEquals(job.getState().getState(), State.RUNNING);
    server.off();
    try {
      engine.stop(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testTemporaries() throws Exception {
    engine.delete(new Job("jobId", 1L));
  }

  @Test
  public void testUpdateMetrics() throws Exception {
    prepareTestJob();
    Job job = Job.create(spec);
    engine.create(job);
    engine.run(job);
    engine.updateMetrics(job.getState());

    server.off();
    Assert.assertEquals(engine.updateMetrics(job.getState()), true);

    // Task stopped by Kapacitor, not throughout manager
    String path = "/kapacitor/v1/tasks/" + job.getState().getEngineId();
    String offForced = "{\"status\":\"disabled\"}";
    server.on();
    server.patch(path, offForced);
    Assert.assertEquals(engine.updateMetrics(job.getState()), true);

    String onForced = "{\"status\":\"enabled\"}";
    server.patch(path, onForced);
    Assert.assertEquals(engine.updateMetrics(job.getState()), true);

    String errorForced = "{\"error\":\"let's say error occurred\"}";
    server.patch(path, errorForced);
    Assert.assertEquals(engine.updateMetrics(job.getState()), true);
  }

  private void prepareTestJob() throws Exception {
    ScriptGraph graph = Mockito.mock(ScriptGraph.class);
    Mockito.doNothing().when(graph).initialize();
    Mockito.when(graph.generateScript()).thenReturn("var test = stream|from()");

    ScriptGraphBuilder builder = Mockito.mock(ScriptGraphBuilder.class);
    Mockito.when(builder.getInstance(any())).thenReturn(graph);

    PowerMockito.mockStatic(ScriptGraphBuilder.class);
    PowerMockito.whenNew(ScriptGraphBuilder.class).withNoArguments().thenReturn(builder);
  }

  private class MockKapacitorRest extends HTTP {

    private Map<String, JsonObject> jobs;
    private static final String TASK_ROOT = "/kapacitor/v1/tasks";

    private boolean alive = true;
    private int count;

    public MockKapacitorRest() {
      jobs = new HashMap<>();
    }

    public void on() {
      alive = true;
    }

    public void off() {
      alive = false;
    }

    @Override
    public HTTP initialize(String host, int port, String scheme) {
      return this;
    }

    @Override
    public JsonElement get(String path) {
      if (!alive) {
        return null;
      }

      if (!path.startsWith(TASK_ROOT)) {
        return null;
      }

      String id = path.split(TASK_ROOT + '/', 2)[1];
      if (jobs.containsKey(id)) {
        return jobs.get(id);
      } else {
        JsonObject error = new JsonObject();
        error.addProperty("error", "no task exists");
        return error;
      }
    }

    @Override
    public JsonElement post(String path, String dataString) {
      if (!alive) {
        return null;
      }

      if (!path.equals(TASK_ROOT)) {
        return null;
      }

      String id;
      try {
        JsonObject job = new Gson().fromJson(dataString, JsonObject.class);

        if (!job.has("type") || !job.has("dbrps") || !job.has("script")) {
          JsonObject error = new JsonObject();
          error.addProperty("error", "test error: wrong input");
          return error;
        }

        if (job.has("id")) {
          id = job.get("id").getAsString();
        } else {
          id = "test" + (++count);
          job.addProperty("id", id);
          job.addProperty("created", Instant.now().toString());
        }

        jobs.put(id, job);
        updateStatus(id);
      } catch (Exception e) {
        return null;
      }

      if (StringUtils.isEmpty(id)) {
        return null;
      }

      JsonObject answer = jobs.get(id);
      return answer;
    }

    @Override
    public JsonElement patch(String path, String dataString) {
      if (!alive) {
        return null;
      }

      if (!path.startsWith(TASK_ROOT)) {
        return null;
      }

      String id;
      try {
        id = path.split(TASK_ROOT + '/', 2)[1];
        if (jobs.containsKey(id)) {
          JsonObject patch = new Gson().fromJson(dataString, JsonObject.class);
          JsonObject job = jobs.get(id);

          for (Entry<String, JsonElement> entry : patch.entrySet()) {
            job.add(entry.getKey(), entry.getValue());
          }
          updateStatus(id);
        } else {
          throw new RuntimeException("Task does not exist");
        }
      } catch (Exception e) {
        JsonObject error = new JsonObject();
        error.addProperty("error", "task does not exist, cannot update");
        return error;
      }

      if (StringUtils.isEmpty(id)) {
        return null;
      }

      JsonObject answer = jobs.get(id);
      return answer;
    }

    private void updateStatus(String id) {
      JsonObject jobInfo = jobs.get(id);
      jobInfo.addProperty("modified", Instant.now().toString());
      if (jobInfo.has("status")) {
        if (jobInfo.get("status").getAsString().equals("enabled")) {
          jobInfo.addProperty("executing", true);
          jobInfo.addProperty("last-enabled", Instant.now().toString());
          return;
        }
      }
      jobInfo.addProperty("executing", false);
    }
  }
}
