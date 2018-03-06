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
public class WorkflowEditorMetadata extends Format {

  private Long workflowId;
  private String data;
  private Long timestamp;

  public WorkflowEditorMetadata() {

  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Invalid workflow id");
    }
    this.workflowId = workflowId;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
