package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.List;

public abstract class TopologyOutputComponent extends TopologyComponent {

  private List<Long> outputStreamIds;
  private List<TopologyStream> outputStreams;

  public TopologyOutputComponent() {

  }

  public List<Long> getOutputStreamIds() {
    return outputStreamIds;
  }

  public void setOutputStreamIds(List<Long> outputStreamIds) {
    this.outputStreamIds = outputStreamIds;
  }

  public List<TopologyStream> getOutputStreams() {
    return outputStreams;
  }

  public void setOutputStreams(List<TopologyStream> outputStreams) {
    this.outputStreams = outputStreams;
  }
}
