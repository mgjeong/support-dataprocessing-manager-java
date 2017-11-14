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

package com.sec.processing.framework.engine.flink;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sec.processing.framework.data.model.job.DataFormat;
import com.sec.processing.framework.data.model.job.JobInfoFormat;
import com.sec.processing.framework.data.model.task.TaskFormat;
import com.sec.processing.framework.db.JobTableManager;
import com.sec.processing.framework.engine.flink.emf.EMFSink;
import com.sec.processing.framework.engine.flink.emf.EMFSource;
import com.sec.processing.framework.engine.flink.operator.TaskFlatMap;
import com.sec.processing.framework.engine.flink.schema.DataSetSchema;
import com.sec.processing.framework.engine.flink.sink.FileOutputSink;
import com.sec.processing.framework.engine.flink.sink.WebSocketServerSink;
import com.sec.processing.framework.engine.flink.zmq.ZMQSink;
import com.sec.processing.framework.engine.flink.zmq.ZMQSource;
import com.sec.processing.framework.engine.flink.zmq.common.ZMQConnectionConfig;
import org.edgexfoundry.processing.runtime.task.DataSet;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class Launcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private JobTableManager jobTableManager = null;

    private StreamExecutionEnvironment env = null;

    public Launcher() {

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
            stream = stream.flatMap(new TaskFlatMap(task));
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
        String dataType = output.getDataType();
        if (dataType.equals("ZMQ")) {
            String[] dataSource = output.getDataSource().split(":");
            ZMQConnectionConfig zmqConnectionConfig = new ZMQConnectionConfig.Builder()
                    .setHost(dataSource[0].trim())
                    .setPort(Integer.parseInt(dataSource[1].trim()))
                    .setIOThreads(1)
                    .build();

            return stream.addSink(new ZMQSink<>(zmqConnectionConfig, dataSource[2], new DataSetSchema()))
                    .setParallelism(1);
        } else if (dataType.equals("WS")) {
            String[] dataSource = output.getDataSource().split(":");
            return stream.addSink(new WebSocketServerSink(Integer.parseInt(dataSource[1])))
                    .setParallelism(1);
        } else if (dataType.equals("EMF")) {
            String[] dataSource = output.getDataSource().split(":");
            // String host = dataSource[0].trim(); // unused
            int port = Integer.parseInt(dataSource[1].trim());
            return stream.addSink(new EMFSink(port)).setParallelism(1);
        } else if (dataType.equals("F")) {
            String outputFilePath = output.getDataSource();
            if (!outputFilePath.endsWith(".txt")) {
                outputFilePath += ".txt";
            }
            return stream.addSink(new FileOutputSink(outputFilePath));
        } else {
            throw new RuntimeException("Unsupported output data type: " + dataType);
        }
    }

    private DataStream<DataSet> addSource(StreamExecutionEnvironment env, DataFormat input) {
        String dataType = input.getDataType();
        if (dataType.equals("ZMQ")) {
            String[] dataSource = input.getDataSource().split(":");
            ZMQConnectionConfig zmqConnectionConfig = new ZMQConnectionConfig.Builder()
                    .setHost(dataSource[0].trim())
                    .setPort(Integer.parseInt(dataSource[1].trim()))
                    .setIOThreads(1)
                    .build();

            return env.addSource(new ZMQSource<>(zmqConnectionConfig,
                    dataSource[2], new DataSetSchema())).setParallelism(1);
        } else if (dataType.equals("EMF")) {
            String[] dataSource = input.getDataSource().split(":");
            String host = dataSource[0].trim();
            int port = Integer.parseInt(dataSource[1].trim());
            return env.addSource(new EMFSource(host, port)).setParallelism(1);
        } else {
            throw new RuntimeException("Unsupported input data type: " + dataType);
        }
    }

    private JobInfoFormat getJobInfoFormat(ParameterTool params) throws Exception {
        if (!params.has("jobId")) {
            throw new RuntimeException("--jobId value is required.");
        }

        try {
            String jobId = params.get("jobId");
            LOGGER.info("JobID passed: " + jobId);

            if (this.jobTableManager == null) {
                this.jobTableManager = JobTableManager.getInstance();
            }

            List<Map<String, String>> payload = this.jobTableManager.getPayloadById(jobId);
            if (payload.isEmpty()) {
                throw new RuntimeException("Job payload not found. JobId = " + jobId);
            }
            Map<String, String> job = payload.get(0); // TODO: Exception handling

            ObjectMapper mapper = new ObjectMapper();

            List<DataFormat> input = mapper.readValue(job.get(JobTableManager.Entry.input.name()),
                    new TypeReference<List<DataFormat>>() {
                    });
            List<DataFormat> output = mapper.readValue(job.get(JobTableManager.Entry.output.name()),
                    new TypeReference<List<DataFormat>>() {
                    });
            List<TaskFormat> task = mapper.readValue(job.get(JobTableManager.Entry.taskinfo.name()),
                    new TypeReference<List<TaskFormat>>() {
                    });

            return new JobInfoFormat(input, output, task);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve JobInfoFormat: " + e.getMessage(), e);
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        launcher.execute(args);
    }
}
