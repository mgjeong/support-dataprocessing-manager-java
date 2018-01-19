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

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraph;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

  private void execute(String[] args) throws Exception {
    final ParameterTool params = ParameterTool.fromArgs(args);

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.getConfig().setGlobalJobParameters(params);

    if (!params.has("json")) {
      throw new RuntimeException("No specified job-config file");
    }

    String jsonString = params.get("json");
    Reader jsonReader;
    if (params.has("internal")) {
      String jsonFileName = "/" + jsonString + ".json";
      jsonReader = new InputStreamReader(getClass().getResourceAsStream(jsonFileName));
    } else {
      jsonReader = new FileReader(jsonString);
    }

    JobGraph jobGraph = new JobGraphBuilder().getInstance(env, jsonReader);
    jsonReader.close();

    jobGraph.initialize();
    env.execute(jobGraph.getJobId());
  }

  public static void main(String[] args) throws Exception {
    Launcher launcher = new Launcher();
    launcher.execute(args);
  }
}
