package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph.ScriptGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KapacitorEngine extends AbstractEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(KapacitorEngine.class);
  private static final String TASK_ROOT = "/kapacitor/v1/tasks";
  private HTTP httpClient = null;

  private String script;

  public KapacitorEngine(String hostname, int port) {
    this.httpClient = new HTTP();
    this.httpClient.initialize(hostname, port, "http");
    this.script = null;
  }

  @Override
  public Job create(WorkflowData workflowData) throws Exception {
    Job job = Job.create(workflowData.getWorkflowId());

    String resultScript = "";
    try {
      ScriptGraph scriptGraph = new ScriptGraphBuilder().getInstance(workflowData)
          .initialize();
      resultScript = scriptGraph.generateScript();
    } catch (Exception e) {
      LOGGER.debug(e.getMessage());
    }

    if (resultScript.equals("")) {
      return null;
    }

    this.script = resultScript;

    String jobId = job.getId();
    JsonObject jobInfo = getBaseJsonObject(jobId);
    LOGGER.info("Kapacitor script is following:\n{}", this.script);
    jobInfo.addProperty("script", this.script);

    // Post defined task to kapacitor
    this.httpClient.post(TASK_ROOT, jobInfo.toString());

    job.getState().setState(State.CREATED);
    job.setConfig(workflowData.getConfig());
    job.getState().setEngineType("KAPACITOR");
    return job;
  }

  private JsonObject getBaseJsonObject(String jobId) {
    JsonObject jobInfo = new JsonObject();
    jobInfo.addProperty("id", jobId);
    jobInfo.addProperty("type", "stream");
    jobInfo.addProperty("status", "disabled");
    JsonArray dbrps = new JsonArray();
    JsonObject dbrp = new JsonObject();
    dbrp.addProperty("db", "dpruntime");
    dbrp.addProperty("rp", "autogen");
    dbrps.add(dbrp);
    jobInfo.add("dbrps", dbrps);

    return jobInfo;
  }

  @Override
  public Job run(Job job) throws Exception {
    String path = TASK_ROOT + '/' + job.getId();
    String flag = "{\"status\":\"enabled\"}";

    JsonObject kapaResponse = null;
    try {
      this.httpClient.patch(path, flag);
      // TO-DO: Exception handling
      LOGGER.info("Job {} is now running", job.getId());
    } catch (Exception e) {
      throw new Exception("Failed to get response from flink.", e);
    }

    job.getState().setState(State.RUNNING);
    job.getState().setStartTime(System.currentTimeMillis());
    job.getState().setEngineId(job.getId());

    return job;
  }

  @Override
  public Job stop(Job job) throws Exception {
    if (job == null) {
      throw new Exception("Job is null.");
    } else if (job.getState().getEngineId() == null) {
      throw new Exception("Engine id for the job does not exist.");
    }

    // DELETE to flink
    String path = TASK_ROOT + '/' + job.getId();
    String flag = "{\"status\":\"disabled\"}";

    JsonElement kapaResponse = null;
    try {
      kapaResponse = this.httpClient.patch(path, flag);
      LOGGER.debug("/jobs/{}/cancel response: {}", job.getState().getEngineId(), kapaResponse);
    } catch (Exception e) {
      throw new Exception("Failed to get response from kapacitor.", e);
    }

    // Result on success is {} (According to kapacitor documentation)
    job.getState().setState(State.STOPPED);
    return job;
  }

  @Override
  public Job delete(Job job) throws Exception {
//    String path = TASK_ROOT + '/' + job.getId();
//    JsonObject kapaResponse = null;
//    try {
//      kapaResponse = this.httpClient.delete(path);
//    } catch (HttpResponseException e) {
//      if (e.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
//        LOGGER.debug(e.getMessage());
//      }
//    } catch (Exception e) {
//      LOGGER.debug(e.getMessage());
//    } finally {
//
//      if (kapaResponse.get("jobid") == null) {
//        job.getState().setState(State.ERROR);
//        job.getState().setStartTime(System.currentTimeMillis());
//        job.getState().setErrorMessage(kapaResponse.get("error").getAsString());
//      } else {
//        job.getState().setState(State.STOPPED);
//        job.getState().setStartTime(System.currentTimeMillis());
//        job.getState().setEngineId(kapaResponse.get("jobid").getAsString());
//      }
//
//      return job;
//    }
    return job;
  }

  @Override
  public List<JobState> getMetrics() throws Exception {
    return null;
  }

  @Override
  public boolean updateMetrics(JobState jobState) throws Exception {
    return false;
  }

  @Override
  public String getHost() {
    return null;
  }

  @Override
  public int getPort() {
    return 0;
  }

