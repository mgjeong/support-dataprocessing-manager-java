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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FlinkEngine extends AbstractEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlinkEngine.class);
  private static final String DEFAULT_JOB_JAR_LOCATION = Settings.RESOURCE_PATH;

  private static final String DEFAULT_LAUNCHER_JAR_LOCATION = DEFAULT_JOB_JAR_LOCATION + "engine-flink.jar";

  private HTTP httpClient = null;

  private String host;
  private int port;

  public FlinkEngine(String host, int port) {

    setHost(host);
    setPort(port);

    this.httpClient = new HTTP();
    this.httpClient.initialize(host, port, "http");
  }

  private Path prepareFlinkJobPlan(WorkflowData workflowData, String jobId) {
    String jsonConfig = new Gson().toJson(workflowData);
    String targetPath = DEFAULT_JOB_JAR_LOCATION + jobId + ".json";
    Path configJson = Paths.get(targetPath);
    File configJsonFile = configJson.toFile();
    try {
      // OutputStreamWriter fileWriter = null;

      if (configJsonFile.exists()) {
        if (!configJsonFile.delete()) {
          throw new RuntimeException("Unable to old config json configuration" + configJson);
        }
      }

      FileUtils.writeStringToFile(configJsonFile, jsonConfig);

//      fileWriter = new OutputStreamWriter(new FileOutputStream(configJson));
//      fileWriter.write(jsonConfig);
//      fileWriter.flush();
    } catch (IOException e) {
      LOGGER.debug(e.getMessage());
    } finally {
//      try {
//        fileWriter.close();
//      } catch (IOException e) {
//        LOGGER.debug(e.getMessage());
//      }
    }
    return configJson.toAbsolutePath();
  }

  private Set<Path> getModelInfo(WorkflowData workflowData) {
    Set<Path> artifacts = new HashSet<>();
    try {
      for (WorkflowProcessor processor : workflowData.getProcessors()) {
        String name = processor.getName();
        String className = processor.getClassname();
        processor.getConfig().getProperties().put("className", className);

        String jarPath = processor.getPath();

        Path artifact = Paths.get(jarPath);
        processor.getConfig().getProperties().put("jar", artifact.getFileName().toString());
        artifacts.add(artifact);
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }

    return artifacts;
  }

  private String prepareJarToDeploy(List<Path> jobData, String jarName) throws Exception {
    Path referenceJar = Paths.get(DEFAULT_LAUNCHER_JAR_LOCATION);
    Path targetJar = Paths.get(DEFAULT_JOB_JAR_LOCATION + jarName + ".jar");

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
      LOGGER.error("Adding job-specific data to jar is failed - exit code: {} / output: {}", shellProcessResult.exitValue, shellProcessResult.stdout);
      throw new RuntimeException("Workflow could not be deployed " + "successfully: fail to add config and artifacts to jar");
    }
    LOGGER.info("Added files to jar {}", jarName);
    return targetJar.toAbsolutePath().toString();
  }

  private Process executeShellProcess(List<String> commands) throws Exception {
    System.out.println("Executing command: " + Joiner.on(" ").join(commands));
    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.redirectErrorStream(true);
    return processBuilder.start();
  }

  private ShellProcessResult waitProcessFor(Process process) throws IOException, InterruptedException {
    StringWriter sw = new StringWriter();
    IOUtils.copy(process.getInputStream(), sw);
    String stdout = sw.toString();
    process.waitFor();
    int exitValue = process.exitValue();
    System.out.println("Command output: " + stdout);
    System.out.println("Command exit status: " + exitValue);
    return new ShellProcessResult(exitValue, stdout);
  }

  @Override
  public Job create(WorkflowData workflowData) throws Exception {
    // Create job
    Job job = Job.create(workflowData.getWorkflowId());

    List<Path> jobSpecificData = new ArrayList<>();
    jobSpecificData.addAll(getModelInfo(workflowData));
    jobSpecificData.add(prepareFlinkJobPlan(workflowData, job.getId()));

    // Generate flink jar to deploy
    String jobJarFile = prepareJarToDeploy(jobSpecificData, job.getId());
    if (jobJarFile == null) {
      throw new Exception("Failed to prepare jar file to deploy.");
    }

    // Upload jar to flink
    String launcherJarId = uploadLauncherJar(jobJarFile);
    if (launcherJarId == null) {
      throw new Exception("Failed to upload Flink jar; Please check out connection");
    }

    // Update job
    job.getState().setState(State.CREATED);
    job.setConfig(workflowData.getConfig());
    job.addConfig("launcherJarId", launcherJarId);
    job.getState().setEngineType("FLINK");
    return job;
  }

  @Override
  public Job run(Job job) throws Exception {
    String launcherJarId;
    if (job == null) {
      throw new Exception("Job is null.");
    } else if (job.getId() == null) {
      throw new Exception("Job id does not exist.");
    } else if ((launcherJarId = job.getConfig("launcherJarId")) == null) {
      throw new Exception("Launcher jar for job(" + job.getId() + ") does not exist. Make sure job is created first.");
    }

    // Flink options
    Map<String, String> args = new HashMap<>();
    args.put("program-args", String.format("--internal --json %s", job.getId()));
    args.put("entry-class", "org.edgexfoundry.support.dataprocessing.runtime.engine.flink.Launcher");
    args.put("parallelism", "1");
    LOGGER.info("Running job {}({})", new Object[]{job.getId(), launcherJarId});

    // POST to flink
    JsonObject flinkResponse = null;
    try {
      flinkResponse = this.httpClient.post("/jars/" + launcherJarId + "/run", args, true).getAsJsonObject();
      LOGGER.debug("/run response: {}", flinkResponse);
    } catch (Exception e) {
      throw new Exception("Failed to get response from flink.", e);
    }

    // Parse flink response and update job state
    if (flinkResponse.get("jobid") == null) {
      job.getState().setState(State.ERROR);
      job.getState().setStartTime(System.currentTimeMillis());
      job.getState().setErrorMessage(flinkResponse.get("error").getAsString());
      job.getState().setHost(getHost());
      job.getState().setPort(getPort());
    } else {
      job.getState().setState(State.RUNNING);
      job.getState().setStartTime(System.currentTimeMillis());
      job.getState().setEngineId(flinkResponse.get("jobid").getAsString());
      job.getState().setHost(getHost());
      job.getState().setPort(getPort());
    }

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
    JsonElement flinkResponse = null;
    try {
      flinkResponse = this.httpClient.delete("/jobs/" + job.getState().getEngineId() + "/cancel");
      LOGGER.debug("/jobs/{}/cancel response: {}", job.getState().getEngineId(), flinkResponse);
    } catch (Exception e) {
      throw new Exception("Failed to get response from flink.", e);
    }

    // Result on success is {} (According to flink documentation)
    job.getState().setState(State.STOPPED);
    return job;
  }

  @Override
  public Job delete(Job job) throws Exception {
    // TODO: delete?
    return job;
  }

  @Override
  public ArrayList<JobState> getMetrics() throws Exception {
    JsonElement element = this.httpClient.get("/joboverview");

    FlinkJobOverview overview = new Gson().fromJson(element.toString(), FlinkJobOverview.class);

    ArrayList<JobState> jobStates = new ArrayList<>();

    // Running Job State * TBD
//    for (FlinkJob flinkJob : overview.getFinished()) {
//      JobState jobState = new JobState();
//      jobStates.add(jobState);
//    }

    // Finished Job State, any reasons.
    for (FlinkJob flinkJob : overview.getFinished()) {

      JobState jobState = new JobState();
      jobState.setEngineId(flinkJob.getJid());
      jobState.setFinishTime(flinkJob.getEndtime());
      if (0 == flinkJob.getState().compareTo("FAILED")) {

        jobState.setState(State.ERROR.name());

        JsonElement jsonElement = this.httpClient.get("/jobs/" + flinkJob.getJid() + "/exceptions");
        FlinkException flinkException = new Gson().fromJson(jsonElement.toString(), FlinkException.class);
        jobState.setErrorMessage(flinkException.getRootException());
      } else {
        jobState.setState(State.STOPPED.name());
      }

      jobStates.add(jobState);
    }

    return jobStates;
  }

  @Override
  public boolean updateMetrics(JobState jobState) throws Exception {

    boolean isUpdated = false;

    JsonElement element = this.httpClient.get("/jobs/" + jobState.getEngineId());
    FlinkJob flinkJob = new Gson().fromJson(element.toString(), FlinkJob.class);

    jobState.setFinishTime(flinkJob.getEndtime());
    if (0 == flinkJob.getState().compareTo("FAILED")) {
      if(jobState.getState().name().compareTo(State.ERROR.name()) != 0) {
        jobState.setState(State.ERROR.name());
        isUpdated = true;
      }

      JsonElement jsonElement = this.httpClient.get("/jobs/" + flinkJob.getJid() + "/exceptions");
      FlinkException flinkException = new Gson().fromJson(jsonElement.toString(), FlinkException.class);
      jobState.setErrorMessage(flinkException.getRootException());
    } else {
      if(0 == flinkJob.getState().compareTo("CANCELED")) {
        jobState.setState(State.STOPPED);
      } else {
        jobState.setState(flinkJob.getState());
      }
    }

    return isUpdated;
  }

  private String uploadLauncherJar(String path) {
    File jarFile = new File(path);
    JsonElement jsonString = this.httpClient.post("/jars/upload", jarFile);
    if (jsonString == null) {
      return null;
    }
    JsonObject jsonResponse = jsonString.getAsJsonObject();
    String jarId = jsonResponse.get("filename").getAsString(); // TODO: Exception handling
    return jarId;
  }

  @Override
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
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
