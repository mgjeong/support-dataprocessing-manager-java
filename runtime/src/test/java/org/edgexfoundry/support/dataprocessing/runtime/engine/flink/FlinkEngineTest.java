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
package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.http.HttpException;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FlinkEngineTest {

  private static final String TEST_DIR = FlinkEngineTest.class.getResource(".").getPath();
  private static final String TEST_JAR = "test.jar";
  private static final String TEST_JAR_PATH = TEST_DIR + TEST_JAR;
  private FlinkEngine engine;
  private MockFlinkRest server = new MockFlinkRest();
  private WorkflowData spec;

  @Before
  public void setup() throws Exception {
    mockRestServer();
  }

  private void mockRestServer() throws Exception {
    server.boot();
    server.capable();

    engine = new FlinkEngine("localhost", 8081);
    Field f = FlinkEngine.class.getDeclaredField("httpClient");
    f.setAccessible(true);
    f.set(engine, server);

    f = FlinkEngine.class.getDeclaredField("defaultLauncherJarLocation");
    f.setAccessible(true);
    f.set(engine, TEST_JAR_PATH);

    f = FlinkEngine.class.getDeclaredField("defaultJobJarLocation");
    f.setAccessible(true);
    f.set(engine, TEST_DIR);
  }

  private void prepareResources() throws Exception {
    File jar = new File(TEST_JAR_PATH);
    if (jar.exists()) {
      if (!jar.delete()) {
        throw new RuntimeException("Failed to delete test resource before unit test");
      }
    }

    jar.createNewFile();
  }

  private void makeSampleData() {
    spec = new WorkflowData();
    spec.setWorkflowId(1234L);

    WorkflowProcessor processor = new WorkflowProcessor();
    processor.setClassname("testClass");
    processor.setPath(TEST_JAR_PATH);

    List<WorkflowProcessor> processorList = new ArrayList<>();
    processorList.add(processor);

    spec.setProcessors(processorList);
  }

  @After
  public void tearDown() {
    File jar = new File(TEST_JAR_PATH);
    if (jar.exists()) {
      if (!jar.delete()) {
        throw new RuntimeException("Failed to delete test resource after unit tests");
      }
    }
  }

  @AfterClass
  public static void clear() {
    File[] files = new File(TEST_DIR).listFiles((dir, name) ->
        name.matches(".*\\.tmp")
    );

    for (File file : files) {
      if (!file.delete()) {
        throw new RuntimeException("Cannot delete temporary files after test class");
      }
    }
  }

  @Test
  public void testLifeCycle() throws Exception {
    prepareResources();
    makeSampleData();

    Job job = Job.create(spec);
    engine.create(job);
    deleteIntermediates(job.getId());
    Assert.assertEquals(job.getState().getState(), State.CREATED);

    engine.run(job);
    Assert.assertEquals(job.getState().getState(), State.RUNNING);

    engine.stop(job);
    Assert.assertEquals(job.getState().getState(), State.STOPPED);
  }

  @Test
  public void testCreateWithInvalidPlan() throws Exception {
    spec = new WorkflowData();
    spec.setWorkflowId(15423L);
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testCreateWithJarCompositionFail() throws Exception {
    File dir = new File(TEST_JAR_PATH);
    if (dir.exists()) {
      Assert.fail("Test resource is not torn down clearly");
    }
    dir.mkdirs();
    File file = new File(dir.getPath() + TEST_JAR);
    file.createNewFile();

    makeSampleData();
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }

    file.delete();
    deleteIntermediates(job.getId());
  }

  @Test
  public void testRunWithInvalidJobs() throws Exception {
    try {
      engine.run(null);
    } catch (NullPointerException e) {
      try {
        engine.run(new Job(null, null));
      } catch (IllegalStateException e1) {
        try {
          Job job = new Job("FlinkEngineTest", 1L);
          engine.run(job);
        } catch (IllegalStateException e2) {
          return;
        }
      }
    }

    Assert.fail("Expected errors did not occur");
  }

  @Test
  public void testStopWithInvalidJobs() throws Exception {
    try {
      engine.stop(null);
    } catch (NullPointerException e) {
      try {
        engine.run(new Job(null, null));
      } catch (IllegalStateException e1) {
        return;
      }
    }

    Assert.fail("Expected errors did not occur");
  }

  @Test
  public void testCreateWithServerOff() throws Exception {
    server.shutdown();
    prepareResources();
    makeSampleData();
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    deleteIntermediates(job.getId());
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testRunWithServerOff() throws Exception {
    prepareResources();
    makeSampleData();
    Job job = Job.create(spec);
    engine.create(job);
    Assert.assertEquals(job.getState().getState(), State.CREATED);
    deleteIntermediates(job.getId());

    server.shutdown();
    try {
      engine.run(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testStopWithServerOff() throws Exception {
    prepareResources();
    makeSampleData();
    Job job = Job.create(spec);
    engine.create(job);
    Assert.assertEquals(job.getState().getState(), State.CREATED);
    deleteIntermediates(job.getId());

    try {
      engine.run(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.RUNNING);

    server.shutdown();
    try {
      engine.stop(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  @Test
  public void testLifeCycleWithServerBroken() throws Exception {
    server.incapable();
    prepareResources();
    makeSampleData();
    Job job = Job.create(spec);
    try {
      engine.create(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    deleteIntermediates(job.getId());
    Assert.assertEquals(job.getState().getState(), State.ERROR);

    server.capable();
    engine.create(job);
    deleteIntermediates(job.getId());
    Assert.assertEquals(job.getState().getState(), State.CREATED);

    server.incapable();
    try {
      engine.run(job);
    } catch (Exception e) {
      job.getState().setState(State.ERROR);
    }
    Assert.assertEquals(job.getState().getState(), State.ERROR);
  }

  private void deleteIntermediates(String id) {
    File json = new File(TEST_DIR + id + ".json");
    if (!json.exists()) {
      Assert.fail("Failed to create json file for workflow configuration");
    }

    File jar = new File(TEST_DIR + id + ".jar");
    if (!jar.exists()) {
      Assert.fail("Failed to create launcher jar");
    }

    json.delete();
    jar.delete();
  }

  @Test
  public void testDelete() throws Exception {
    engine.delete(null);
  }

  private class MockFlinkRest extends HTTP {

    private Map<Integer, String> jobs;
    private Map<String, String> jars;
    private Map<String, Map<String, Function<String, JsonElement>>> post;
    private Map<String, Map<String, Function<String, JsonElement>>> delete;

    private boolean alive = true;
    private boolean saturated = true;
    private int count;

    public MockFlinkRest() {
      jars = new HashMap<>();
      jobs = new HashMap<>();
      post = new HashMap<>();
      delete = new HashMap<>();
      addFlinkRestHandlers();
    }

    private void addFlinkRestHandlers() {
      Map<String, Function<String, JsonElement>> postToJars = new HashMap<>();
      postToJars.put("run", this::jarRun);
      postToJars.put("upload", this::jarUpload);
      post.put("jars", postToJars);

      Map<String, Function<String, JsonElement>> deleteToJobs = new HashMap<>();
      deleteToJobs.put("cancel", this::jobCancel);
      delete.put("jobs", deleteToJobs);
    }

    private JsonElement getService(String rest, List<String> options) {
      Map<String, Map<String, Function<String, JsonElement>>> restMap;

      switch (rest) {
        case "post":
          restMap = post;
          break;
        case "delete":
          restMap = delete;
          break;
        default:
          return null;
      }

      String location = options.get(0);
      String ops = options.get(1);
      String entity = null;
      if (options.size() > 2) {
        entity = options.get(2);
      }

      if (!saturated) {
        if (restMap.containsKey(location)) {
          Map<String, Function<String, JsonElement>> handlers = restMap.get(location);
          if (handlers.containsKey(ops)) {
            return handlers.get(ops).apply(entity);
          }
        }
      }
      return null;
    }

    public void capable() {
      saturated = false;
    }

    public void incapable() {
      saturated = true;
    }

    public void boot() {
      alive = true;
    }

    public void shutdown() {
      alive = false;
    }

    @Override
    public JsonElement post(String path, Map<String, String> args, boolean useArgAsParam) {
      if (alive) {
        return getService("post", parsePath(path));
      }

      return null;
    }

    @Override
    public JsonElement post(String path, File fileToUpload) {
      if (alive) {
        List<String> options = parsePath(path);
        options.add(fileToUpload.getPath());
        return getService("post", options);
      }

      return null;
    }

    @Override
    public JsonElement delete(String path) throws Exception {
      if (alive) {
        return getService("delete", parsePath(path));
      }
      throw new HttpException("404 not found");
    }

    private List<String> parsePath(String path) {
      String[] splits = path.split("/", 0);
      int length = splits.length;

      List<String> res = new ArrayList<>();
      if (length < 3) {
        return null;
      }
      res.add(splits[1]);
      res.add(splits[length - 1]);

      if (length == 4) {
        res.add(splits[2]);
      }

      if (length > 4) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < length - 1; i++) {
          sb.append(splits[i]);
          sb.append("/");
        }
        res.add(sb.toString());
      }

      return res;
    }

    private JsonElement jarUpload(String filePath) {
      String filename = Paths.get(filePath).getFileName().toString();
      jars.put(filename, filePath);
      JsonObject answer = new JsonObject();
      answer.addProperty("filename", filename);
      return answer;
    }

    private JsonElement jarRun(String id) {
      if (jars.containsKey(id)) {
        jobs.put(++count, id);
        JsonObject answer = new JsonObject();
        answer.addProperty("jobid", String.valueOf(count));
        return answer;
      }
      return null;
    }

    private JsonElement jobCancel(String id) {
      int integerId = Integer.parseInt(id);
      if (jobs.containsKey(integerId)) {
        if (jobs.get(integerId).equals("running")) {
          jobs.put(integerId, "canceled");
          JsonObject answer = new JsonObject();
          return answer;
        }
      }
      return null;
    }
  }
}
