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
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
  private StreamExecutionEnvironment env;

  public static void main(String[] args) throws Exception {
    Launcher launcher = new Launcher();
    launcher.execute(args);
  }

  private void execute(String[] args) throws Exception {
    ParameterTool params = ParameterTool.fromArgs(args);

    env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.getConfig().setGlobalJobParameters(params);

    if (!params.has("json")) {
      throw new RuntimeException("Not any specified job-config file");
    }

    String jsonId = params.get("json");
    Reader jsonReader = null;
    try {
      if (params.has("internal")) {
        String inJarPath = "/" + jsonId + ".json";
        LOGGER.debug("{}", getClass().getResource(inJarPath));
        jsonReader = new InputStreamReader(getClass().getResourceAsStream(inJarPath));

      } else {
        jsonReader = new FileReader(jsonId);
      }
    } catch (NullPointerException e) {
      throw new RuntimeException("Invalid job configuration file", e);
    }

    WorkflowData workflowData = new Gson().fromJson(jsonReader, WorkflowData.class);
    jsonReader.close();

    JobGraph jobGraph = new JobGraphBuilder().getInstance(env, workflowData);
    jobGraph.initialize();

    env.execute(jobGraph.getJobId());
  }
}
