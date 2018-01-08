package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyDetailed extends Format {

  public enum TopologyRunningStatus {
    RUNNING, NOT_RUNNING, UNKNOWN
  }

  private Topology topology;
  private TopologyRunningStatus running;
  private String namespaceName;

  public TopologyDetailed() {

  }

  public Topology getTopology() {
    return topology;
  }

  public void setTopology(Topology topology) {
    this.topology = topology;
  }

  public TopologyRunningStatus getRunning() {
    return running;
  }

  public void setRunning(TopologyRunningStatus running) {
    this.running = running;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }
}
