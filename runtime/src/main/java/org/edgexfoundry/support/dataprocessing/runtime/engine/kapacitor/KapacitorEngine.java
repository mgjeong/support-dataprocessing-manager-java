package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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
  public Job create(WorkflowData workflow) throws Exception {
    return null;
  }

  @Override
  public Job run(Job job) throws Exception {
    return null;
  }

  @Override
  public Job stop(Job job) throws Exception {
    return null;
  }

  @Override
  public Job delete(Job job) throws Exception {
    return null;
  }

  @Override
  public ArrayList<JobState> getMetrics() throws Exception {
    return null;
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