/*
  @Override
  public JobResponseFormat createJob() {
    return new JobResponseFormat();
  }

  @Override
  public JobResponseFormat createJob(String jobId) {
    LOGGER.info("Kapacitor job {} is created", jobId);
    return createJob().setJobId(jobId);
  }

  @Override
  public String createJob(WorkflowData workflow) {
    String resultScript = "";
    try {
      ScriptGraph scriptGraph = new ScriptGraphBuilder().getInstance(workflow)
          .initialize();
      resultScript = scriptGraph.generateScript();
    } catch (Exception e) {
      LOGGER.debug(e.getMessage());
    }

    if (resultScript.equals("")) {
      return null;
    }

    this.script = resultScript;

    String jobId = workflow.getWorkflowName();
    JsonObject jobInfo = getBaseJsonObject(jobId);
    LOGGER.info("Kapacitor script is following: {}", this.script);
    jobInfo.addProperty("script", this.script);

    // Post defined task to kapacitor
    this.httpClient.post(TASK_ROOT, jobInfo.toString());
    LOGGER.info("Kapacitor Job Id {} is registered.", jobId);

    return workflow.getWorkflowName();
  }

  @Override
  public JobResponseFormat deploy(String jobId) {
    String path = TASK_ROOT + '/' + jobId;
    String flag = "{\"status\":\"enabled\"}";

    this.httpClient.patch(path, flag);
    // TO-DO: Exception handling
    LOGGER.info("Job {} is now running", jobId);

    JobResponseFormat responseFormat = new JobResponseFormat();
    responseFormat.setJobId(jobId);
    return responseFormat;
  }

  private JsonObject getBaseJsonObject(String jobId) {
    JsonObject jobInfo = new JsonObject();
    jobInfo.addProperty("id", jobId);
    jobInfo.addProperty("type", "stream");
    jobInfo.addProperty("status", "disabled");
    JsonArray dbrps = new JsonArray();
    JsonObject dbrp = new JsonObject();
    dbrp.addProperty("db", "dpruntime");
    dbrp.addProperty("rp", "autogen");
    dbrps.add(dbrp);
    jobInfo.add("dbrps", dbrps);

    return jobInfo;
  }

  @Override
  public JobResponseFormat run(String jobId) {
    JsonObject jobInfo = getBaseJsonObject(jobId);

    try {
      JobTableManager jobTableManager = JobTableManager.getInstance();
      Map<String, String> job = jobTableManager.getPayloadById(jobId).get(0);
      ObjectMapper mapper = new ObjectMapper();
      List<DataFormat> inputs =
          mapper.readValue(
              job.get(JobTableManager.Entry.input.name()), new TypeReference<List<DataFormat>>() {
              });
      List<DataFormat> outputs =
          mapper.readValue(
              job.get(JobTableManager.Entry.output.name()), new TypeReference<List<DataFormat>>() {
              });
      List<TaskFormat> tasks =
          mapper.readValue(
              job.get(JobTableManager.Entry.taskinfo.name()),
              new TypeReference<List<TaskFormat>>() {
              });
      ScriptFactory scriptFactory = new ScriptFactory();
      scriptFactory.setInputs(inputs);
      scriptFactory.setOutputs(outputs);
      scriptFactory.setTasks(tasks);
      String script = scriptFactory.getScript();

      LOGGER.info("Kapacitor script is following: {}", script);
      jobInfo.addProperty("script", script);

      // Post defined task to kapacitor
      this.httpClient.post(TASK_ROOT, jobInfo.toString());
      LOGGER.info("Kapacitor Job Id {} is registered.", jobId);

      String path = TASK_ROOT + '/' + jobId;
      String flag = "{\"status\":\"enabled\"}";

      this.httpClient.patch(path, flag);
      // TO-DO: Exception handling
      LOGGER.info("Job {} is now running", jobId);
      JobTableManager.getInstance().updateEngineId(jobId, null);

    } catch (Exception e) {
      LOGGER.error("Failed to retrieve JobInfoFormat: " + e.getMessage(), e);
    } finally {
      JobResponseFormat responseFormat = new JobResponseFormat();
      responseFormat.setJobId(jobId);
      return responseFormat;
    }
  }

  @Override
  public JobResponseFormat stop(String jobId) {
    String path = TASK_ROOT + '/' + jobId;
    String flag = "{\"status\":\"disabled\"}";

    try {
      this.httpClient.patch(path, flag);
      JobTableManager.getInstance().updateEngineId(jobId, null);
    } catch (Exception e) {
      LOGGER.debug(e.getMessage());
    } finally {
      JobResponseFormat responseFormat = new JobResponseFormat();
      responseFormat.setJobId(jobId);
      return responseFormat;
    }
  }

  @Override
  public JobResponseFormat delete(String jobId) {
    String path = TASK_ROOT + '/' + jobId;
    try {
      this.httpClient.delete(path);
      JobTableManager.getInstance().updateEngineId(jobId, null);
    } catch (HttpResponseException e) {
      if (e.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
        LOGGER.debug(e.getMessage());
      }
    } catch (Exception e) {
      LOGGER.debug(e.getMessage());
    } finally {
      JobResponseFormat responseFormat = new JobResponseFormat();
      responseFormat.setJobId(jobId);
      return responseFormat;
    }
  }
*/
}