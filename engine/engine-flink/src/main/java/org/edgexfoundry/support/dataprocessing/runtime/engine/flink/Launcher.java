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

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EZMQSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.ezmq.EZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraphBuilder;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.task.ModelLoader;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.operator.TaskFlatMap;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.schema.DataSetSchema;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.FileOutputSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.MongoDBSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.sink.WebSocketServerSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZMQSink;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.ZMQSource;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.zmq.common.ZMQConnectionConfig;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

  private static final String CONFIGPATH = "/config.json";

  private JobTableManager jobTableManager = null;

  private StreamExecutionEnvironment env = null;

  private HTTP httpClient = null;

  public Launcher() {

  }

  public void execute_tmp(String[] args) throws Exception {
    final ParameterTool params = ParameterTool.fromArgs(args);

    if (env == null) {
      env = StreamExecutionEnvironment.getExecutionEnvironment();
    }

    env.getConfig().setGlobalJobParameters(params);
    Reader jsonReader = new InputStreamReader(getClass().getResourceAsStream(CONFIGPATH));
    JobGraph jobGraph = new JobGraphBuilder().getInstance(env, jsonReader).initialize();

    jsonReader.close();
    jobGraph.execute();
  }

  public void execute(String[] args) throws Exception {
    // Input parameters
    final ParameterTool params = ParameterTool.fromArgs(args);
    LOGGER.info("Parameters loaded from arguments.");

    // Set up the execution environment
    if (env == null) {
      env = StreamExecutionEnvironment.getExecutionEnvironment();
    }

    // Make parameters available in the web interface
    env.getConfig().setGlobalJobParameters(params);

    // Get user request
    JobInfoFormat request = getJobInfoFormat(params);
    LOGGER.info("User request retrieved.");

    // Input
    DataStream<DataSet> stream = null;
    DataStream<DataSet> subStream;
    for (DataFormat inputData : request.getInput()) {
      LOGGER.info("Adding source: {}", inputData.getDataType());
      subStream = addSource(env, inputData);
      if (stream == null) {
        stream = subStream;
      } else {
        stream = stream.union(subStream);
      }
    }

    // Tasks
    for (TaskFormat task : request.getTask()) {
      LOGGER.info("Adding task: {} - {}", task.getName(), task.getType());
      stream = stream.flatMap(new TaskFlatMap(task, params.get("host")));
    }

    // Output
    for (DataFormat outputData : request.getOutput()) {
      LOGGER.info("Adding sink: {}", outputData.getDataType());
      addSink(stream, outputData);
    }

    // Execute
    String jobName = makeJobName(request);
    LOGGER.info("Executing job: {}", jobName);
    env.execute(jobName);
  }

  private String makeJobName(JobInfoFormat request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getInput().get(0).getDataType()).append("-");
    for (TaskFormat algorithm : request.getTask()) {
      sb.append(algorithm.getName()).append("-");
    }
    sb.append(request.getOutput().get(0).getDataType());
    return sb.toString();
  }

  private DataStreamSink<DataSet> addSink(DataStream<DataSet> stream, DataFormat output) {
    String dataType = output.getDataType().toLowerCase();
    if (dataType.equals("zmq")) {
      String[] dataSource = output.getDataSource().split(":");
      ZMQConnectionConfig zmqConnectionConfig = new ZMQConnectionConfig.Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIOThreads(1)
          .build();

      return stream.addSink(new ZMQSink<>(zmqConnectionConfig, dataSource[2], new DataSetSchema()))
          .setParallelism(1);
    } else if (dataType.equals("ws")) {
      String[] dataSource = output.getDataSource().split(":");
      return stream.addSink(new WebSocketServerSink(Integer.parseInt(dataSource[1])))
          .setParallelism(1);
    } else if (dataType.equals("ezmq")) {
      String[] dataSource = output.getDataSource().split(":");
      // String host = dataSource[0].trim(); // unused
      int port = Integer.parseInt(dataSource[1].trim());
      return stream.addSink(new EZMQSink(port)).setParallelism(1);
    } else if (dataType.equals("f")) {
      String outputFilePath = output.getDataSource();
      if (!outputFilePath.endsWith(".txt")) {
        outputFilePath += ".txt";
      }
      return stream.addSink(new FileOutputSink(outputFilePath));
    } else if (dataType.equals("mongodb")) {
      return stream.addSink(new MongoDBSink(output.getDataSource(), output.getName()))
          .setParallelism(1);
    } else {
      throw new RuntimeException("Unsupported output data type: " + dataType);
    }
  }

  private DataStream<DataSet> addSource(StreamExecutionEnvironment env, DataFormat input) {
    String dataType = input.getDataType().toLowerCase();
    if (dataType.equals("zmq")) {
      String[] dataSource = input.getDataSource().split(":");
      ZMQConnectionConfig zmqConnectionConfig = new ZMQConnectionConfig.Builder()
          .setHost(dataSource[0].trim())
          .setPort(Integer.parseInt(dataSource[1].trim()))
          .setIOThreads(1)
          .build();

      return env.addSource(new ZMQSource<>(zmqConnectionConfig,
          dataSource[2], new DataSetSchema())).setParallelism(1);
    } else if (dataType.equals("ezmq")) {
      String[] dataSource = input.getDataSource().split(":", 3);
      String host = dataSource[0].trim();
      int port = Integer.parseInt(dataSource[1].trim());
      if (dataSource.length == 3) {
        String topic = dataSource[2].trim();
        return env.addSource(new EZMQSource(host, port, topic)).setParallelism(1);
      } else {
        return env.addSource(new EZMQSource(host, port)).setParallelism(1);
      }
    } else {
      throw new RuntimeException("Unsupported input data type: " + dataType);
    }
  }


  private JobInfoFormat getJobInfo(String jobId) {

    if (null != httpClient) {
      try {
        String rawJson = httpClient.get("/analytics/v1/job/info/" + jobId).toString();

        JobInfoFormat jobInfo = new Gson().fromJson(rawJson, JobInfoFormat.class);
        return jobInfo;
      } catch (NullPointerException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    return null;
  }

  private JobInfoFormat getJobInfoFormat(ParameterTool params) throws Exception {
    if (!params.has("jobId")) {
      throw new RuntimeException("--jobId value is required.");
    }

    try {
      String jobId = params.get("jobId");
      LOGGER.info("JobID passed: " + jobId);
      String host = params.get("host");
      LOGGER.info("Host passed: " + host);
      if (null == host) {
        host = "localhost:8082";
      }

      httpClient = new HTTP().initialize(host, "http");

      return getJobInfo(jobId);
    } catch (Exception e) {
      LOGGER.error("Failed to retrieve JobInfoFormat: " + e.getMessage(), e);
      throw e;
    }
  }

  public static void main(String[] args) throws Exception {
    Launcher launcher = new Launcher();
    launcher.execute_tmp(args);
  }
}
