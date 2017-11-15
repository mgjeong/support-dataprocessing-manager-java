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
package org.edgexfoundry.support.dataprocessing.runtime.data.model.response;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "JobGroupResponse", description = "JobGroupResponse")
public class JobGroupResponseFormat extends Format {
    @ApiModelProperty(required = true)
    private ErrorFormat error;
    @ApiModelProperty(required = true)
    private List<JobGroupFormat> jobGroups;

    public JobGroupResponseFormat() {
        this(new ArrayList<JobGroupFormat>());
    }

    public JobGroupResponseFormat(List<JobGroupFormat> jobGroups) {
        this(new ErrorFormat(), new ArrayList<JobGroupFormat>());
    }

    public JobGroupResponseFormat(ErrorFormat error, List<JobGroupFormat> jobGroups) {
        setError(error);
        setJobGroups(jobGroups);
    }

    public void addJobGroup(JobGroupFormat jobGroup) {
        if (jobGroup != null) {
            this.jobGroups.add(jobGroup);
        }
    }

    public ErrorFormat getError() {
        return error;
    }

    public void setError(ErrorFormat error) {
        this.error = error;
    }

    public List<JobGroupFormat> getJobGroups() {
        return this.jobGroups;
    }

    public void setJobGroups(List<JobGroupFormat> jobGroups) {
        this.jobGroups = jobGroups;
    }
}
