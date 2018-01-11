package org.edgexfoundry.support.dataprocessing.runtime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.Part;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ClusterWithService;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Namespace;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyDetailed;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJob;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyJobGroup;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyStream;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyVersion;
import org.edgexfoundry.support.dataprocessing.runtime.db.TopologyJobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.TopologyTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Topology UI", description = "API List for Topology UI")
@RequestMapping("/api/v1/catalog")
public class TopologyController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologyController.class);

  private TopologyTableManager topologyTableManager = null;
  private TaskManager taskManager = null;

  public TopologyController() {
    this.topologyTableManager = TopologyTableManager.getInstance();
    this.taskManager = TaskManager.getInstance();
  }

  @ApiOperation(value = "Sample get response", notes = "Returns response entity.")
  @RequestMapping(value = "/ping", method = RequestMethod.GET)
  public ResponseEntity getResponse() {
    return respondEntity(new ResponseFormat(), HttpStatus.OK);
  }

  @ApiOperation(value = "Get topologies", notes = "Returns a list of all topologies.")
  @RequestMapping(value = "/topologies", method = RequestMethod.GET)
  public ResponseEntity listTopologies(
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "sort", required = false) String sortType,
      @RequestParam(value = "ascending", required = false) Boolean ascending,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Collection<Topology> topologies = this.topologyTableManager.listTopologies();
    Collection<TopologyDetailed> topologyDetailedList = new ArrayList<>();
    for (Topology topology : topologies) {
      TopologyDetailed detailed = new TopologyDetailed();
      detailed.setTopology(topology);
      detailed.setRunning(TopologyDetailed.TopologyRunningStatus.NOT_RUNNING);
      detailed.setNamespaceName("Dover");
      topologyDetailedList.add(detailed);
    }
    return respondEntity(topologyDetailedList, HttpStatus.OK);
  }

  @ApiOperation(value = "Save topology", notes = "Saves topology detail.")
  @RequestMapping(value = "/topologies/{topologyId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopology(@PathVariable("topologyId") Long topologyId,
      @RequestBody Topology topology) {
    topology = this.topologyTableManager.addOrUpdateTopology(topologyId, topology);
    return respond(topology, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology by Id", notes = "Returns topology detail by Id.")
  @RequestMapping(value = "/topologies/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyById(@PathVariable("topologyId") Long topologyId,
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    return getTopologyByIdAndVersionId(topologyId, 1L, detail, latencyTopN);
  }

  @ApiOperation(value = "Get topology by id and version", notes = "Returns a topology by id and its version.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}")
  public ResponseEntity getTopologyByIdAndVersionId(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Topology topology = this.topologyTableManager.getTopology(topologyId);
    if (topology == null) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, "Topology does not exist."),
          HttpStatus.NOT_FOUND);
    }

    if (detail != null && detail) {
      // Enrich topology
      TopologyDetailed detailed = new TopologyDetailed();
      detailed.setTopology(topology);
      detailed.setNamespaceName("Dover");
      detailed.setRunning(TopologyDetailed.TopologyRunningStatus.NOT_RUNNING);
      return respond(detailed, HttpStatus.OK);
    } else {
      return respond(topology, HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Remove topology", notes = "Removes a topology")
  @RequestMapping(value = "/topologies/{topologyId}", method = RequestMethod.DELETE)
  public ResponseEntity removeTopology(@PathVariable("topologyId") Long topologyId,
      @RequestParam(value = "onlyCurrent", required = false) boolean onlyCurrent,
      @RequestParam(value = "force", required = false) boolean force) {
    Topology topology = this.topologyTableManager.removeTopology(topologyId);
    return respond(topology, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology versions", notes = "Returns a list of versions of a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions")
  public ResponseEntity listTopologyVersions(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyVersion> versions = this.topologyTableManager
        .listTopologyVersionInfos(topologyId);
    return respondEntity(versions, HttpStatus.OK);
  }

  @ApiOperation(value = "Get namespaces", notes = "Returns a list of all namespaces.")
  @RequestMapping(value = "/namespaces", method = RequestMethod.GET)
  public ResponseEntity listNamespaces(
      @RequestParam("detail") Boolean detail
  ) {
    Collection<Namespace> namespaces = this.topologyTableManager.listNamespaces();
    return respondEntity(namespaces, HttpStatus.OK);
  }

  @ApiOperation(value = "Get namespace detail", notes = "Returns a detailed namespace")
  @RequestMapping(value = "/namespaces/{namespaceId}", method = RequestMethod.GET)
  public ResponseEntity getNamespaceById(@PathVariable("namespaceId") Long namespaceId,
      @RequestParam(value = "detail", required = false) Boolean detail) {
    Collection<Namespace> namespaces = this.topologyTableManager.listNamespaces();
    Namespace namespace = namespaces.iterator().next();
    return respond(namespace, HttpStatus.OK);
  }

  @ApiOperation(value = "Get component bundles", notes = "Returns a list of bundles by component.")
  @RequestMapping(value = "/streams/componentbundles/{component}", method = RequestMethod.GET)
  public ResponseEntity listTopologyComponentBundles(
      @PathVariable("component") TopologyComponentBundle.TopologyComponentType componentType
  ) {
    this.taskManager.getTaskModelList();
    switch (componentType) {
      case SOURCE:
        return listTopologyComponentSourceBundles();
      case SINK:
        return listTopologyComponentSinkBundles();
      case PROCESSOR:
        return listTopologyComponentProcessorBundles();
      case TOPOLOGY:
        return listTopologyComponentTopologyBundles();
      case LINK:
        return listTopologyComponentLinkBundles();
      default:
        return respondEntity(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS),
            HttpStatus.BAD_REQUEST);
    }
  }

  @ApiOperation(value = "Add topology", notes = "Adds a topology.")
  @RequestMapping(value = "/topologies", method = RequestMethod.POST)
  public ResponseEntity addTopology(@RequestBody Topology topology) {
    Topology createdTopology = this.topologyTableManager.addTopology(topology);
    return respond(createdTopology, HttpStatus.OK);
  }

  @ApiOperation(value = "Add topology editor metadata", notes = "Adds metadata required to edit a topology.")
  @RequestMapping(value = "/system/topologyeditormetadata", method = RequestMethod.POST)
  public ResponseEntity addTopologyEditorMetadata(
      @RequestBody TopologyEditorMetadata topologyEditorMetadata) {
    TopologyEditorMetadata createdTopologyEditorMetadata = this.topologyTableManager
        .addTopologyEditorMetadata(topologyEditorMetadata);
    return respond(createdTopologyEditorMetadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Add or update topologyeditor metadata", notes = "Adds or updates metdata required to edit a topology.")
  @RequestMapping(value = "/system/topologyeditormetadata/{topologyId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologyEditorMetaData(
      @PathVariable("topologyId") Long topologyId,
      @RequestBody TopologyEditorMetadata metaData
  ) {
    metaData = this.topologyTableManager.addOrUpdateTopologyEditorMetadata(topologyId, metaData);
    return respond(metaData, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sources", notes = "Returns a list of sources in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/sources", method = RequestMethod.GET)
  public ResponseEntity listTopologySources(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologySource> sources = this.topologyTableManager.listSources(topologyId);
    return respondEntity(sources, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sources by version", notes = "Returns a list of sources in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sources", method = RequestMethod.GET)
  public ResponseEntity listTopologySourcesForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologySource> sources = this.topologyTableManager
        .listSources(topologyId);
    return respondEntity(sources, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology processors", notes = "Returns a list of processors in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/processors", method = RequestMethod.GET)
  public ResponseEntity listTopologyProcessors(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyProcessor> processors = this.topologyTableManager
        .listProcessors(topologyId);
    return respondEntity(processors, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology processors by version", notes = "Returns a list of processors in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/processors", method = RequestMethod.GET)
  public ResponseEntity listTopologyProcessorsForVersion(
      @PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologyProcessor> processors = this.topologyTableManager
        .listProcessors(topologyId);
    return respondEntity(processors, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sinks", notes = "Returns a list of sinks in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/sinks", method = RequestMethod.GET)
  public ResponseEntity listTopologySinks(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologySink> sinks = this.topologyTableManager.listSinks(topologyId);
    return respondEntity(sinks, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sinks by version", notes = "Returns a list of sinks in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sinks", method = RequestMethod.GET)
  public ResponseEntity listTopologySinksForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologySink> sinks = this.topologyTableManager.listSinks(topologyId);
    return respondEntity(sinks, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology edges", notes = "Returns a list of edges in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/edges", method = RequestMethod.GET)
  public ResponseEntity listTopologyEdges(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyEdge> edges = this.topologyTableManager.listTopologyEdges(topologyId);
    return respondEntity(edges, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology edges by version", notes = "Returns a list of edges in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/edges", method = RequestMethod.GET)
  public ResponseEntity listTopologyEdgesForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologyEdge> edges = this.topologyTableManager.listTopologyEdges(topologyId);
    return respondEntity(edges, HttpStatus.OK);
  }

  @ApiOperation(value = "Get editor meta data", notes = "Returns meta data required to edit a topology.")
  @RequestMapping(value = "/system/topologyeditormetadata/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyEditorMetadataByTopologyId(
      @PathVariable("topologyId") Long topologyId) {
    TopologyEditorMetadata metadata = this.topologyTableManager
        .getTopologyEditorMetadata(topologyId);
    return respond(metadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Get editor meta data by version", notes = "Returns meta data required to edit a topology.")
  @RequestMapping(value = "/system/versions/{versionId}/topologyeditormetadata/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyEditorMetadataByTopologyIdAndVersionId(
      @PathVariable("versionId") Long versionId,
      @PathVariable("topologyId") Long topologyId) {
    TopologyEditorMetadata metadata = this.topologyTableManager
        .getTopologyEditorMetadata(topologyId);
    return respond(metadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Get deployment state", notes = "Returns a deployment state of a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/deploymentstate", method = RequestMethod.GET)
  public ResponseEntity topologyDeploymentState(@PathVariable("topologyId") Long topologyId) {
    TopologyState state = new TopologyState();
    state.setTopologyId(topologyId);
    state.setName("TOPOLOGY_STATE_INITIAL");
    state.setDescription("Topology initialized.");
    return respond(state, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology editor toolbar", notes = "Returns a list of components for editor toolbar.")
  @RequestMapping(value = "/system/topologyeditortoolbar", method = RequestMethod.GET)
  public ResponseEntity listTopologyEditorToolbar() {
    TopologyEditorToolbar toolbar = this.topologyTableManager
        .getTopologyEditorToolbar();
    return respond(toolbar, HttpStatus.OK);
  }

  @ApiOperation(value = "Add or update topology editor toolbar", notes = "Adds or updates topology editor toolbar.")
  @RequestMapping(value = "/system/topologyeditortoolbar", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologyEditorToolbar(
      @RequestBody TopologyEditorToolbar toolbar) {
    toolbar = this.topologyTableManager.addOrUpdateTopologyEditorToolbar(toolbar);
    return respondEntity(toolbar, HttpStatus.OK);
  }

  @ApiOperation(value = "Add topology source", notes = "Adds a source to a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/sources", method = RequestMethod.POST)
  public ResponseEntity addTopologySource(@PathVariable("topologyId") Long topologyId,
      @RequestBody TopologySource topologySource) {
    topologySource = this.topologyTableManager.addTopologyComponent(topologyId, topologySource);
    return respond(topologySource, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get topology source by id", notes = "Returns topology source by id.")
  @RequestMapping(value = "/topologies/{topologyId}/sources/{sourceId}", method = RequestMethod.GET)
  public ResponseEntity getTopologySourceById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sourceId") Long sourceId) {
    TopologySource source = (TopologySource) this.topologyTableManager
        .getTopologyComponent(topologyId, sourceId);
    return respond(source, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology source by id and version", notes = "Returns topology source by id and its version")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sources/{sourceId}")
  public ResponseEntity getTopologySourceByIdAndVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("sourceId") Long sourceId) {
    TopologyComponent component = this.topologyTableManager
        .getTopologyComponent(topologyId, sourceId);
    return respond(component, HttpStatus.OK);
  }

  @ApiOperation(value = "Update topology source", notes = "Updates a topology source.")
  @RequestMapping(value = "/topologies/{topologyId}/sources/{sourceId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologySource(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sourceId") Long sourceId,
      @RequestBody TopologySource topologySource) {
    topologySource = this.topologyTableManager
        .addOrUpdateTopologyComponent(topologyId, sourceId, topologySource);
    return respond(topologySource, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove topology source", notes = "Removes a topology source.")
  @RequestMapping(value = "/topologies/{topologyId}/sources/{sourceId}", method = RequestMethod.DELETE)
  public ResponseEntity removeTopologySource(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sourceId") Long sourceId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    TopologySource removed = this.topologyTableManager
        .removeTopologyComponent(topologyId, sourceId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Add topology processor", notes = "Adds a processor to a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/processors", method = RequestMethod.POST)
  public ResponseEntity addTopologyProcessor(@PathVariable("topologyId") Long topologyId,
      @RequestBody TopologyProcessor topologyProcessor) {
    topologyProcessor = this.topologyTableManager
        .addTopologyComponent(topologyId, topologyProcessor);
    return respond(topologyProcessor, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get topology processor by id", notes = "Returns topology processor by id.")
  @RequestMapping(value = "/topologies/{topologyId}/processor/{processorId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyProcessorById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("processorId") Long processorId) {
    TopologyProcessor processor = (TopologyProcessor) this.topologyTableManager
        .getTopologyComponent(topologyId, processorId);
    return respond(processor, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology processor by id and version", notes = "Returns topology processor by id and its version")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/processors/{processorId}")
  public ResponseEntity getTopologyProcessorByIdAndVersion(
      @PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("processorId") Long processorId) {
    TopologyComponent component = this.topologyTableManager
        .getTopologyComponent(topologyId, processorId);
    return respond(component, HttpStatus.OK);
  }

  @ApiOperation(value = "Update topology processor", notes = "Updates a topology processor.")
  @RequestMapping(value = "/topologies/{topologyId}/processors/{processorId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologyProcessor(@PathVariable("topologyId") Long topologyId,
      @PathVariable("processorId") Long processorId,
      @RequestBody TopologyProcessor topologyProcessor) {
    topologyProcessor = this.topologyTableManager
        .addOrUpdateTopologyComponent(topologyId, processorId, topologyProcessor);
    return respond(topologyProcessor, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove topology processor", notes = "Removes a topology processor.")
  @RequestMapping(value = "/topologies/{topologyId}/processors/{processorId}", method = RequestMethod.DELETE)
  public ResponseEntity removeTopologyProcessor(@PathVariable("topologyId") Long topologyId,
      @PathVariable("processorId") Long processorId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    TopologyProcessor removed = this.topologyTableManager
        .removeTopologyComponent(topologyId, processorId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Add topology sink", notes = "Adds a sink to a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/sinks", method = RequestMethod.POST)
  public ResponseEntity addTopologySink(@PathVariable("topologyId") Long topologyId,
      @RequestBody TopologySink topologySink) {
    topologySink = this.topologyTableManager
        .addTopologyComponent(topologyId, topologySink);
    return respond(topologySink, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove topology sink", notes = "Removes a topology sink.")
  @RequestMapping(value = "/topologies/{topologyId}/sinks/{sinkId}", method = RequestMethod.DELETE)
  public ResponseEntity removeTopologySink(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sinkId") Long sinkId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    TopologySink removed = this.topologyTableManager
        .removeTopologyComponent(topologyId, sinkId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sink by id", notes = "Returns topology sink by id.")
  @RequestMapping(value = "/topologies/{topologyId}/sink/{sinkId}", method = RequestMethod.GET)
  public ResponseEntity getTopologySinkById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sinkId") Long sinkId) {
    TopologySink sink = (TopologySink) this.topologyTableManager
        .getTopologyComponent(topologyId, sinkId);
    return respond(sink, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sink by id and version", notes = "Returns topology sink by id and its version")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sinks/{sinkId}")
  public ResponseEntity getTopologySinkByIdAndVersion(
      @PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("sinkId") Long sinkId) {
    TopologyComponent component = this.topologyTableManager
        .getTopologyComponent(topologyId, sinkId);
    return respond(component, HttpStatus.OK);
  }

  @ApiOperation(value = "Update topology sink", notes = "Updates a topology sink.")
  @RequestMapping(value = "/topologies/{topologyId}/sinks/{sinkId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologySink(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sinkId") Long sinkId,
      @RequestBody TopologySink topologySink) {
    topologySink = this.topologyTableManager
        .addOrUpdateTopologyComponent(topologyId, sinkId, topologySink);
    return respond(topologySink, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Add topology edge", notes = "Adds an edge to a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/edges", method = RequestMethod.POST)
  public ResponseEntity addTopologyEdge(@PathVariable("topologyId") Long topologyId,
      @RequestBody TopologyEdge topologyEdge) {
    topologyEdge = this.topologyTableManager
        .addTopologyEdge(topologyId, topologyEdge);
    return respond(topologyEdge, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get topology edge by id", notes = "Returns topology edge by id.")
  @RequestMapping(value = "/topologies/{topologyId}/edges/{edgeId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyEdgeById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("edgeId") Long edgeId) {
    TopologyEdge edge = this.topologyTableManager.getTopologyEdge(topologyId, edgeId);
    return respond(edge, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology edge by id and version", notes = "Returns topology edge by id and its version")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/edges/{edgeId}")
  public ResponseEntity getTopologyEdgeByIdAndVersion(
      @PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("edgeId") Long edgeId) {
    TopologyEdge edge = this.topologyTableManager.getTopologyEdge(topologyId, edgeId);
    return respond(edge, HttpStatus.OK);
  }

  @ApiOperation(value = "Update topology edge", notes = "Updates a topology edge.")
  @RequestMapping(value = "/topologies/{topologyId}/edges/{edgeId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologyEdge(@PathVariable("topologyId") Long topologyId,
      @PathVariable("edgeId") Long edgeId,
      @RequestBody TopologyEdge topologyEdge) {
    topologyEdge = this.topologyTableManager
        .addOrUpdateTopologyEdge(topologyId, edgeId, topologyEdge);
    return respond(topologyEdge, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove topology edge", notes = "Removes a topology edge.")
  @RequestMapping(value = "/topologies/{topologyId}/edges/{edgeId}", method = RequestMethod.DELETE)
  public ResponseEntity removeTopologyEdge(@PathVariable("topologyId") Long topologyId,
      @PathVariable("edgeId") Long edgeId) {
    TopologyEdge removed = this.topologyTableManager.removeTopologyEdge(topologyId, edgeId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Get field hints", notes = "Returns field hints of a component.")
  @RequestMapping(value = "/streams/componentbundles/{component}/{id}/hints/namespaces/{namespaceId}", method = RequestMethod.GET)
  public ResponseEntity getFieldHints(
      @PathVariable("component") TopologyComponentType componentType,
      @PathVariable("id") Long id, @PathVariable("namespaceId") Long namespaceId) {
    return respond("{}", HttpStatus.OK);
  }

  @ApiOperation(value = "Reconfigure", notes = "Returns whether a component needs to be reconfigured or not.")
  @RequestMapping(value = "/topologies/{topologyId}/reconfigure", method = RequestMethod.GET)
  public ResponseEntity getComponentsToReconfigure(@PathVariable("topologyId") Long topologyId) {
    // Nothing to reconfigure, always.
    return respond("{\"BRANCH\":[],\"PROCESSOR\":[],\"SINK\":[],\"RULE\":[],\"WINDOW\":[]}",
        HttpStatus.OK);
  }

  @ApiOperation(value = "Get notifiers", notes = "Returns a list of available notifiers.")
  @RequestMapping(value = "/notifiers", method = RequestMethod.GET)
  public ResponseEntity listNotifiers() {
    // returns an empty list
    return respondEntity(new JsonArray(), HttpStatus.OK);
  }

  @ApiOperation(value = "Get streams by version", notes = "Returns a list of streams for a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/streams", method = RequestMethod.GET)
  public ResponseEntity listStreamInfosForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologyStream> streams = this.topologyTableManager
        .listTopologyStreams(topologyId);
    return respondEntity(streams, HttpStatus.OK);
  }

  @ApiOperation(value = "Get streams", notes = "Returns a list of streams for topology.")
  @RequestMapping(value = "/topologies/{topologyId}/streams", method = RequestMethod.GET)
  public ResponseEntity listStreamInfos(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyStream> streams = this.topologyTableManager.listTopologyStreams(topologyId);
    return respondEntity(streams, HttpStatus.OK);
  }

  @ApiOperation(value = "Get stream", notes = "Returns a stream info.")
  @RequestMapping(value = "/topologies/{topologyId}/streams/{streamId}", method = RequestMethod.GET)
  public ResponseEntity getStreamInfoById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("streamId") Long streamId) {
    TopologyStream stream = this.topologyTableManager.getTopologyStream(topologyId, streamId);
    return respond(stream, HttpStatus.OK);
  }

  @ApiOperation(value = "Get stream by version", notes = "Returns a stream info.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/streams/{streamId}", method = RequestMethod.GET)
  public ResponseEntity getStreamInfoById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("streamId") Long streamId) {
    TopologyStream stream = this.topologyTableManager
        .getTopologyStream(topologyId, streamId);
    return respond(stream, HttpStatus.OK);
  }

  @ApiOperation(value = "Remove stream by id", notes = "Removes a stream")
  @RequestMapping(value = "/topologies/{topologyId}/streams/{streamId}", method = RequestMethod.DELETE)
  public ResponseEntity removeStreamInfo(@PathVariable("topologyId") Long topologyId,
      @PathVariable("streamId") Long streamId) {
    TopologyStream removed = this.topologyTableManager.removeTopologyStream(topologyId, streamId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Get storm URL", notes = "Returns URL running storm.")
  @RequestMapping(value = "/clusters/{clusterId}/services/storm/mainpage/url", method = RequestMethod.GET)
  public ResponseEntity getMainPageByClusterId(@PathVariable("clusterId") Long clusterId) {
    JsonObject obj = new JsonObject();
    obj.addProperty("url", "http://localhost:9090");
    return respond(obj, HttpStatus.OK);
  }

  @ApiOperation(value = "Get clusters", notes = "Returns a list of clusters.")
  @RequestMapping(value = "/clusters", method = RequestMethod.GET)
  public ResponseEntity listClusters(
      @RequestParam(value = "detail", required = false) Boolean detail) {
    Collection<ClusterWithService> clusterWithServices = this.topologyTableManager
        .listClusterWithServices();
    return respondEntity(clusterWithServices, HttpStatus.OK);
  }

  @ApiOperation(value = "Get cluster by id", notes = "Returns a cluster by id.")
  @RequestMapping(value = "/clusters/{clusterId}", method = RequestMethod.GET)
  public ResponseEntity getClusterById(@PathVariable("clusterId") Long clusterId,
      @RequestParam(value = "detail", required = false) Boolean detail) {
    ClusterWithService cs = this.topologyTableManager.listClusterWithServices().iterator().next();
    return respond(cs, HttpStatus.OK);
  }

  @ApiOperation(value = "Validate topology", notes = "Validates a topology")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/actions/validate", method = RequestMethod.POST)
  public ResponseEntity validateTopology(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Topology result = this.topologyTableManager.getTopology(topologyId);
    return respond(result, HttpStatus.OK);
  }

  @ApiOperation(value = "Deploy topology", notes = "Deploys a topology")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/actions/deploy", method = RequestMethod.POST)
  public ResponseEntity deployTopology(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Topology result = this.topologyTableManager.getTopology(topologyId);

    // flink result
    TopologyData topologyData = this.topologyTableManager.doExportTopology(result);

    LOGGER.info("TopologyData: " + topologyData.getConfigStr());

    // Create
    TopologyJobGroup jobGroup = TopologyJobGroup.create(topologyData);
    // write to database
    TopologyJobTableManager.getInstance().addOrUpdateTopologyJobGroup(jobGroup);

    List<String> targetHosts = new ArrayList<>();
    targetHosts.add((String) topologyData.getConfig().get("targetHost"));
    for (String targetHost : targetHosts) {
      String[] splits = targetHost.split(":");
      FlinkEngine engine = new FlinkEngine(splits[0], Integer.parseInt(splits[1]));
      String engineId = engine.createJob(topologyData);
      TopologyJob job = TopologyJob.create(jobGroup.getId());
      job.setEngineId(engineId);
      // job.setData(targetHost); // ???
      jobGroup.addJob(job);

      // Write to database
      TopologyJobTableManager.getInstance().addOrUpdateTopologyJob(job);

      // Run
      try {
        engine.run(job.getEngineId());
        job.getState().setState("RUNNING");
        job.getState().setStartTime(System.currentTimeMillis());
      } catch (Exception e) {
        job.getState().setState("ERROR");
      }
      TopologyJobTableManager.getInstance().addOrUpdateTopologyJobState(jobGroup.getId(),
          job.getId(), job.getState());
    }

    return respond(result, HttpStatus.OK);
  }

  @ApiOperation(value = "Export topology", notes = "Exports a topology")
  @RequestMapping(value = "/topologies/{topologyId}/actions/export", method = RequestMethod.GET)
  public ResponseEntity exportTopology(@PathVariable("topologyId") Long topologyId) {
    try {
      Topology topology = this.topologyTableManager.getTopology(topologyId);
      String exportedTopology = this.topologyTableManager.exportTopology(topology);
      return respond(exportedTopology, HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ApiOperation(value = "Import topology", notes = "Imports a topology")
  @RequestMapping(value = "/topologies/actions/import", method = RequestMethod.POST)
  public ResponseEntity importTopology(@RequestParam("file") Part part,
      @RequestParam("namespaceId") final Long namespaceId,
      @RequestParam("topologyName") final String topologyName) {
    try {
      TopologyData topologyData = new ObjectMapper()
          .readValue(part.getInputStream(), TopologyData.class);
      Topology imported = this.topologyTableManager
          .importTopology(topologyName, topologyData);
      return respond(imported, HttpStatus.OK);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ResponseEntity listTopologyComponentTopologyBundles() {
    // TEMP
    Collection<TopologyComponentBundle> topologies
        = this.topologyTableManager
        .listTopologyComponentBundles(TopologyComponentBundle.TopologyComponentType.TOPOLOGY);
    return respondEntity(topologies, HttpStatus.OK);
  }

  private ResponseEntity listTopologyComponentLinkBundles() {
    // TEMP
    Collection<TopologyComponentBundle> topologies
        = this.topologyTableManager
        .listTopologyComponentBundles(TopologyComponentBundle.TopologyComponentType.LINK);
    return respondEntity(topologies, HttpStatus.OK);
  }

  private ResponseEntity listTopologyComponentProcessorBundles() {
    // TEMP
    Collection<TopologyComponentBundle> processors
        = this.topologyTableManager
        .listTopologyComponentBundles(TopologyComponentBundle.TopologyComponentType.PROCESSOR);
    return respondEntity(processors, HttpStatus.OK);
  }

  private ResponseEntity listTopologyComponentSinkBundles() {
    // TEMP
    Collection<TopologyComponentBundle> sinks
        = this.topologyTableManager
        .listTopologyComponentBundles(TopologyComponentBundle.TopologyComponentType.SINK);
    return respondEntity(sinks, HttpStatus.OK);
  }

  private ResponseEntity listTopologyComponentSourceBundles() {
    // TEMP
    Collection<TopologyComponentBundle> sources
        = this.topologyTableManager
        .listTopologyComponentBundles(TopologyComponentBundle.TopologyComponentType.SOURCE);
    return respondEntity(sources, HttpStatus.OK);
  }

  private static final JsonParser jsonParser = new JsonParser();

  private static ResponseEntity respondEntity(Object obj, HttpStatus httpStatus) {
    JsonElement entity = jsonParser.parse(obj.toString());
    JsonObject parent = new JsonObject();
    parent.add("entities", entity);
    return respond(parent, httpStatus);
  }

  private static ResponseEntity respond(Object obj, HttpStatus httpStatus) {
    return ResponseEntity
        .status(httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(obj.toString());
  }
}
