package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Schema.Type;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowStream extends Format {

  private Long id;
  private Long versionId;
  private Long componentId;
  private String streamId;
  private String description;
  private Long workflowId;
  private List<Field> fields = new ArrayList<>();
  private Long versionTimestamp;

  public WorkflowStream() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public String getStreamId() {
    return streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    this.fields = fields;
  }

  public Long getVersionTimestamp() {
    return versionTimestamp;
  }

  public void setVersionTimestamp(Long versionTimestamp) {
    this.versionTimestamp = versionTimestamp;
  }

  @JsonIgnore
  public Long getComponentId() {
    return componentId;
  }

  @JsonIgnore
  public void setComponentId(Long componentId) {
    this.componentId = componentId;
  }

  @JsonIgnore
  public String getFieldsStr() {
    if (this.fields.isEmpty()) {
      return "[]";
    } else {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.fields);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonIgnore
  public void setFieldsStr(String fields) {
    try {
      if (!StringUtils.isEmpty(fields)) {
        ObjectMapper mapper = new ObjectMapper();
        this.fields = mapper
            .readValue(fields, new TypeReference<List<Field>>() {
            });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void addField(Field field) {
    if (field != null && fields != null) {
      fields.add(field);
    }
  }

  @JsonInclude(Include.NON_NULL)
  public static class Field {

    private String name;
    private Schema.Type type;
    boolean optional;

    public Field() {

    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public boolean isOptional() {
      return optional;
    }

    public void setOptional(boolean optional) {
      this.optional = optional;
    }
  }
}
