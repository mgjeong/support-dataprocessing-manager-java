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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public abstract class WorkflowOutputComponent extends WorkflowComponent {

  private List<WorkflowStream> outputStreams = new ArrayList<>();

  public WorkflowOutputComponent() {

  }

  public List<WorkflowStream> getOutputStreams() {
    return outputStreams;
  }

  public void setOutputStreams(List<WorkflowStream> outputStreams) {
    if (outputStreams == null) {
      throw new RuntimeException("Invalid output streams");
    }
    this.outputStreams = outputStreams;
  }

  @JsonIgnore
  public void addOutputStream(WorkflowStream stream) {
    if (stream == null) {
      throw new RuntimeException("Invalid stream");
    }
    outputStreams.add(stream);
  }
}
