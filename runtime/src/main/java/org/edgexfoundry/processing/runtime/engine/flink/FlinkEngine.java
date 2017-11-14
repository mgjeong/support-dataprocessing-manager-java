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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sec.processing.framework.connection.HTTP;
import com.sec.processing.framework.data.model.error.ErrorFormat;
import com.sec.processing.framework.data.model.error.ErrorType;
import com.sec.processing.framework.data.model.response.JobResponseFormat;
import com.sec.processing.framework.db.JobTableManager;
import com.sec.processing.framework.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlinkEngine extends AbstractEngine {
    private static final String DEFAULT_LAUNCHER_JAR_LOCATION = "./framework/resource/engine-flink.jar";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkEngine.class);
    private HTTP httpClient = null;
    private String launcherJarId = null;

    public FlinkEngine(String flinkHost, int flinkPort) {
        this.httpClient = new HTTP();
        this.httpClient.initialize(flinkHost, flinkPort, "http");
    }

    @Override
    public JobResponseFormat createJob() {
        return createJob(generateJobId());
    }

    @Override
    public JobResponseFormat createJob(String jobId) {
        // Make response
        JobResponseFormat response = new JobResponseFormat();
        response.setJobId(jobId);

        return response;
    }

    @Override
    public JobResponseFormat run(String jobId) {
        JobResponseFormat responseFormat = new JobResponseFormat();
        try {
            // Check if launcher jar for flink is uploaded
            if (this.launcherJarId == null) {
                String launcherJarId = uploadLauncherJar();
                if (launcherJarId == null) {
                    responseFormat.setError(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK,
                            "Fail to upload launcher Jar"));
                    return responseFormat;
                }
                this.launcherJarId = launcherJarId;
            }

            Map<String, String> args = new HashMap<>();
            args.put("program-args", String.format("--jobId %s", jobId));
            args.put("entry-class", "com.sec.processing.framework.engine.flink.Launcher");
            args.put("parallelism", "1");

            LOGGER.info("Running job {}({})", new Object[]{jobId, this.launcherJarId});

            JsonElement jsonElem = this.httpClient.post("/jars/" + this.launcherJarId + "/run", args, true);
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

    private String uploadLauncherJar() {
        File jarFile = new File(DEFAULT_LAUNCHER_JAR_LOCATION);
        JsonElement jsonString = this.httpClient.post("/jars/upload", jarFile);
        if (jsonString == null) {
            return null;
        }
        JsonObject jsonResponse = jsonString.getAsJsonObject();
        String jarId = jsonResponse.get("filename").getAsString(); // TODO: Exception handling
        return jarId;
    }

    @Override
    public JobResponseFormat stop(String jobId) {
        try {
            List<Map<String, String>> result = JobTableManager.getInstance().getEngineIdById(jobId);
            String flinkJobId = null;
            if (!result.isEmpty()) {
                flinkJobId = result.get(0).get(JobTableManager.Entry.engineid.name());
            }

            if (flinkJobId != null) {
                this.httpClient.delete("/jobs/" + flinkJobId + "/cancel"); // TODO: Exception handling

                JobTableManager.getInstance().updateEngineId(jobId, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Make response
            JobResponseFormat responseFormat = new JobResponseFormat();
            responseFormat.setJobId(jobId);
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
