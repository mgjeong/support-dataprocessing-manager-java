package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;

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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ComponentUISpecification extends Format {

    private List<UIField> fields;

    public ComponentUISpecification() {
      this.fields = new ArrayList<>();
    }

    public List<UIField> getFields() {
      return fields;
    }

    public void setFields(List<UIField> fields) {
      if (fields == null) {
        throw new RuntimeException("Invalid fields");
      }
      this.fields = fields;
    }

    public void addUIField(UIField uiField) {
      if (uiField == null) {
        throw new RuntimeException("Invalid field");
      }
      this.fields.add(uiField);
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class UIField extends Format {

    private String uiName;
    private String fieldName;
    private Boolean isUserInput;
    private String tooltip;
    private Boolean isOptional;
    private UiFieldType type;
    private String defaultValue;
    private List<String> options = new ArrayList<>();

    public UIField() {

    }

    public List<String> getOptions() {
      return options;
    }

    public void setOptions(List<String> options) {
      this.options = options;
    }

    public String getUiName() {
      return uiName;
    }

    public void setUiName(String uiName) {
      this.uiName = uiName;
    }

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
    }

    @JsonProperty("isUserInput")
    public Boolean getUserInput() {
      return isUserInput;
    }

    public void setUserInput(Boolean userInput) {
      isUserInput = userInput;
    }

    @JsonProperty("isOptional")
    public Boolean getOptional() {
      return isOptional;
    }

    public void setOptional(Boolean optional) {
      isOptional = optional;
    }

    public String getTooltip() {
      return tooltip;
    }

    public void setTooltip(String tooltip) {
      this.tooltip = tooltip;
    }

    public UiFieldType getType() {
      return type;
    }

    public void setType(UiFieldType type) {
      this.type = type;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    public void addOption(String option) {
      if (!StringUtils.isEmpty(option)) {
        this.options.add(option);
      }
    }
  }
}
