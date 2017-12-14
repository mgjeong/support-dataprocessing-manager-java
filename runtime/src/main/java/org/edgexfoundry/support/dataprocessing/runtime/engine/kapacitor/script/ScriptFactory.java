package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.DataFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptFactory.class);
    private static JobTableManager jobTableManager = null;
    private List<Map<String, String>> requestInfo = null;

    public ScriptFactory(String jobId) throws Exception {
        this.jobTableManager = JobTableManager.getInstance();
        this.requestInfo = this.jobTableManager.getPayloadById(jobId);
    }

    public String getScript() throws IOException {
        Map<String, String> job = requestInfo.get(0); // TO-DO: check if exceptions are possible
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
        for (DataFormat output : outputs) {
            scriptTail += generateScriptTail(output);
        }

        // This "query checking" logic should apply to task.getType() later
        // Also this current logic will check name is custom or not

        String scriptBody = "";
        for (TaskFormat task : tasks) {
            if (!task.getName().equals("query")) {
                throw new RuntimeException("For now, only one query type task is supported");
            }

            Object scriptBodyObject = task.getParams().get("request");
            if (scriptBodyObject instanceof String) {
                scriptBody += (String) scriptBodyObject + "\n";
            } else {
                throw new RuntimeException("Request should be String type");
            }
        }

        String script = "";
        for (String header : headers.keySet()) {
            script += headers.get(header) + '\n';
        }

        if (scriptBody != null) {
            script += scriptBody.replace("<", "\'").replace(">", "\'") + '\n';
        }

        script += scriptTail;
        return script;
    }

    private Map<String, String> generateScriptHeaders(DataFormat source) {
        String dataType = source.getDataType();
        String dataSource = source.getDataSource().replaceAll("\\s", "");

        if (!dataType.equalsIgnoreCase("EMF")) {
            throw new RuntimeException("Unsupported input data type; " + dataType);
        }
        if (dataSource == null) {
            throw new RuntimeException("Invalid data source; " + dataSource);
        }

        String[] sourceSplits = dataSource.split(":", 3);
        String[] topics = null;
        if (sourceSplits.length == 3) {
            topics = sourceSplits[2].split(",");
        }

        String sourceAddress = sourceSplits[0] + ':' + sourceSplits[1];
        Map<String, String> scriptHeaders = new HashMap<>();

        for (String topic : topics) {
            String key = topic;
            scriptHeaders.put(key, generateScriptHeaderByTopic(topic, sourceAddress, topic));
        }

        if (topics == null) {
            scriptHeaders.put(sourceAddress, generateScriptHeaderByTopic(sourceAddress, sourceAddress, null));
        }

        return scriptHeaders;
    }

    private String generateScriptHeaderByTopic(String table, String sourceAddress, String topic) {
        String validName = table.replaceAll("\\W", "");
        String measurement = String.format("var %s = stream|from().measurement(\'%s\')", validName, validName);
        String injection = String.format("@inject().source('emf').address(\'%s\')", sourceAddress);

        if (topic == null) {
            return measurement + injection;
        }

        String subscription = String.format(".topic(\'%s\')", topic);
        return measurement + injection + subscription;
    }

    private String generateScriptTail(DataFormat output) {
        String dataType = output.getDataType();
        String dataSink = output.getDataSource().replaceAll("\\s", "");

        if (!dataType.equalsIgnoreCase("EMF") && !dataType.equalsIgnoreCase("F")) {
            throw new RuntimeException("Unsupported output data type" + dataType);
        }
        String[] sinkSplits = dataSink.split(":", 3);
        String[] topics = null;
        if (sinkSplits.length == 3) {
            topics = sinkSplits[2].split(",");
        }

        String oneScriptTail = String.format("@deliver().sink(\'%s\').address(\'%s\')", dataType, dataSink);
        if (topics == null) {
            return oneScriptTail;
        } else {
            String scriptTail = "";
            for (String topic: topics) {
                scriptTail += generateScriptTailByTopic(dataType, dataSink, topic);
            }
            return scriptTail;
        }
    }

    private String generateScriptTailByTopic(String dataType, String dataSink, String topic) {
        String scriptTail = String.format("@deliver().sink(\'%s\').address(\'%s\')", dataType, dataSink);
        scriptTail += String.format(".topic(\'%s\')", topic) + '\n';
        return scriptTail;
    }
}
