/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDetailed extends Format {

  private Workflow workflow;
  private RunningStatus running;
  public WorkflowDetailed() {

  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    if (workflow == null) {
      throw new RuntimeException("Invalid workflow");
    }
    this.workflow = workflow;
  }

  public RunningStatus getRunning() {
    return running;
  }

  public void setRunning(RunningStatus running) {
    if (running == null) {
      throw new RuntimeException("Invalid running status");
    }
    this.running = running;
  }

  public enum RunningStatus {
    RUNNING, NOT_RUNNING, UNKNOWN
  }
}
