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
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkEngine extends AbstractEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlinkEngine.class);
  private static final String DEFAULT_JOB_JAR_LOCATION = "/runtime/resource/";

  private static final String DEFAULT_LAUNCHER_JAR_LOCATION = DEFAULT_JOB_JAR_LOCATION
      + "engine-flink.jar";

  private HTTP httpClient = null;
  private String launcherJarId = null;

  public FlinkEngine(String flinkHost, int flinkPort) {
    this.httpClient = new HTTP();
    this.httpClient.initialize(flinkHost, flinkPort, "http");
  }

  @Override
  public JobResponseFormat createJob() {
    return new JobResponseFormat();
  }

  @Override
  public JobResponseFormat createJob(String jobId) {

    LOGGER.info("Flink job {} is created", jobId);

    return createJob().setJobId(jobId);
  }

  @Override
  public String createJob(TopologyData topologyData) {
    List<Path> jobSpecificData = new ArrayList<>();
    jobSpecificData.addAll(getModelInfo(topologyData));
    jobSpecificData.add(prepareFlinkJobPlan(topologyData));

    String topologyName = topologyData.getTopologyName();
    String jobJarFile = null;
    try {
      jobJarFile = prepareJarToDeploy(jobSpecificData, topologyName);
    } catch (Exception e) {
      LOGGER.debug(e.getMessage());
    }

    if (this.launcherJarId == null) {
      String launcherJarId = uploadLauncherJar(jobJarFile);
      if (launcherJarId == null) {
        LOGGER.error("Failed to upload Flink jar; Please check out connection");
      }
      this.launcherJarId = launcherJarId;
    }

    return topologyData.getTopologyName();
  }

  private Path prepareFlinkJobPlan(TopologyData topologyData) {
    String jsonConfig = new Gson().toJson(topologyData);
    String targetPath = DEFAULT_JOB_JAR_LOCATION + topologyData.getTopologyName() + ".json";
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

  private Set<Path> getModelInfo(TopologyData topologyData) {
    Set<Path> artifacts = new HashSet<>();
    try {
      for (TopologyProcessor processor : topologyData.getProcessors()) {
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
      LOGGER.error("Adding job-specific data to jar is failed - exit code: {} / output: {}",
          shellProcessResult.exitValue, shellProcessResult.stdout);
      throw new RuntimeException("Topology could not be deployed " +
          "successfully: fail to add config and artifacts to jar");
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

  private ShellProcessResult waitProcessFor(Process process)
      throws IOException, InterruptedException {
    StringWriter sw = new StringWriter();
    IOUtils.copy(process.getInputStream(), sw);
    String stdout = sw.toString();
    process.waitFor();
    int exitValue = process.exitValue();
    System.out.println("Command output: " + stdout);
    System.out.println("Command exit status: " + exitValue);
    return new ShellProcessResult(exitValue, stdout);
  }

  private static class ShellProcessResult {

    private final int exitValue;
    private final String stdout;

    ShellProcessResult(int exitValue, String stdout) {
      this.exitValue = exitValue;
      this.stdout = stdout;
    }

  }

  @Override
  public JobResponseFormat deploy(String jobId) {
    JobResponseFormat responseFormat = new JobResponseFormat();
    String flinkJobId = null;
    try {
      Map<String, String> args = new HashMap<>();
      args.put("program-args", String.format("--json %s", jobId));
      args.put("entry-class",
          "org.edgexfoundry.support.dataprocessing.runtime.engine.flink.Launcher");
      args.put("parallelism", "1");

      LOGGER.info("Running job {}({})", new Object[]{jobId, this.launcherJarId});

      JsonElement jsonElem = this.httpClient
          .post("/jars/" + this.launcherJarId + "/run", args, true);
      JsonObject jsonResponse = jsonElem.getAsJsonObject();
      LOGGER.debug("/run response: {}", jsonResponse);

      if (jsonResponse.get("jobid") == null) {
        responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
            jsonResponse.get("error").getAsString()));
        return responseFormat;
      }

      flinkJobId = jsonResponse.get("jobid").getAsString(); // TODO: Exception handling

      // Update database with flink's job Id

      JobTableManager.getInstance().updateEngineId(jobId, flinkJobId);
      LOGGER.info("FlinkEngine Job Id for {}: {}", jobId, flinkJobId);
    } catch (Exception e) {
      responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
          e.getMessage()));
    } finally {
      // Make response
      responseFormat.setJobId(flinkJobId);
      return responseFormat;
    }
  }

  @Override
  public JobResponseFormat run(String jobId) {
    String host = null;
    List<Map<String, String>> result = null;
    try {
      result = JobTableManager.getInstance().getPayloadById(jobId);

      if (!result.isEmpty()) {
        host = result.get(0).get(JobTableManager.Entry.runtimeHost.name());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return run(jobId, host);
  }

  public JobResponseFormat run(String jobId, String runtimeHost) {
    JobResponseFormat responseFormat = new JobResponseFormat();
    try {
      // Check if launcher jar for flink is uploaded
      if (this.launcherJarId == null) {
        String launcherJarId = uploadLauncherJar(DEFAULT_LAUNCHER_JAR_LOCATION);
        if (launcherJarId == null) {
          responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
              "Fail to upload launcher Jar"));
          return responseFormat;
        }
        this.launcherJarId = launcherJarId;
      }

      if (null == runtimeHost) {
        runtimeHost = "127.0.0.1:8082";
      }

      Map<String, String> args = new HashMap<>();
      args.put("program-args", String.format("--jobId %s --host %s", jobId, runtimeHost));
      args.put("entry-class",
          "org.edgexfoundry.support.dataprocessing.runtime.engine.flink.Launcher");
      args.put("parallelism", "1");

      LOGGER.info("Running job {}({})", new Object[]{jobId, this.launcherJarId});

      JsonElement jsonElem = this.httpClient
          .post("/jars/" + this.launcherJarId + "/run", args, true);
      JsonObject jsonResponse = jsonElem.getAsJsonObject();
      LOGGER.debug("/run response: {}", jsonResponse);

      if (jsonResponse.get("jobid") == null) {
        responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
            jsonResponse.get("error").getAsString()));
        return responseFormat;
      }

      String flinkJobId = jsonResponse.get("jobid").getAsString(); // TODO: Exception handling

      // Update database with flink's job Id

      JobTableManager.getInstance().updateEngineId(jobId, flinkJobId);
      LOGGER.info("FlinkEngine Job Id for {}: {}", jobId, flinkJobId);
    } catch (Exception e) {
      responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
          e.getMessage()));
    } finally {
      // Make response
      responseFormat.setJobId(jobId);
      return responseFormat;
    }
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
  public JobResponseFormat stop(String flinkJobId) {
    try {
      //List<Map<String, String>> result = JobTableManager.getInstance().getEngineIdById(jobId);
      if (flinkJobId != null) {
        this.httpClient.delete("/jobs/" + flinkJobId + "/cancel"); // TODO: Exception handling

        //JobTableManager.getInstance().updateEngineId(jobId, null);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Make response
      JobResponseFormat responseFormat = new JobResponseFormat();
      responseFormat.setJobId(flinkJobId);
      return responseFormat;
    }
  }

  @Override
  public JobResponseFormat delete(String jobId) {
    // Make response
    JobResponseFormat jobResponseFormat = new JobResponseFormat();
    jobResponseFormat.setJobId(jobId);
    return jobResponseFormat;
  }
}
