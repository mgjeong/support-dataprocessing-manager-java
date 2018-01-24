package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

import java.util.ArrayList;

public class WorkflowMetric extends Format {

  private ArrayList<GroupInfo> groups;

  public ArrayList<GroupInfo> getGroups() {
    return groups;
  }

  public void setGroups(ArrayList<GroupInfo> groups) {
    this.groups = groups;
  }

  public static class Work extends Format {
    private long running;
    private long stop;

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
