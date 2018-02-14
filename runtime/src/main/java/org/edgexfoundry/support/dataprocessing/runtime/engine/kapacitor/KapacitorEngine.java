package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
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
import org.springframework.util.StringUtils;

public class KapacitorEngine extends AbstractEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(KapacitorEngine.class);
  private static final String TASK_ROOT = "/kapacitor/v1/tasks";
  private HTTP httpClient = null;

  private String host;
  private int port;

  /**
   * Request generator for each Kapacitor REST server. The corresponding method of an instance of
   * this class is called on each request, such as job deploying, stopping, or status monitoring
   *
   * @param host hostname of Kapacitor REST server
   * @param port port number of Kapacitor REST server
   */
  public KapacitorEngine(String host, int port) {
    this.host = host;
    this.port = port;
    this.httpClient = new HTTP();
    this.httpClient.initialize(host, port, "http");
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public void create(Job job) throws Exception {
    WorkflowData workflowData = job.getWorkflowData();

    JobState jobState = job.getState();
    jobState.setEngineType("KAPACITOR");
    jobState.setHost(host);
    jobState.setPort(port);

    ScriptGraph scriptGraph = new ScriptGraphBuilder().getInstance(workflowData);
    scriptGraph.initialize();
    String resultScript = scriptGraph.generateScript();

    if (StringUtils.isEmpty(resultScript)) {
      throw new RuntimeException("Failed to prepare Kapacitor script to request");
    }

    JsonObject jobInfo = getBaseJsonObject(job.getId());
    LOGGER.info("Kapacitor script is generated as following:\n{}", resultScript);
    jobInfo.addProperty("script", resultScript);

    // Post defined task to kapacitor
    JsonObject kapaResponse = this.httpClient.post(TASK_ROOT, jobInfo.toString())
        .getAsJsonObject();

    if (kapaResponse.get("id") == null) {
      jobState.setState(State.ERROR);
      jobState.setErrorMessage(kapaResponse.get("error").getAsString());
    } else {
      jobState.setState(State.CREATED);
      job.addConfig("script", resultScript);
      jobState.setEngineType("KAPACITOR");
      jobState.setEngineId(kapaResponse.get("id").getAsString());
    }
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
  public void run(Job job) throws Exception {
    if (job == null) {
      throw new NullPointerException("Job is null.");
    }

    if (job.getId() == null) {
      throw new IllegalStateException("Job id does not exist.");
    }

    if (job.getConfig("script") == null) {
      throw new IllegalStateException("Script for job(" + job.getId()
          + ") does not exist. Make sure script is generated first.");
    }

    String path = TASK_ROOT + '/' + job.getState().getEngineId();
    String flag = "{\"status\":\"enabled\"}";

    JsonObject kapaResponse = this.httpClient.patch(path, flag).getAsJsonObject();
    LOGGER.info("Job {} is now running on kapacitor {}", job.getId(), job.getState().getHost());

    if (kapaResponse == null) {
      throw new RuntimeException("Failed to run Kapacitor job; Please check out connection");
    }

    JobState jobState = job.getState();

    if (kapaResponse.get("id") == null) {
      jobState.setState(State.ERROR);
      jobState.setStartTime(System.currentTimeMillis());
      jobState.setErrorMessage(kapaResponse.get("error").getAsString());
    } else {
      jobState.setState(State.RUNNING);
      jobState.setStartTime(System.currentTimeMillis());
      jobState.setEngineId(kapaResponse.get("id").getAsString());
    }
  }

  @Override
  public void stop(Job job) throws Exception {
    if (job == null) {
      throw new NullPointerException("Job is null.");
    } else if (job.getState().getEngineId() == null) {
      throw new IllegalStateException("Engine id for the job does not exist.");
    }

    String path = TASK_ROOT + '/' + job.getState().getEngineId();
    String flag = "{\"status\":\"disabled\"}";

    JsonObject kapaResponse = this.httpClient.patch(path, flag).getAsJsonObject();
    LOGGER.debug("Job {} stop response: {}", job.getState().getEngineId(), kapaResponse);

    job.getState().setState(State.STOPPED);
    job.getState().setFinishTime(System.currentTimeMillis());
  }

  @Override
  public void delete(Job job) throws Exception {
  }

  @Override
  public boolean updateMetrics(JobState jobState) throws Exception {

    boolean isUpdated = false;

    JsonElement newState = this.httpClient.get(TASK_ROOT + "/" + jobState.getEngineId());
    State previousState = jobState.getState();
    if (newState == null) {
      if (!previousState.equals(State.STOPPED)) {
        jobState.setState(State.ERROR);
        jobState.setErrorMessage("Job " + jobState.getEngineId() + " is unreachable");
        return true;
      } else {
        return false;
      }
    }

    KapacitorJob kapaJob = new Gson().fromJson(newState.toString(), KapacitorJob.class);

    long lastModified = getTimestamp(kapaJob.getModified());

    // If creation is failed, there is no job information in Kapacitor. When running is invalid,
    // Kapacitor will save error message in "error" field. At the same time, "status" is "enabled"
    // and "executing" is false.
    jobState.setFinishTime(lastModified);

    if (!StringUtils.isEmpty(kapaJob.getError())) {
      if (!previousState.equals(State.ERROR)) {
        jobState.setState(State.ERROR);
        isUpdated = true;
      }
      jobState.setErrorMessage(kapaJob.getError());
      return isUpdated;
    }

    if (kapaJob.getStatus().equals("disabled") && !kapaJob.isExecuting()) {
      if (!previousState.equals(State.STOPPED)) {
        jobState.setState(State.STOPPED);
        isUpdated = true;
      }
      return isUpdated;
    }

    if (kapaJob.getStatus().equals("enabled") && kapaJob.isExecuting()) {
      if (!previousState.equals(State.RUNNING)) {
        jobState.setState(State.RUNNING);
        isUpdated = true;
      }
      return isUpdated;
    }

    return false;
  }

  private long getTimestamp(String isoDate) {
    return Instant.parse(isoDate).toEpochMilli();
  }
}
