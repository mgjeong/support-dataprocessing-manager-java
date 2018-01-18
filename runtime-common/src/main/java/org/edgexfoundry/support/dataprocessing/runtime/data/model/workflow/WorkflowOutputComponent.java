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
    this.outputStreams = outputStreams;
  }

  @JsonIgnore
  public void addOutputStream(WorkflowStream stream) {
    if (stream != null && outputStreams != null) {
      outputStreams.add(stream);
    }
  }
}
