package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public abstract class TopologyOutputComponent extends TopologyComponent {

  private List<TopologyStream> outputStreams = new ArrayList<>();

  public TopologyOutputComponent() {

  }

  public List<TopologyStream> getOutputStreams() {
    return outputStreams;
  }

  public void setOutputStreams(List<TopologyStream> outputStreams) {
    this.outputStreams = outputStreams;
  }

  @JsonIgnore
  public void addOutputStream(TopologyStream stream) {
    if (stream != null && outputStreams != null) {
      outputStreams.add(stream);
    }
  }
}
