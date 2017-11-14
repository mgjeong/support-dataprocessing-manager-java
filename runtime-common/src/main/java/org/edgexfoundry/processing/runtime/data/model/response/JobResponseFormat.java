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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Response", description = "Response")
public class JobResponseFormat extends Format {
    @ApiModelProperty(required = true)
    private ErrorFormat error;
    @ApiModelProperty(required = true, example = "ef91c5b3-b5bb-4e8b-8145-e271ac16cefc")
    private String jobId;

    public JobResponseFormat() {
        this(new ErrorFormat(), null);
    }

    public JobResponseFormat(ErrorFormat error) {
        this(error, null);
    }

    public JobResponseFormat(String jobId) {
        this(new ErrorFormat(), jobId);
    }

    public JobResponseFormat(ErrorFormat error, String jobId) {
        setError(error);
        setJobId(jobId);
    }

    public ErrorFormat getError() {
        return error;
    }

    public void setError(ErrorFormat error) {
        this.error = error;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
