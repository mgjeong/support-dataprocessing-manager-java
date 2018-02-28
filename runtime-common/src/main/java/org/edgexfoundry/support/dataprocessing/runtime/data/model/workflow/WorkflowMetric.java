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

  private List<GroupInfo> groups;

  public List<GroupInfo> getGroups() {
    return groups;
  }

  public void setGroups(List<GroupInfo> groups) {
    this.groups = groups;
  }

  public static class Work extends Format {

    private long running;
    private long stop;

    public Work() {
      this(0, 0);
    }

    public Work(long running, long stop) {
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

  public static class GroupInfo extends Format {

    private String groupId;
    private Work works;

    public String getGroupId() {
      return groupId;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }

    public Work getWorks() {
      return works;
    }

    public void setWorks(Work works) {
      this.works = works;
    }
  }
}
