package org.edgexfoundry.support.dataprocessing.runtime.task.model;

import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class QueryModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryModel.class);
    private String request = null;

    @Override
    public TaskType getType() {
        return TaskType.QUERY;
    }

    @Override
    public String getName() {
        return "query";
    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam param = new TaskModelParam();
        param.put("request", "TOPIC1|movingAverage(<BC>,5).as(<avg>)");
        return param;
    }

    @Override
    public void setParam(TaskModelParam params) {
        request = params.get("request").toString();
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        LOGGER.debug("[Query] query is not supported in this engine, and therefore, data will be bypassed");
        return in;
    }


}
