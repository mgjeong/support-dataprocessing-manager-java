package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.ScriptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KapacitorEngine extends AbstractEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(KapacitorEngine.class);
    private static final String TASK_ROOT = "/kapacitor/v1/tasks";
    private HTTP httpClient = null;

    public KapacitorEngine(String hostname, int port) {
        this.httpClient = new HTTP();
        this.httpClient.initialize(hostname, port, "http");
    }

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
    public String createJob(TopologyData topology) {
        return null;
    }

    @Override
    public JobResponseFormat deploy(String topologyName) {
        return null;
    }

    @Override
    public JobResponseFormat run(String jobId) {
        JsonObject taskInfo = new JsonObject();
        taskInfo.addProperty("id", jobId);
        taskInfo.addProperty("type", "stream");
        taskInfo.addProperty("status", "disabled");
        JsonArray dbrps = new JsonArray();
        JsonObject dbrp = new JsonObject();

        // Custom database and retention policy is set for now
        dbrp.addProperty("db", "dpruntime");
        dbrp.addProperty("rp", "autogen");
        dbrps.add(dbrp);
        taskInfo.add("dbrps", dbrps);

        try {
            JobTableManager jobTableManager = JobTableManager.getInstance();
            Map<String, String> job = jobTableManager.getPayloadById(jobId).get(0);
            ObjectMapper mapper = new ObjectMapper();
            List<DataFormat> inputs =
                mapper.readValue(
                    job.get(JobTableManager.Entry.input.name()), new TypeReference<List<DataFormat>>() {});
            List<DataFormat> outputs =
                mapper.readValue(
                    job.get(JobTableManager.Entry.output.name()), new TypeReference<List<DataFormat>>() {});
            List<TaskFormat> tasks =
                mapper.readValue(
                    job.get(JobTableManager.Entry.taskinfo.name()),
                    new TypeReference<List<TaskFormat>>() {});
            ScriptFactory scriptFactory = new ScriptFactory();
            scriptFactory.setInputs(inputs);
            scriptFactory.setOutputs(outputs);
            scriptFactory.setTasks(tasks);
            String script = scriptFactory.getScript();

            LOGGER.info("Kapacitor script is following: {}", script);
            taskInfo.addProperty("script", script);
            taskInfo.addProperty("status", "disabled");

            // Post defined task to kapacitor
            this.httpClient.post(TASK_ROOT, taskInfo.toString());
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

}