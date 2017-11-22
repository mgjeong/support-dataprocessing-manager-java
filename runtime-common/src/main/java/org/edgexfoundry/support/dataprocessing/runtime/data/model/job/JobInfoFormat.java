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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TaskFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JobInfo", description = "JobInfo")
public class JobInfoFormat extends Format {
    @ApiModelProperty(required = false)
    private String jobId = null;
    @ApiModelProperty(required = false)
    private JobState state;
    @ApiModelProperty(required = true)
    private String targetHost = null;
    @ApiModelProperty(required = true)
    private List<DataFormat> input = null;
    @ApiModelProperty(required = true)
    private List<DataFormat> output = null;
    @ApiModelProperty(required = true)
    private List<TaskFormat> task = null;
    @ApiModelProperty(required = false)
    private String engineType = null;


    public JobInfoFormat() {
        this(new ArrayList<DataFormat>(),
                new ArrayList<DataFormat>(),
                new ArrayList<TaskFormat>(),
                JobState.CREATE);
    }

    public JobInfoFormat(List<DataFormat> input, List<DataFormat> output, List<TaskFormat> task) {
        this(input, output, task, JobState.CREATE);
    }

    public JobInfoFormat(List<DataFormat> input, List<DataFormat> output, List<TaskFormat> task, JobState state) {
        setInput(input);
        setOutput(output);
        setTask(task);
        setState(state);
    }

    public void addTask(TaskFormat task) {
        if (task != null) {
            this.task.add(task);
        }
    }

    public void addInput(DataFormat input) {
        if (input != null) {
            this.input.add(input);
        }
    }

    public void addOutput(DataFormat output) {
        if (output != null) {
            this.output.add(output);
        }
    }

    public List<DataFormat> getInput() {
        return input;
    }

    public void setInput(List<DataFormat> input) {
        this.input = input;
    }

    public List<DataFormat> getOutput() {
        return output;
    }

    public void setOutput(List<DataFormat> output) {
        this.output = output;
    }

    public List<TaskFormat> getTask() {
        return task;
    }

    public void setTask(List<TaskFormat> task) {
        this.task = task;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTargetHost() {
        return this.targetHost;
    }

    public void setEngineType(String type) {
        this.engineType = type;
    }
    public String getEngineType() {
        return this.engineType;
    }

    public void setTargetHost(String targethost) {
        this.targetHost = targethost;
    }

    public void setPayload(JobInfoFormat job) {
       this.setInput(job.getInput());
       this.setOutput(job.getOutput());
       this.setTask(job.getTask());
    }
}
