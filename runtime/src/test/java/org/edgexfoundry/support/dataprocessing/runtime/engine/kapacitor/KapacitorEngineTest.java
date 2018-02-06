package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import static org.mockito.ArgumentMatchers.any;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

  @Before
  public void setup() throws Exception {
    server = new MockKapacitorRest();
    server.on();
    PowerMockito.mockStatic(HTTP.class);
    PowerMockito.whenNew(HTTP.class).withNoArguments().thenReturn(server);

    engine = new KapacitorEngine("localhost", 9092);
    spec = new WorkflowData();
    spec.setWorkflowId(51423L);
  }

  @Test
  public void testLifeCycle() throws Exception {
    prepareTestJob();
    Job job = engine.create(spec);
    Assert.assertEquals(job.getState().getState(), State.CREATED);

    job = engine.run(job);
    Assert.assertEquals(job.getState().getState(), State.RUNNING);

    job = engine.stop(job);
    Assert.assertEquals(job.getState().getState(), State.STOPPED);
  }

  @Test
  public void testCreateWithInvalidSpec() {
    Job job = engine.create(spec);
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testRunWithInvalidJobs() {
    try {
      engine.run(null);
    } catch (NullPointerException e) {
      try {
        engine.run(new Job());
      } catch (IllegalStateException e1) {
        try {
          Job job = Job.create(spec.getWorkflowId());
          engine.run(job);
        } catch (IllegalStateException e2) {
          return;
        }
      }
    }
    Assert.fail("Expected errors did not occur");
  }

  @Test
  public void testRunWithInvalidRequests() {
    Job unregistered = Job.create(51423L);
    Map<String, Object> config = new HashMap<>();
    config.put("script", "var test = stream|from()");
    unregistered.setConfig(config);

    Job result = engine.run(unregistered);
    Assert.assertEquals(result.getState().getState(), State.ERROR);
    Assert.assertNotNull(result.getState().getErrorMessage());
  }

  @Test
  public void testStopWithInvalidJobs() {
    try {
      engine.stop(null);
    } catch (NullPointerException e) {
      try {
        engine.stop(Job.create(51423L));
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
    Job job = engine.create(spec);
    Assert.assertEquals(job.getState().getState(), State.ERROR);

    server.on();
    job = engine.create(spec);
    Assert.assertEquals(job.getState().getState(), State.CREATED);
    server.off();
    Job unfortunateJob = engine.run(job);
    Assert.assertEquals(unfortunateJob.getState().getState(), State.ERROR);

    server.on();
    job = engine.run(job);
    Assert.assertEquals(job.getState().getState(), State.RUNNING);
    server.off();
    job = engine.stop(job);
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testTemporaries() throws Exception {
    engine.delete(new Job());
    engine.getMetrics();
    engine.updateMetrics(new JobState());
    engine.getHost();
    engine.getPort();
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
      if (jobInfo.has("status")) {
        if (jobInfo.get("status").getAsString().equals("enabled")) {
          jobInfo.addProperty("executing", "true");
          return;
        }
      }
      jobInfo.addProperty("executing", "false");
    }
  }
}
