package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

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
public class TopologyData {

  private Long topologyId;
  private String topologyName;
  private Map<String, Object> config = new HashMap<>();
  private List<TopologySource> sources = new ArrayList<>();
  private List<TopologySink> sinks = new ArrayList<>();
  private List<TopologyProcessor> processors = new ArrayList<>();
  private List<TopologyEdge> edges = new ArrayList<>();
  private TopologyEditorMetadata topologyEditorMetadata;

  public TopologyData() {

  }

  public String getTopologyName() {
    return topologyName;
  }

  public void setTopologyName(String topologyName) {
    this.topologyName = topologyName;
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

  public List<TopologySource> getSources() {
    return sources;
  }

  public void setSources(
      List<TopologySource> sources) {
    this.sources = sources;
  }

  public List<TopologySink> getSinks() {
    return sinks;
  }

  public void setSinks(
      List<TopologySink> sinks) {
    this.sinks = sinks;
  }

  public List<TopologyProcessor> getProcessors() {
    return processors;
  }

  public void setProcessors(
      List<TopologyProcessor> processors) {
    this.processors = processors;
  }

  public List<TopologyEdge> getEdges() {
    return edges;
  }

  public void setEdges(
      List<TopologyEdge> edges) {
    this.edges = edges;
  }

  public TopologyEditorMetadata getTopologyEditorMetadata() {
    return topologyEditorMetadata;
  }

  public void setTopologyEditorMetadata(
      TopologyEditorMetadata topologyEditorMetadata) {
    this.topologyEditorMetadata = topologyEditorMetadata;
  }

  public Long getTopologyId() {
    return this.topologyId;
  }

  public void setTopologyId(Long topologyId){
    this.topologyId = topologyId;
  }
}
