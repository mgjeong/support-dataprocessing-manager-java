package org.edgexfoundry.processing.runtime.task.model;

import org.edgexfoundry.processing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.processing.runtime.task.DataSet;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.edgexfoundry.processing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WindowByCountModel extends AbstractTaskModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowByCountModel.class);

    private int countLimit;

    private DataSet cached = null;

    @Override
    public TaskType getType() {
        return TaskType.CUSTOM; // TODO: Change this
    }

    @Override
    public String getName() {
        return "WindowByCount"; // Change this if necessary
    }

    @Override
    public TaskModelParam getDefaultParam() {
        TaskModelParam param = new TaskModelParam();
        param.put("count", 3);
        return param;
    }

    @Override
    public void setParam(TaskModelParam param) {
        this.countLimit = (int) param.get("count");
        LOGGER.info("countLimit=" + this.countLimit);
    }

    @Override
    public DataSet calculate(DataSet in, List<String> inRecordKeys, List<String> outRecordKeys) {
        /*
        if (cached == null) {
            cached = in;
        } else {
            cached.merge(in);
        }

        if (cached.count() < this.countLimit) {
            // if not enough data is collected, return
            return null;
        } else {
            // What if cached.count() > this.countLimit ???
            StreamData rtn = cached;
            cached = null;
            return rtn;
        }
        */
        throw new UnsupportedOperationException("Could you implement this for me, please?");
    }
}
