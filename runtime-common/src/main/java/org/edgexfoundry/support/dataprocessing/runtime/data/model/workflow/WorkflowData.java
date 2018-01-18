package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(Include.NON_NULL)
public class WorkflowData {

  private Long workflowId;
  private String workflowName;
  private Map<String, Object> config = new HashMap<>();
  private List<WorkflowSource> sources = new ArrayList<>();
  private List<WorkflowSink> sinks = new ArrayList<>();
  private List<WorkflowProcessor> processors = new ArrayList<>();
  private List<WorkflowEdge> edges = new ArrayList<>();
  private WorkflowEditorMetadata workflowEditorMetadata;

  public enum EngineType {
    MULTI, FLINK, KAPACITOR, UNKNOWN
  }

  public WorkflowData() {

  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  @JsonProperty("config")
  public String getConfigStr() {
    try {
      if (!this.config.isEmpty()) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.config);
      } else {
        return "{}";
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public Map<String, Object> getConfig() {
    return this.config;
  }

  @JsonProperty("config")
  public void setConfig(String configStr) {
    try {
      if (!StringUtils.isEmpty(configStr)) {
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper
            .readValue(configStr, new TypeReference<Map<String, Object>>() {
            });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<WorkflowSource> getSources() {
    return sources;
  }

  public void setSources(
      List<WorkflowSource> sources) {
    this.sources = sources;
  }

  public List<WorkflowSink> getSinks() {
    return sinks;
  }

  public void setSinks(
      List<WorkflowSink> sinks) {
    this.sinks = sinks;
  }

  public List<WorkflowProcessor> getProcessors() {
    return processors;
  }

  public void setProcessors(
      List<WorkflowProcessor> processors) {
    this.processors = processors;
  }

  public List<WorkflowEdge> getEdges() {
    return edges;
  }

  public void setEdges(
      List<WorkflowEdge> edges) {
    this.edges = edges;
  }

  public WorkflowEditorMetadata getWorkflowEditorMetadata() {
    return workflowEditorMetadata;
  }

  public void setWorkflowEditorMetadata(
      WorkflowEditorMetadata workflowEditorMetadata) {
    this.workflowEditorMetadata = workflowEditorMetadata;
  }

  public Long getWorkflowId() {
    return this.workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public EngineType getEngineType() {
    EngineType engineType = null;

    for (WorkflowProcessor processor : processors) {
      if (processor.getEngineType().toLowerCase().equals("flink")) {
        if (engineType == null) {
          engineType = EngineType.FLINK;
        } else if (engineType == EngineType.KAPACITOR) {
          engineType = EngineType.MULTI;
          return engineType;
        }
      } else if (processor.getEngineType().toLowerCase().equals("kapacitor")) {
        if (engineType == null) {
          engineType = EngineType.KAPACITOR;
        } else if (engineType == EngineType.FLINK) {
          engineType = EngineType.MULTI;
          return engineType;
        }
      } else {
        engineType = EngineType.UNKNOWN;
        return engineType;
      }
    }

    return engineType;
  }
}
