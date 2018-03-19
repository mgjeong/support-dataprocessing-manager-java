/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class WorkflowData extends Format {

  private static final long serialVersionUID = 1L;

  private Long workflowId;
  private String workflowName;
  private Map<String, Object> config = new HashMap<>();
  private List<WorkflowSource> sources = new ArrayList<>();
  private List<WorkflowSink> sinks = new ArrayList<>();
  private List<WorkflowProcessor> processors = new ArrayList<>();
  private List<WorkflowEdge> edges = new ArrayList<>();
  private WorkflowEditorMetadata workflowEditorMetadata;

  public WorkflowData() {

  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  @JsonIgnore
  public String getConfigStr() {
    try {
      if (!this.config.isEmpty()) {
        return mapper.writeValueAsString(this.config);
      } else {
        return "{}";
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonProperty("config")
  public Map<String, Object> getConfig() {
    return this.config;
  }

  @JsonProperty("config")
  public void setConfig(Map<String, Object> config) {
    if (config == null) {
      throw new RuntimeException("Invalid config");
    }

    this.config = config;
  }

  public List<WorkflowSource> getSources() {
    return sources;
  }

  public void setSources(
      List<WorkflowSource> sources) {
    if (sources == null) {
      throw new RuntimeException("Invalid sources");
    }
    this.sources = sources;
  }

  public List<WorkflowSink> getSinks() {
    return sinks;
  }

  public void setSinks(
      List<WorkflowSink> sinks) {
    if (sinks == null) {
      throw new RuntimeException("Invalid sinks");
    }
    this.sinks = sinks;
  }

  public List<WorkflowProcessor> getProcessors() {
    return processors;
  }

  public void setProcessors(
      List<WorkflowProcessor> processors) {
    if (processors == null) {
      throw new RuntimeException("Invalid processors");
    }
    this.processors = processors;
  }

  public List<WorkflowEdge> getEdges() {
    return edges;
  }

  public void setEdges(
      List<WorkflowEdge> edges) {
    if (edges == null) {
      throw new RuntimeException("Invalid edges");
    }
    this.edges = edges;
  }

  public WorkflowEditorMetadata getWorkflowEditorMetadata() {
    return workflowEditorMetadata;
  }

  public void setWorkflowEditorMetadata(
      WorkflowEditorMetadata workflowEditorMetadata) {
    if (workflowEditorMetadata == null) {
      throw new RuntimeException("Invalid workflow editor metadata");
    }
    this.workflowEditorMetadata = workflowEditorMetadata;
  }

  public Long getWorkflowId() {
    return this.workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Invalid workflow id");
    }
    this.workflowId = workflowId;
  }

  @JsonIgnore
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

  public enum EngineType {
    MULTI, FLINK, KAPACITOR, UNKNOWN
  }
}
