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
