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

import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

public class WorkflowMetric extends Format {

  private List<WorkflowInfo> workflowInfos;

  public List<WorkflowInfo> getWorkflowInfos() {
    return workflowInfos;
  }

  public void setWorkflowInfos(List<WorkflowInfo> workflowInfos) {
    this.workflowInfos = workflowInfos;
  }

  public static class Count extends Format {

    private long running;
    private long stop;

    public Count() {
      this(0, 0);
    }

    public Count(long running, long stop) {
      setRunning(running);
      setStop(stop);
    }

    public long getRunning() {
      return running;
    }

    public void setRunning(long running) {
      this.running = running;
    }

    public long getStop() {
      return stop;
    }

    public void setStop(long stop) {
      this.stop = stop;
    }
  }

  public static class WorkflowInfo extends Format {

    private Long workflowId;
    private Count count;

    public Long getWorkflowId() {
      return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
      this.workflowId = workflowId;
    }

    public Count getCount() {
      return count;
    }

    public void setCount(
        Count count) {
      this.count = count;
    }
  }
}
