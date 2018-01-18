package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowState extends Format {

  private Long workflowId;
  private String name;
  private String description;

  public WorkflowState() {

  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
