package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologyData {

  private String topologyName;
  private String config;
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

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
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
}
