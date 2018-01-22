package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream.Grouping;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowEdge extends Format {

  private Long id;
  private Long versionId;
  private Long workflowId;
  private Long fromId;
  private Long toId;
  private Long versionTimestamp;
  private List<StreamGrouping> streamGroupings;

  public WorkflowEdge() {

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

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public Long getFromId() {
    return fromId;
  }

  public void setFromId(Long fromId) {
    this.fromId = fromId;
  }

  public Long getToId() {
    return toId;
  }

  public void setToId(Long toId) {
    this.toId = toId;
  }

  @JsonProperty("timestamp")
  public Long getVersionTimestamp() {
    return versionTimestamp;
  }

  @JsonProperty("timestamp")
  public void setVersionTimestamp(Long versionTimestamp) {
    this.versionTimestamp = versionTimestamp;
  }

  public List<StreamGrouping> getStreamGroupings() {
    return streamGroupings;
  }

  public void setStreamGroupings(
      List<StreamGrouping> streamGroupings) {
    this.streamGroupings = streamGroupings;
  }

  @JsonIgnore
  public String getStreamGroupingsStr() {
    if (this.streamGroupings.isEmpty()) {
      return "[]";
    } else {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.streamGroupings);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonIgnore
  public void setStreamGroupingsStr(String groupings) {
    try {
      if (!StringUtils.isEmpty(groupings)) {
        ObjectMapper mapper = new ObjectMapper();
        this.streamGroupings = mapper
            .readValue(groupings, new TypeReference<List<StreamGrouping>>() {
            });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonInclude(Include.NON_NULL)
  public static class StreamGrouping extends Format {

    private Long streamId;
    private Grouping grouping;
    private List<String> fields;

    public Long getStreamId() {
      return streamId;
    }

    public void setStreamId(Long streamId) {
      this.streamId = streamId;
    }

    public Grouping getGrouping() {
      return grouping;
    }

    public void setGrouping(
        Grouping grouping) {
      this.grouping = grouping;
    }

    public List<String> getFields() {
      return fields;
    }

    public void setFields(List<String> fields) {
      this.fields = fields;
    }
  }
}
