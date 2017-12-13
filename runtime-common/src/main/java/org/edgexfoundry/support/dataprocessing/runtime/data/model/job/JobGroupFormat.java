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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JobGroup", description = "JobGroup")
public class JobGroupFormat extends Format {

    @ApiModelProperty(required = false)
    private String groupId;

    @ApiModelProperty(required = false)
    private String runtimeHost = null;

    @ApiModelProperty(required = true)
    private List<JobInfoFormat> jobs;

    public JobGroupFormat() {
        this(new ArrayList<JobInfoFormat>());
    }

    public JobGroupFormat(ArrayList<JobInfoFormat> jobs) {
        setJobs(jobs);
    }

    public void addJob(JobInfoFormat job) {
        if (job != null) {
            this.jobs.add(job);
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<JobInfoFormat> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobInfoFormat> jobs) {
        this.jobs = jobs;
    }

    public String getRuntimeHost() {
        return this.runtimeHost;
    }
    public void setRuntimeHost(String runtimeHost) {
        this.runtimeHost = runtimeHost;
    }

}
