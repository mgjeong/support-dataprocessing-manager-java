package org.edgexfoundry.support.dataprocessing.runtime.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ClusterWithService;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ClusterWithService.ServiceConfiguration;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Namespace;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyOutputComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream;
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

  /**
   * key: bundle id, value: bundle
   */
  private Map<Long, TopologyComponentBundle> topologyComponentBundleMap;

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
   * key: edge id, value: edge
   */
  private Map<Long, TopologyEdge> topologyEdgeMap;

  /**
   * key: stream id, value: stream
   */
  private Map<Long, TopologyStream> topologyStreamMap;

  /**
   * key: cluster with service id, value: cluster with service
   */
  private Map<Long, ClusterWithService> clusterWithServiceMap;

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
    this.topologyEdgeMap = new HashMap<>();
    this.topologyStreamMap = new HashMap<>();
    this.clusterWithServiceMap = new HashMap<>();
    this.topologyComponentBundleMap = new HashMap<>();

    mockComponentBundles();
    mockNamespaces();
    mockClusterWithServices();
  }

  private void mockClusterWithServices() {
    ClusterWithService cs = new ClusterWithService();
    cs.setId(1L);
    ClusterWithService.Cluster cluster = new ClusterWithService.Cluster();
    cluster.setId(1L);
    cluster.setDescription("First cluster");
    cluster.setName("First Cluster");
    cluster.setTimestamp(System.currentTimeMillis());
    cs.setCluster(cluster);

    List<ServiceConfiguration> serviceConfigurations = new ArrayList<>();
    ServiceConfiguration sc = new ServiceConfiguration();
    ClusterWithService.Service service = new ClusterWithService.Service();
    service.setId(1L);
    service.setName("STORM");
    service.setClusterId(1L);
    service.setDescription("");
    service.setTimestamp(System.currentTimeMillis());
    sc.setService(service);
    List<ClusterWithService.Configuration> configurations = new ArrayList<>();
    ClusterWithService.Configuration configuration = new ClusterWithService.Configuration();
    configuration.setId(1L);
    configuration.setServiceId(1L);
    configuration.setName("storm");
    configuration.setConfiguration("{}");
    configuration.setDescription("");
    configuration.setFilename("");
    configuration.setTimestamp(System.currentTimeMillis());
    configuration.setConfigurationMap(new HashMap<>());
    configurations.add(configuration);
    sc.setConfigurations(configurations);
    serviceConfigurations.add(sc);
    cs.setServiceConfigurations(serviceConfigurations);

    this.clusterWithServiceMap.put(cs.getId(), cs);
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
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Source", "dataSource", "Enter data source");
    dpfwSource.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSource.setFieldHintProviderClass(null);
    dpfwSource.setTransformationClass("");
    dpfwSource.setBuiltin(true);
    dpfwSource.setMavenDeps("");

    this.topologyComponentBundleMap.put(dpfwSource.getId(), dpfwSource);

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
    addUIField(componentUISpecification, "weights", "weights", "Enter weights");
    addUIField(componentUISpecification, "error", "error", "Enter error");
    addUIField(componentUISpecification, "type", "type", "Enter type");
    addUIField(componentUISpecification, "inrecord", "inrecord", "Enter inrecord");
    addUIField(componentUISpecification, "outrecord", "outrecord", "Enter outrecord");
    regression.setTopologyComponentUISpecification(componentUISpecification);
    regression.setFieldHintProviderClass("");
    regression.setTransformationClass("");
    regression.setBuiltin(true);
    regression.setMavenDeps("");

    this.topologyComponentBundleMap.put(regression.getId(), regression);

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
    addUIField(componentUISpecification, "Data Type", "dataType", "Enter data type");
    addUIField(componentUISpecification, "Data Sink", "dataSink", "Enter data sink");
    dpfwSink.setTopologyComponentUISpecification(componentUISpecification);

    dpfwSink.setFieldHintProviderClass("");
    dpfwSink.setTransformationClass("");
    dpfwSink.setBuiltin(true);
    dpfwSink.setMavenDeps("");

    this.topologyComponentBundleMap.put(dpfwSink.getId(), dpfwSink);

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

    this.topologyComponentBundleMap.put(runtimeTopology.getId(), runtimeTopology);
  }

  private void addUIField(ComponentUISpecification componentUISpecification, String uiName,
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
    return this.topologyComponentBundleMap.values().stream()
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

  public Topology getTopology(Long topologyId, Long versionId) {
    return this.topologies.get(topologyId);
  }

  public Collection<TopologySource> listSources(Long topologyId, Long versionId) {
    Collection<TopologySource> sources = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if ((component instanceof TopologySource) && (component.getTopologyId() == topologyId)) {
        sources.add((TopologySource) component);
      }
    }
    return sources;
  }

  public Collection<TopologyProcessor> listProcessors(Long topologyId, Long versionId) {
    Collection<TopologyProcessor> processors = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if ((component instanceof TopologyProcessor) && (component.getTopologyId() == topologyId)) {
        processors.add((TopologyProcessor) component);
      }
    }
    return processors;
  }

  public Collection<TopologySink> listSinks(Long topologyId, Long versionId) {
    Collection<TopologySink> sinks = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if ((component instanceof TopologySink) && (component.getTopologyId() == topologyId)) {
        sinks.add((TopologySink) component);
      }
    }
    return sinks;
  }

  public Collection<TopologyEdge> listEdges(Long topologyId, Long versionId) {
    Collection<TopologyEdge> edges = new ArrayList<>();
    for (TopologyEdge component : this.topologyEdgeMap.values()) {
      if (component.getTopologyId() == topologyId) {
        edges.add(component);
      }
    }
    return edges;
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
    for (TopologyComponentBundle bundle : this.topologyComponentBundleMap.values()) {
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

    if (topologySource.getOutputStreams() != null) {
      updateOutputStreams(topologySource);
    }

    this.topologyComponentMap
        .put(makeTopologyComponentKey(topologyId, 1L, sourceId), topologySource);
    return topologySource;
  }

  private void updateOutputStreams(TopologyOutputComponent topologyOutputComponent) {
    List<TopologyStream> outputStreams = topologyOutputComponent.getOutputStreams();
    for (TopologyStream outputStream : outputStreams) {
      if (outputStream.getId() == null) {
        outputStream.setId(TEMP_IDX++);
      }

      this.topologyStreamMap.put(outputStream.getId(), outputStream);
    }
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

  public TopologyProcessor addOrUpdateTopologyProcessor(Long topologyId, Long sourceId,
      TopologyProcessor topologyProcessor) {
    topologyProcessor.setId(sourceId);
    topologyProcessor.setVersionId(1L);
    topologyProcessor.setTopologyId(topologyId);
    topologyProcessor.setReconfigure(false);
    topologyProcessor.setTimestamp(System.currentTimeMillis());

    if (topologyProcessor.getOutputStreams() != null) {
      updateOutputStreams(topologyProcessor);
    }

    this.topologyComponentMap
        .put(makeTopologyComponentKey(topologyId, 1L, sourceId), topologyProcessor);
    return topologyProcessor;
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

  public TopologySink addOrUpdateTopologySink(Long topologyId, Long sourceId,
      TopologySink topologySink) {
    topologySink.setId(sourceId);
    topologySink.setVersionId(1L);
    topologySink.setTopologyId(topologyId);
    topologySink.setReconfigure(false);
    topologySink.setTimestamp(System.currentTimeMillis());

    this.topologyComponentMap
        .put(makeTopologyComponentKey(topologyId, 1L, sourceId), topologySink);
    return topologySink;
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

  public TopologyEdge addTopologyEdge(Long topologyId, Long versionId, TopologyEdge topologyEdge) {
    if (topologyEdge.getId() == null) {
      topologyEdge.setId(TEMP_IDX++);
    }
    topologyEdge.setVersionId(versionId);
    topologyEdge.setTopologyId(topologyId);
    topologyEdge.setVersionTimestamp(System.currentTimeMillis());
    this.topologyEdgeMap.put(topologyEdge.getId(), topologyEdge);
    return topologyEdge;
  }

  private String makeTopologyComponentKey(Long topologyId, Long versionId, Long componentId) {
    return topologyId + "_" + versionId + "_" + componentId;
  }

  public TopologyComponent getTopologyComponent(Long topologyId, Long versionId, Long componentId) {
    return this.topologyComponentMap
        .get(makeTopologyComponentKey(topologyId, versionId, componentId));
  }

  public TopologyEdge getTopologyEdge(Long topologyId, Long versionId, Long edgeId) {
    return this.topologyEdgeMap.get(edgeId);
  }

  public TopologyEdge addOrUpdateTopologyEdge(Long topologyId, Long edgeId,
      TopologyEdge topologyEdge) {
    topologyEdge.setId(edgeId);
    topologyEdge.setVersionId(1L);
    topologyEdge.setTopologyId(topologyId);
    topologyEdge.setVersionTimestamp(System.currentTimeMillis());

    this.topologyEdgeMap.put(edgeId, topologyEdge);
    return topologyEdge;
  }

  public Collection<TopologyStream> listStreamInfos(Long topologyId, Long versionId) {
    Collection<TopologyStream> streams = new ArrayList<>();
    for (TopologyStream stream : this.topologyStreamMap.values()) {
      if (stream.getTopologyId() == topologyId) {
        streams.add(stream);
      }
    }
    return streams;
  }

  public TopologyStream getStreamInfo(Long topologyId, Long versionId, Long streamId) {
    return this.topologyStreamMap.get(streamId);
  }

  public TopologyStream removeStreamInfo(Long topologyId, Long streamId) {
    return this.topologyStreamMap.remove(streamId);
  }

  public TopologyEdge removeTopologyEdge(Long topologyId, Long edgeId) {
    return this.topologyEdgeMap.remove(edgeId);
  }

  public TopologyProcessor removeTopologyProcessor(Long topologyId, Long processorId,
      Long versionId,
      boolean removeEdges) {
    String key = makeTopologyComponentKey(topologyId, versionId, processorId);
    TopologyProcessor processor = (TopologyProcessor) this.topologyComponentMap.get(key);
    if (removeEdges) {
      removeAllEdges(topologyId, processor);
    }
    removeStreamMapping(processor);
    this.topologyComponentMap.remove(key);
    return processor;
  }

  public TopologySource removeTopologySource(Long topologyId, Long sourceId, Long versionId,
      boolean removeEdges) {
    String key = makeTopologyComponentKey(topologyId, versionId, sourceId);
    TopologySource source = (TopologySource) this.topologyComponentMap.get(key);
    if (removeEdges) {
      removeAllEdges(topologyId, source);
    }
    removeStreamMapping(source);
    this.topologyComponentMap.remove(key);
    return source;
  }

  public TopologySink removeTopologySink(Long topologyId, Long sinkId, Long versionId,
      boolean removeEdges) {
    String key = makeTopologyComponentKey(topologyId, versionId, sinkId);
    TopologySink sink = (TopologySink) this.topologyComponentMap.get(key);
    if (removeEdges) {
      removeAllEdges(topologyId, sink);
    }
    this.topologyComponentMap.remove(key);
    return sink;
  }

  private void removeStreamMapping(TopologyOutputComponent component) {
    /*
    if (component.getOutputStreams() == null || component.getOutputStreams().isEmpty()) {
      return;
    }
    for (TopologyStream stream : component.getOutputStreams()) {
      this.topologyStreamMap.remove(stream.getId());
    }
    */
  }

  private void removeAllEdges(Long topologyId, TopologyComponent component) {
    List<Long> edgesToRemove = new ArrayList<>();
    for (TopologyEdge edge : this.topologyEdgeMap.values()) {
      if (edge.getFromId() == component.getId() ||
          edge.getToId() == component.getId()) {
        edgesToRemove.add(edge.getId());
      }
    }
    for (Long id : edgesToRemove) {
      removeTopologyEdge(topologyId, id);
    }
  }

  public Topology addOrUpdateTopology(Long topologyId, Topology topology) {
    topology.setId(topologyId);
    this.topologies.put(topologyId, topology);
    return topology;
  }

  public Collection<ClusterWithService> listClusterWithServices() {
    return this.clusterWithServiceMap.values();
  }

  public Topology removeTopology(Long topologyId) {
    Topology topology = this.topologies.get(topologyId);
    this.topologies.remove(topology.getId());

    // remove sources
    // remove processors
    // remove sinks
    Collection<Long> del = new ArrayList<>();
    for (TopologyComponent component : this.topologyComponentMap.values()) {
      if (component.getTopologyId() == topologyId) {
        del.add(component.getId());
      }
    }
    for (Long d : del) {
      this.topologyComponentMap.remove(d);
    }

    del.clear();
    // remove streams
    for (TopologyStream stream : this.topologyStreamMap.values()) {
      if (stream.getTopologyId() == topologyId) {
        del.add(stream.getId());
      }
    }
    for (Long d : del) {
      this.topologyStreamMap.remove(d);
    }

    del.clear();
    // remove edges
    for (TopologyEdge edge : this.topologyEdgeMap.values()) {
      if (edge.getTopologyId() == topologyId) {
        del.add(edge.getId());
      }
    }
    for (Long d : del) {
      this.topologyEdgeMap.remove(d);
    }

    return topology;
  }

  public String exportTopology(Topology topology) throws Exception {
    TopologyData topologyData = doExportTopology(topology);
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(topologyData);
  }

  public TopologyData doExportTopology(Topology topology) {
    TopologyData topologyData = new TopologyData();
    topologyData.setTopologyName(topology.getName());
    topologyData.setConfig(topology.getConfigStr());
    topologyData.setTopologyEditorMetadata(
        getTopologyEditorMetadata(topology.getId(), topology.getVersionId()));

    topologyData.setSources(
        (List<TopologySource>) listSources(topology.getId(), topology.getVersionId()));
    topologyData.setProcessors(
        (List<TopologyProcessor>) listProcessors(topology.getId(), topology.getVersionId()));
    topologyData.setSinks(
        (List<TopologySink>) listSinks(topology.getId(), topology.getVersionId()));
    topologyData
        .setEdges((List<TopologyEdge>) listEdges(topology.getId(), topology.getVersionId()));

    return topologyData;
  }

  public Topology importTopology(Long namespaceId, String topologyName,
      TopologyData topologyData) {
    Topology topology = new Topology();
    topology.setId(TEMP_IDX++);
    topology.setTimestamp(System.currentTimeMillis());
    topology.setVersionId(1L);
    topology.setName(topologyName);
    topology.setConfigStr(topologyData.getConfigStr());
    topology.setNamespaceId(namespaceId);
    this.topologies.put(topology.getId(), topology);

    TopologyEditorMetadata editorMetadata = topologyData.getTopologyEditorMetadata();
    this.topologyEditorMetadataMap.put(topology.getId(), editorMetadata);

    List<TopologySource> sources = topologyData.getSources();
    for (TopologySource source : sources) {
      source.setId(TEMP_IDX++);
      source.setTopologyId(topology.getId());
      source.setVersionId(1L);
      source.setTimestamp(System.currentTimeMillis());
      this.topologyComponentMap
          .put(makeTopologyComponentKey(topology.getId(), topology.getVersionId(), source.getId()),
              source);
    }

    List<TopologyProcessor> processors = topologyData.getProcessors();
    for (TopologyProcessor processor : processors) {
      processor.setId(TEMP_IDX++);
      processor.setTopologyId(topology.getId());
      processor.setVersionId(1L);
      processor.setTimestamp(System.currentTimeMillis());
      this.topologyComponentMap
          .put(makeTopologyComponentKey(topology.getId(), topology.getVersionId(),
              processor.getId()),
              processor);
    }

    List<TopologySink> sinks = topologyData.getSinks();
    for (TopologySink sink : sinks) {
      sink.setId(TEMP_IDX++);
      sink.setTopologyId(topology.getId());
      sink.setVersionId(1L);
      sink.setTimestamp(System.currentTimeMillis());
      this.topologyComponentMap
          .put(makeTopologyComponentKey(topology.getId(), topology.getVersionId(), sink.getId()),
              sink);
    }

    List<TopologyEdge> edges = topologyData.getEdges();
    for (TopologyEdge edge : edges) {
      edge.setId(TEMP_IDX++);
      edge.setTopologyId(topology.getId());
      edge.setVersionId(1L);
      edge.setVersionTimestamp(System.currentTimeMillis());
      this.topologyEdgeMap.put(edge.getId(), edge);
    }

    return topology;
  }
}
