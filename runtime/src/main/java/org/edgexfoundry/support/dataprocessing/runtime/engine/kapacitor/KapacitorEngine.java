package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KapacitorEngine extends AbstractEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(KapacitorEngine.class);
    private static final String TASK_ROOT = "/kapacitor/v1/tasks";
    private static JobTableManager jobTableManager = null;
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
    public JobResponseFormat run(String jobId) {
        JsonObject taskInfo = new JsonObject();
        taskInfo.addProperty("id", jobId);
        taskInfo.addProperty("type", "stream");
        taskInfo.addProperty("status", "disabled");
        JsonArray dbrps = new JsonArray();
        JsonObject dbrp = new JsonObject();
        dbrp.addProperty("db", "telegraf3");
        dbrp.addProperty("rp", "autogen");
        dbrps.add(dbrp);
        taskInfo.add("dbrps", dbrps);

        // read job information
        try {
            if (this.jobTableManager == null) {
                this.jobTableManager = JobTableManager.getInstance();
            }

            List<Map<String, String>> payload = this.jobTableManager.getPayloadById(jobId);
            if (payload.isEmpty()) {
                throw new RuntimeException("Job payload not found. JobId = " + jobId);
            }

            Map<String, String> job = payload.get(0); // TO-DO: check if exceptions are possible
            // read source db and table
            ObjectMapper mapper = new ObjectMapper();
            List<DataFormat> inputs = mapper.readValue(job.get(JobTableManager.Entry.input.name()),
                    new TypeReference<List<DataFormat>>() {
                    });
            List<DataFormat> outputs = mapper.readValue(job.get(JobTableManager.Entry.output.name()),
                    new TypeReference<List<DataFormat>>() {
                    });
            List<TaskFormat> tasks = mapper.readValue(job.get(JobTableManager.Entry.taskinfo.name()),
                    new TypeReference<List<TaskFormat>>() {
                    });

            // Setting script
            Map<String, String> headers = new HashMap<>();
            for (DataFormat input : inputs) {
                headers.putAll(generateScriptHeaders(input));
            }

            String scriptTail = "";
            for (DataFormat output: outputs) {
                scriptTail += generateScriptTail(output);
            }

            // This "query checking" logic should apply to task.getType() later
            // Also this current logic will check name is custom or not
            TaskFormat task = tasks.get(0);
            if (!task.getName().equals("query")) {
                throw new RuntimeException("For now, only one query type task is supported");
            }

            String scriptBody = task.getScript().replace("<", "\'").replace(">", "\'");

            LOGGER.info("Building Kapacitor jobId: {}", jobId);

            String scriptHeader = "";
            for (String header: headers.keySet()) {
                scriptHeader += headers.get(header) + '\n';
            }

            String script = scriptHeader + '|' + scriptBody + scriptTail;

            LOGGER.info("Kapacitor script is following: {}", script);
            taskInfo.addProperty("script", script);
            taskInfo.addProperty("status", "enabled");

            // Post defined task to kapacitor
            this.httpClient.post(TASK_ROOT, taskInfo.toString());
            LOGGER.info("Kapacitor Job Id {} is registered.", jobId);

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve JobInfoFormat: " + e.getMessage(), e);
        }

        String path = TASK_ROOT + '/' + jobId;
        String flag = "{\"status\":\"enabled\"}";

        try {
            this.httpClient.patch(path, flag);
            // TO-DO: Exception handling
            LOGGER.info("Job {} is now running", jobId);
            JobTableManager.getInstance().updateEngineId(jobId, null);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage());
        } finally {
            JobResponseFormat responseFormat = new JobResponseFormat();
            responseFormat.setJobId(jobId);
            return responseFormat;
        }

    }

    private Map<String, String> generateScriptHeaders(DataFormat source) {
        String dataType = source.getDataType();
        String dataSource = source.getDataSource();
        String[] topics = null;
        if (source.getTopics() != null) {
            topics = source.getTopics().replaceAll("\\s", "").split(",");
        }
        if (!dataType.equalsIgnoreCase("EMF")) {
            throw new RuntimeException("Unsupported input data type" + dataType);
        }

        Map<String, String> scriptHeaders = new HashMap<>();
        if (topics == null) {
            String key = dataSource;
            scriptHeaders.put(key, generateScriptHeaderByTopic(dataSource, null));
        } else {
            for (String topic : topics) {
                String key = dataSource + topic;
                scriptHeaders.put(key, generateScriptHeaderByTopic(dataSource, topic));
            }
        }

        return scriptHeaders;
    }

    private String generateScriptHeaderByTopic(String dataSource, String topic) {
        String table;
        String injection = String.format("@inject().source('emf').address(\'%s\')", dataSource);
        if (topic == null) {
            table = String.format("stream|from().measurement(\'%s\')", dataSource);
            return table + injection;
        } else {
            table = String.format("stream|from().measurement(\'%s\')", dataSource + topic);
            return table + injection + String.format(".topic(\'%s\')", topic);
        }
    }

    private String generateScriptTail(DataFormat output) {
        String dataType = output.getDataType();
        String dataSink = output.getDataSource();
        String topics = output.getTopics();

        if (!dataType.equalsIgnoreCase("EMF") && !dataType.equalsIgnoreCase("F")) {
            throw new RuntimeException("Unsupported output data type" + dataType);
        }

        String scriptTail = String.format("@deliver().sink(\'%s\').address(\'%s\')", dataType, dataSink);
        if (topics == null) {
            return scriptTail;
        } else {
            return scriptTail + String.format(".topics(\'%s\')", topics);
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