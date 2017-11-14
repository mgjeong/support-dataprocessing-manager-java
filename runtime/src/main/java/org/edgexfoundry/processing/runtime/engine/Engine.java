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

package org.edgexfoundry.processing.runtime.engine;

import org.edgexfoundry.processing.runtime.data.model.response.JobResponseFormat;

public interface Engine {
    JobResponseFormat createJob();

    JobResponseFormat createJob(String jobId);

    JobResponseFormat run(String jobId);

    JobResponseFormat stop(String jobId);

    JobResponseFormat delete(String jobId);
}
