package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowComponentBundle extends Format {

  public enum WorkflowComponentBundleType {
    SOURCE, PROCESSOR, SINK, WORKFLOW, LINK;

    public static WorkflowComponentBundleType toWorkflowComponentBundleType(String type) {
      for (WorkflowComponentBundleType t : WorkflowComponentBundleType.values()) {
        if (t.name().equalsIgnoreCase(type)) {
          return t;
        }
      }
      return null;
    }
  }

  private Long id;
  private String name;
  private WorkflowComponentBundleType type;
  private String streamingEngine;
  private String subType;
  private String bundleJar;

  private ComponentUISpecification workflowComponentUISpecification;
  private String transformationClass;
  private Boolean builtin;

  public WorkflowComponentBundle() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WorkflowComponentBundleType getType() {
    return type;
  }

  public void setType(WorkflowComponentBundleType type) {
    this.type = type;
  }

  public String getStreamingEngine() {
    return streamingEngine;
  }

  public void setStreamingEngine(String streamingEngine) {
    this.streamingEngine = streamingEngine;
  }

  public String getSubType() {
    return subType;
  }

  public void setSubType(String subType) {
    this.subType = subType;
  }

  public String getBundleJar() {
    return bundleJar;
  }

  public void setBundleJar(String bundleJar) {
    this.bundleJar = bundleJar;
  }

  public ComponentUISpecification getWorkflowComponentUISpecification() {
    return workflowComponentUISpecification;
  }

  public void setWorkflowComponentUISpecification(
      ComponentUISpecification workflowComponentUISpecification) {
    this.workflowComponentUISpecification = workflowComponentUISpecification;
  }

  public String getTransformationClass() {
    return transformationClass;
  }

  public void setTransformationClass(String transformationClass) {
    this.transformationClass = transformationClass;
  }

  public Boolean isBuiltin() {
    return builtin;
  }

  public void setBuiltin(Boolean builtin) {
    this.builtin = builtin;
  }

}
