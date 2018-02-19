/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkEngine extends AbstractEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlinkEngine.class);
  private String defaultJobJarLocation = Settings.RESOURCE_PATH;

  private String defaultLauncherJarLocation = Settings.RESOURCE_PATH
      + "engine-flink.jar";

  private HTTP httpClient = null;

  private String host;
  private int port;

  /**
   * Request generator for each Flink JobManager The corresponding method of an instance of this
   * class is called on each request, such as job deploying, stopping, or status monitoring.
   *
   * @param host hostname of Flink REST server
   * @param port port number of Flink REST server
   */
  public FlinkEngine(String host, int port) {
    this.host = host;
    this.port = port;

    this.httpClient = new HTTP();
    this.httpClient.initialize(host, port, "http");
  }

  @Override
  public void create(Job job) throws Exception {
    WorkflowData workflowData = job.getWorkflowData();
    job.setConfig(workflowData.getConfig());
    job.getState().setEngineType(EngineType.FLINK.name());
    job.getState().setHost(this.host);
    job.getState().setPort(this.port);

    List<Path> jobSpecificData = new ArrayList<>();
    jobSpecificData.addAll(getModelInfo(workflowData));
    jobSpecificData.add(prepareFlinkJobPlan(workflowData, job.getId()));

    // Generate flink jar to deploy
    Path jobJarFile = prepareJarToDeploy(jobSpecificData, job.getId());
    if (jobJarFile == null) {
      throw new RuntimeException("Failed to prepare jar file to deploy.");
    }

    // Upload jar to flink
    String launcherJarId = uploadLauncherJar(jobJarFile);
    if (launcherJarId == null) {
      throw new RuntimeException("Failed to upload Flink jar; Please check out connection");
    }

    // Update job
    job.addConfig("launcherJarId", launcherJarId);
    job.getState().setState(State.CREATED);
  }

  private Path prepareFlinkJobPlan(WorkflowData workflowData, String jobId) throws Exception {
    String jsonConfig = new Gson().toJson(workflowData);
    String targetPath = defaultJobJarLocation + jobId + ".json";
    Path configJson = Paths.get(targetPath);
    File configJsonFile = configJson.toFile();

    if (configJsonFile.exists()) {
      if (!configJsonFile.delete()) {
        throw new RuntimeException("Unable to delete old configuration " + configJson);
      }
    }

    FileUtils.writeStringToFile(configJsonFile, jsonConfig);

    return configJson.toAbsolutePath();
  }

  private Set<Path> getModelInfo(WorkflowData workflowData) {
    Set<Path> artifacts = new HashSet<>();
    for (WorkflowProcessor processor : workflowData.getProcessors()) {
      String className = processor.getClassname();
      processor.getConfig().getProperties().put("className", className);

      String jarPath = processor.getPath();

      Path artifact = Paths.get(jarPath);
      processor.getConfig().getProperties().put("jar", artifact.getFileName().toString());
      artifacts.add(artifact);
    }

    if (artifacts.size() == 0) {
      throw new IllegalStateException("At least one processor required");
    }
    return artifacts;
  }

  private Path prepareJarToDeploy(List<Path> jobData, String jarName) throws Exception {
    Path referenceJar = Paths.get(defaultLauncherJarLocation);
    Path targetJar = Paths.get(defaultJobJarLocation + jarName + ".jar");

    if (targetJar.toFile().exists()) {
      LOGGER.info("Delete old version of Job Jar file {}", targetJar.toAbsolutePath().toString());
      targetJar.toFile().delete();
    }
    Path newJar = Files.copy(referenceJar, targetJar);
    List<String> commands = new ArrayList<>();
    commands.add("jar");
    commands.add("uf");
    commands.add(newJar.toString());
    for (Path inputFile : jobData) {
      commands.add("-C");
      commands.add(inputFile.getParent().toString());
      commands.add(inputFile.getFileName().toString());
    }

    Process process = executeShellProcess(commands);
    ShellProcessResult shellProcessResult = waitProcessFor(process);
    if (shellProcessResult.exitValue != 0) {
      LOGGER.error("Adding job-specific data to jar is failed - exit code: {} / output: {}",
          shellProcessResult.exitValue, shellProcessResult.stdout);
      throw new RuntimeException("Workflow could not be deployed "
          + "successfully: fail to add config and artifacts to jar");
    }
    LOGGER.info("Added files to jar {}", jarName);
    return targetJar;
  }

  private Process executeShellProcess(List<String> commands) throws Exception {
    LOGGER.info("Executing command: " + Joiner.on(" ").join(commands));
    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.redirectErrorStream(true);
    return processBuilder.start();
  }

  private ShellProcessResult waitProcessFor(Process process)
      throws IOException, InterruptedException {
    StringWriter sw = new StringWriter();
    IOUtils.copy(process.getInputStream(), sw);
    String stdout = sw.toString();
    process.waitFor();
    int exitValue = process.exitValue();
    LOGGER.debug("Command output: " + stdout);
    LOGGER.debug("Command exit status: " + exitValue);
    return new ShellProcessResult(exitValue, stdout);
  }

  @Override
  public void run(Job job) throws Exception {
    if (job == null) {
      throw new NullPointerException("Job is null.");
    }

    if (job.getId() == null) {
      throw new IllegalStateException("Job id does not exist.");
    }

    String launcherJarId;
    if ((launcherJarId = job.getConfig("launcherJarId")) == null) {
      throw new IllegalStateException("Launcher jar for job(" + job.getId()
          + ") does not exist. Make sure job is created first.");
    }

    // Flink options
    Map<String, String> args = new HashMap<>();
    args.put("program-args", String.format("--internal --json %s", job.getId()));
    args.put("entry-class",
        "org.edgexfoundry.support.dataprocessing.runtime.engine.flink.Launcher");
    args.put("parallelism", "1");
    LOGGER.info("Running job {}({})", new Object[]{job.getId(), launcherJarId});

    // POST to flink
    JsonObject flinkResponse = this.httpClient
        .post("/jars/" + launcherJarId + "/run", args, true).getAsJsonObject();
    LOGGER.debug("/run response: {}", flinkResponse);

    // Parse flink response and update job state
    JobState jobState = job.getState();
    jobState.setStartTime(System.currentTimeMillis());

    if (flinkResponse.get("jobid") == null) {
      throw new RuntimeException(flinkResponse.get("error").getAsString());
    } else {
      jobState.setState(State.RUNNING);
      jobState.setEngineId(flinkResponse.get("jobid").getAsString());
    }
  }

  @Override
  public void stop(Job job) throws Exception {
    if (job == null) {
      throw new NullPointerException("Job is null.");
    }

    if (job.getState().getEngineId() == null) {
      throw new IllegalStateException("Engine id for the job does not exist.");
    }

    // DELETE to flink
    JsonElement flinkResponse = this.httpClient
        .delete("/jobs/" + job.getState().getEngineId() + "/cancel");
    LOGGER.debug("/jobs/{}/cancel response: {}", job.getState().getEngineId(), flinkResponse);

    // Result on success is {} (According to flink documentation)
    job.getState().setState(State.STOPPED);
    job.getState().setFinishTime(System.currentTimeMillis());
  }

  @Override
  public void delete(Job job) throws Exception {
    // TODO: delete?
  }

  @Override
  public boolean updateMetrics(JobState jobState) throws Exception {

    boolean isUpdated = false;

    JsonElement element = this.httpClient.get("/jobs/" + jobState.getEngineId());
    FlinkJob flinkJob = new Gson().fromJson(element.toString(), FlinkJob.class);

    jobState.setFinishTime(flinkJob.getEndtime());
    if (0 == flinkJob.getState().compareTo("FAILED")) {
      if (jobState.getState().name().compareTo(State.ERROR.name()) != 0) {
        jobState.setState(State.ERROR.name());
        isUpdated = true;
      }

      JsonElement jsonElement = this.httpClient.get("/jobs/" + flinkJob.getJid() + "/exceptions");
      FlinkException flinkException = new Gson()
          .fromJson(jsonElement.toString(), FlinkException.class);
      jobState.setErrorMessage(flinkException.getRootException());
    } else {
      if (0 == flinkJob.getState().compareTo("CANCELED")) {
        jobState.setState(State.STOPPED);
      } else {
        jobState.setState(flinkJob.getState());
      }
    }

    return isUpdated;
  }

  private String uploadLauncherJar(Path path) {
    File jarFile = path.toFile();
    JsonElement jsonString = this.httpClient.post("/jars/upload", jarFile);
    if (jsonString == null) {
      return null;
    }
    JsonObject jsonResponse = jsonString.getAsJsonObject();
    String jarId = jsonResponse.get("filename").getAsString(); // TODO: Exception handling
    return jarId;
  }

  private static class ShellProcessResult {

    private final int exitValue;
    private final String stdout;

    ShellProcessResult(int exitValue, String stdout) {
      this.exitValue = exitValue;
      this.stdout = stdout;
    }
  }
}
