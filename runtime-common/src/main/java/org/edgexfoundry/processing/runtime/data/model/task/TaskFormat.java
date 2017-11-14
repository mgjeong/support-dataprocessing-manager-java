package org.edgexfoundry.processing.runtime.data.model.task;

import org.edgexfoundry.processing.runtime.data.model.Format;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.edgexfoundry.processing.runtime.task.TaskType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Process", description = "Process")
public class TaskFormat extends Format {
    @ApiModelProperty(required = false)
    private TaskType type;
    @ApiModelProperty(required = true)
    private String name;
    @ApiModelProperty(required = true, dataType = "json")
    private TaskModelParam params;
    @ApiModelProperty(required = true)
    private List<String> inrecord;
    @ApiModelProperty(required = true)
    private List<String> outrecord;

    public TaskFormat() {
        this(TaskType.INVALID, null, (TaskModelParam) null);
    }

    public TaskFormat(TaskFormat task) {
        this(task.getType(), task.getName(), task.getParams());
    }

    public TaskFormat(TaskType type, String name, String params) {
        this(type, name, TaskModelParam.create(params));
    }

    public TaskFormat(TaskType type, String name, TaskModelParam params) {
        setType(type);
        setName(name);
        setParams(params);
        this.inrecord = new ArrayList<>();
        this.outrecord = new ArrayList<>();
    }

    public TaskModelParam getParams() {
        return params;
    }

    public void setParams(TaskModelParam params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public List<String> getInrecord() {
        return inrecord;
    }

    public void setInrecord(List<String> inrecord) {
        this.inrecord = inrecord;
    }

    public List<String> getOutrecord() {
        return outrecord;
    }

    public void setOutrecord(List<String> outrecord) {
        this.outrecord = outrecord;
    }

}
