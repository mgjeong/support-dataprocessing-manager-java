package org.edgexfoundry.support.dataprocessing.runtime.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Namespace;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: this is a temporary table manager class that mimics database operation.
// TODO: concurrency control is NOT considered for this mock-up implementation.
public final class TopologyTableManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyTableManager.class);

  /**
   * key: topology id, value: topology
   */
  private Map<Long, Topology> topologies;

  /**
   * key: topology id, value: editor metadata
   */
  private Map<Long, TopologyEditorMetadata> topologyEditorMetadataMap;
  private List<TopologyComponentBundle> topologyComponentBundles;

  /**
   * key: user id, value: editor toolbar
   */
  private Map<Long, TopologyEditorToolbar> topologyEditorToolbarMap;

  /**
   * key: topology id_version id_component id, value: component
   */
  private Map<String, TopologyComponent> topologyComponentMap;

  /**
   * key: namespace id, value: namespace
   */
  private Map<Long, Namespace> namespaceMap;

  /**
   * Temporary. Used to mimic database auto-increment counter
   */
  private static Long TEMP_IDX = 1L;

  public TopologyTableManager() {
    // mock
    mockDatabase();
  }

  private void mockDatabase() {
    this.topologies = new HashMap<>();
    this.topologyEditorMetadataMap = new HashMap<>();
    this.topologyEditorToolbarMap = new HashMap<>();
    this.topologyComponentMap = new HashMap<>();
    this.namespaceMap = new HashMap<>();

    this.topologyComponentBundles = new ArrayList<>();
    mockComponentBundles();
    mockNamespaces();
  }

  private void mockNamespaces() {
    Namespace.Info firstInfo = new Namespace.Info();
    firstInfo.setId(1L);
    firstInfo.setDescription("First namespace");
    firstInfo.setName("Dover");
    firstInfo.setStreamingEngine("STORM");
    firstInfo.setTimeSeriesDB(null);
    firstInfo.setTimestamp(System.currentTimeMillis());

    Namespace.ServiceClusterMap firstMap = new Namespace.ServiceClusterMap();
    firstMap.setClusterId(1L);
    firstMap.setNamespaceId(1L);
    firstMap.setServiceName("STORM");

    // enrich
    Namespace first = new Namespace();
    first.setNamespace(firstInfo);
    first.addMapping(firstMap);
    this.namespaceMap.put(firstInfo.getId(), first);
  }

  private void mockComponentBundles() {
    // add source
    TopologyComponentBundle dpfwSource = new TopologyComponentBundle();
    dpfwSource.setId(TEMP_IDX++);
    dpfwSource.setName("DPFW-SOURCE");
    dpfwSource.setType(TopologyComponentBundle.TopologyComponentType.SOURCE);
    dpfwSource.setTimestamp(System.currentTimeMillis());
    dpfwSource.setStreamingEngine("STORM");
    dpfwSource.setSubType("DPFW");
    dpfwSource.setBundleJar("");
    dpfwSource.setTransformationClass("");

    ComponentUISpecification componentUISpecification = new ComponentUISpecification();
    addUIFIeld(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIFIeld(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSource.setFieldHintProviderClass(null);
    dpfwSource.setTransformationClass("");
    dpfwSource.setBuiltin(true);
    dpfwSource.setMavenDeps("");

    this.topologyComponentBundles.add(dpfwSource);

    // add processor
    TopologyComponentBundle regression = new TopologyComponentBundle();
    regression.setId(TEMP_IDX++);
    regression.setName("regression-linear");
    regression.setType(TopologyComponentBundle.TopologyComponentType.PROCESSOR);
    regression.setStreamingEngine("STORM");
    regression.setTimestamp(System.currentTimeMillis());
    regression.setSubType("DPFW");
    regression.setBundleJar("");
    componentUISpecification = new ComponentUISpecification();
    addUIFIeld(componentUISpecification, "weights", "weights", "Enter weights");
    addUIFIeld(componentUISpecification, "error", "error", "Enter error");
    addUIFIeld(componentUISpecification, "type", "type", "Enter type");
    addUIFIeld(componentUISpecification, "inrecord", "inrecord", "Enter inrecord");
    addUIFIeld(componentUISpecification, "outrecord", "outrecord", "Enter outrecord");
    regression.setTopologyComponentUISpecification(componentUISpecification);
    regression.setFieldHintProviderClass("");
    regression.setTransformationClass("");
    regression.setBuiltin(true);
    regression.setMavenDeps("");

    this.topologyComponentBundles.add(regression);

    // add sink
    TopologyComponentBundle dpfwSink = new TopologyComponentBundle();
    dpfwSink.setId(TEMP_IDX++);
    dpfwSink.setName("DPFW-SINK");
    dpfwSink.setType(TopologyComponentBundle.TopologyComponentType.SINK);
    dpfwSink.setTimestamp(System.currentTimeMillis());
    dpfwSink.setStreamingEngine("STORM");
    dpfwSink.setSubType("DPFW");
    dpfwSink.setBundleJar("");

    componentUISpecification = new ComponentUISpecification();
    addUIFIeld(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIFIeld(componentUISpecification, "Data Sink", "dataSink", "Enter data sink");
    dpfwSink.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSink.setFieldHintProviderClass("");
    dpfwSink.setTransformationClass("");
    dpfwSink.setBuiltin(true);
    dpfwSink.setMavenDeps("");

    this.topologyComponentBundles.add(dpfwSink);

    // add topology
    TopologyComponentBundle runtimeTopology = new TopologyComponentBundle();
    runtimeTopology.setId(TEMP_IDX++);
    runtimeTopology.setName("Runtime topology");
    runtimeTopology.setType(TopologyComponentBundle.TopologyComponentType.TOPOLOGY);
    runtimeTopology.setTimestamp(System.currentTimeMillis());
    runtimeTopology.setStreamingEngine("STORM");
    runtimeTopology.setSubType("TOPOLOGY");
    runtimeTopology.setBundleJar(null);

    componentUISpecification = new ComponentUISpecification();
    ComponentUISpecification.UIField runtimeHost = new ComponentUISpecification.UIField();
    runtimeHost.setUiName("Runtime host");
    runtimeHost.setFieldName("runtimeHost");
    runtimeHost.setUserInput(true);
    runtimeHost.setTooltip("Enter hostname of runtime edge.");
    runtimeHost.setOptional(false);
    runtimeHost.setType("string");
    runtimeHost.setDefaultValue("localhost:8082");
    componentUISpecification.addUIField(runtimeHost);
    ComponentUISpecification.UIField targetHost = new ComponentUISpecification.UIField();
    targetHost.setUiName("Target host");
    targetHost.setFieldName("targetHost");
    targetHost.setUserInput(true);
    targetHost.setTooltip("Enter hostname of target edge.");
    targetHost.setOptional(false);
    targetHost.setType("string");
    targetHost.setDefaultValue("localhost:9092");
    componentUISpecification.addUIField(targetHost);
    runtimeTopology.setTopologyComponentUISpecification(componentUISpecification);

    runtimeTopology.setFieldHintProviderClass(null);
    runtimeTopology.setTransformationClass("dummy");
    runtimeTopology.setBuiltin(true);
    runtimeTopology.setMavenDeps("");

    this.topologyComponentBundles.add(runtimeTopology);
  }

  private void addUIFIeld(ComponentUISpecification componentUISpecification, String uiName,
      String fieldName, String tooltip) {
    ComponentUISpecification.UIField field = new ComponentUISpecification.UIField();
    field.setUiName(uiName);
    field.setFieldName(fieldName);
    field.setUserInput(true);
    field.setTooltip(tooltip);
    field.setOptional(false);
    field.setType("string");
    componentUISpecification.addUIField(field);
  }

  public Collection<Topology> listTopologies() {
    return Collections.unmodifiableCollection(this.topologies.values());
  }

  public Collection<TopologyComponentBundle> listTopologyComponentBundles(
      TopologyComponentBundle.TopologyComponentType type) {
    return this.topologyComponentBundles.stream()
        .filter(component -> component.getType() == type)
        .collect(Collectors.toSet());
  }

  public Topology addTopology(Topology topology) {
    topology.setId(TEMP_IDX++);
    topology.setTimestamp(System.currentTimeMillis());
    this.topologies.put(topology.getId(), topology);
    return topology;
  }

  public TopologyEditorMetadata addTopologyEditorMetadata(
      TopologyEditorMetadata topologyEditorMetadata) {
    Long topologyId = topologyEditorMetadata.getTopologyId();
    topologyEditorMetadata.setVersionId(1L);
    topologyEditorMetadata.setTimestamp(System.currentTimeMillis());
    this.topologyEditorMetadataMap.put(topologyId, topologyEditorMetadata);
    return topologyEditorMetadata;
  }

  public TopologyEditorMetadata addOrUpdateTopologyEditorMetadata(Long topologyId,
      TopologyEditorMetadata metaData) {
    metaData.setTimestamp(System.currentTimeMillis());
    this.topologyEditorMetadataMap.put(topologyId, metaData);
    return metaData;
  }

  public Topology getTopology(Long topologyId) {
    return this.topologies.get(topologyId);
  }

  public Collection<TopologySource> listSources(Long topologyId, Long versionId) {
    Collection<TopologySource> sources = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if (!(component instanceof TopologySource)) {
        continue;
      }
      if (component.getTopologyId() == topologyId) {
        sources.add((TopologySource) component);
      }
    }
    return sources;
  }

  public Collection<TopologyProcessor> listProcessors(Long topologyId, Long versionId) {
    Collection<TopologyProcessor> processors = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if (!(component instanceof TopologyProcessor)) {
        continue;
      }
      if (component.getTopologyId() == topologyId) {
        processors.add((TopologyProcessor) component);
      }
    }
    return processors;
  }

  public Collection<TopologySink> listSinks(Long topologyId, Long versionId) {
    Collection<TopologySink> sinks = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if (!(component instanceof TopologySink)) {
        continue;
      }
      if (component.getTopologyId() == topologyId) {
        sinks.add((TopologySink) component);
      }
    }
    return sinks;
  }

  public Collection<TopologyEdge> listEdges(Long topologyId, Long versionId) {
    return new HashSet<>();
  }

  public TopologyEditorMetadata getTopologyEditorMetadata(Long topologyId, Long versionId) {
    TopologyEditorMetadata topologyEditorMetadata = this.topologyEditorMetadataMap.get(topologyId);
    return topologyEditorMetadata;
  }

  public Collection<TopologyVersion> listTopologyVersionInfos(Long topologyId) {
    List<TopologyVersion> versions = new ArrayList<>();
    TopologyVersion firstVersion = new TopologyVersion();
    firstVersion.setId(1L);
    firstVersion.setDescription("First version");
    firstVersion.setName("CURRENT");
    firstVersion.setTimestamp(System.currentTimeMillis());
    firstVersion.setTopologyId(topologyId);
    versions.add(firstVersion);

    return Collections.unmodifiableCollection(versions);
  }

  public Collection<Namespace> listNamespaces() {
    return this.namespaceMap.values();
  }


  public TopologyEditorToolbar getTopologyEditorToolbar() {
    TopologyEditorToolbar toolbar = this.topologyEditorToolbarMap.get(1);// user id is always 1
    if (toolbar == null) {
      toolbar = makeDefaultTopologyEditorToolbar();
      this.topologyEditorToolbarMap.put(1L, toolbar);
    }
    return toolbar;
  }

  private TopologyEditorToolbar makeDefaultTopologyEditorToolbar() {
    TopologyEditorToolbar toolbar = new TopologyEditorToolbar();
    toolbar.setUserId(1L);
    JsonObject data = new JsonObject();
    JsonArray sources = new JsonArray();
    JsonArray processors = new JsonArray();
    JsonArray sinks = new JsonArray();
    for (TopologyComponentBundle bundle : this.topologyComponentBundles) {
      JsonObject b = new JsonObject();
      b.addProperty("bundleId", bundle.getId());
      if (bundle.getType() == TopologyComponentType.SOURCE) {
        sources.add(b);
      } else if (bundle.getType() == TopologyComponentType.SINK) {
        sinks.add(b);
      } else if (bundle.getType() == TopologyComponentType.PROCESSOR) {
        processors.add(b);
      }
    }
    data.add("sources", sources);
    data.add("sinks", sinks);
    data.add("processors", processors);
    toolbar.setData(data.toString());
    toolbar.setTimestamp(System.currentTimeMillis());
    return toolbar;
  }

  public TopologyEditorToolbar addOrUpdateTopologyEditorToolbar(TopologyEditorToolbar toolbar) {
    toolbar.setTimestamp(System.currentTimeMillis());
    this.topologyEditorToolbarMap.put(toolbar.getUserId(), toolbar);
    return toolbar;
  }

  public TopologySource addOrUpdateTopologySource(Long topologyId, Long sourceId,
      TopologySource topologySource) {
    topologySource.setId(sourceId);
    topologySource.setVersionId(1L);
    topologySource.setTopologyId(topologyId);
    topologySource.setReconfigure(false);
    topologySource.setTimestamp(System.currentTimeMillis());

    this.topologyComponentMap
        .put(makeTopologyComponentKey(topologyId, 1L, sourceId), topologySource);
    return topologySource;
  }

  public TopologySource addTopologySource(Long topologyId, Long versionId,
      TopologySource topologySource) {
    if (topologySource.getId() == null) {
      topologySource.setId(TEMP_IDX++);
    }
    topologySource.setVersionId(versionId);
    topologySource.setTopologyId(topologyId);
    topologySource.setOutputStreams(new ArrayList<>());
    topologySource.setTimestamp(System.currentTimeMillis());
    String key = makeTopologyComponentKey(topologyId, versionId, topologySource.getId());
    this.topologyComponentMap.put(key, topologySource);
    return topologySource;
  }

  public TopologyProcessor addTopologyProcessor(Long topologyId, Long versionId,
      TopologyProcessor topologyProcessor) {
    if (topologyProcessor.getId() == null) {
      topologyProcessor.setId(TEMP_IDX++);
    }
    topologyProcessor.setVersionId(versionId);
    topologyProcessor.setTopologyId(topologyId);
    topologyProcessor.setOutputStreams(new ArrayList<>());
    String key = makeTopologyComponentKey(topologyId, versionId, topologyProcessor.getId());
    this.topologyComponentMap.put(key, topologyProcessor);
    return topologyProcessor;
  }

  public TopologySink addTopologySink(Long topologyId, Long versionId,
      TopologySink topologySink) {
    if (topologySink.getId() == null) {
      topologySink.setId(TEMP_IDX++);
    }
    topologySink.setVersionId(versionId);
    topologySink.setTopologyId(topologyId);
    String key = makeTopologyComponentKey(topologyId, versionId, topologySink.getId());
    this.topologyComponentMap.put(key, topologySink);
    return topologySink;
  }

  private String makeTopologyComponentKey(Long topologyId, Long versionId, Long componentId) {
    return topologyId + "_" + versionId + "_" + componentId;
  }

  public TopologyComponent getTopologyComponent(Long topologyId, Long versionId, Long componentId) {
    return this.topologyComponentMap
        .get(makeTopologyComponentKey(topologyId, versionId, componentId));
  }

}
