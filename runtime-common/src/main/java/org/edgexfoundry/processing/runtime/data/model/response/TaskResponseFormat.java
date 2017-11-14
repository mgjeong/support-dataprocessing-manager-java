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
package com.sec.processing.framework.data.model.response;

import com.sec.processing.framework.data.model.error.ErrorFormat;
import com.sec.processing.framework.data.model.Format;
import com.sec.processing.framework.data.model.task.TaskFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "TaskResponse", description = "TaskResponse")
public class TaskResponseFormat extends Format {
    @ApiModelProperty(required = true)
    private ErrorFormat error;
    @ApiModelProperty(required = true)
    private List<TaskFormat> task;

    public TaskResponseFormat() {
        this(new ErrorFormat(), new ArrayList<TaskFormat>());
    }

    public TaskResponseFormat(List<TaskFormat> task) {
        this(new ErrorFormat(), task);
    }

    public TaskResponseFormat(ErrorFormat error, List<TaskFormat> task) {
        setTask(task);
        setError(error);
    }

    public void addAlgorithmFormat(TaskFormat algorithm) {
        if (algorithm != null) {
            this.task.add(algorithm);
        }
    }

    public ErrorFormat getError() {
        return error;
    }

    public void setError(ErrorFormat error) {
        this.error = error;
    }

    public List<TaskFormat> getTask() {
        return task;
    }

    public void setTask(List<TaskFormat> task) {
        this.task = task;
    }
}
