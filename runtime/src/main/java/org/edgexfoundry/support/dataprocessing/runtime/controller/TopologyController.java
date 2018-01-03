package org.edgexfoundry.support.dataprocessing.runtime.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Namespace;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.Topology;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponent;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyDetailed;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologySource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyState;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyVersion;
import org.edgexfoundry.support.dataprocessing.runtime.db.TopologyTableManager;
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
    this.topologyTableManager = new TopologyTableManager();
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

  @ApiOperation(value = "Get topology by Id", notes = "Returns topology detail by Id.")
  @RequestMapping(value = "/topologies/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyById(@PathVariable("topologyId") Long topologyId,
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Topology topology = this.topologyTableManager.getTopology(topologyId);
    if (topology == null) {
      return respondEntity(new ErrorFormat(ErrorType.DPFW_ERROR_DB, "Topology does not exist."),
          HttpStatus.NOT_FOUND);
    }

    // Enrich topology
    TopologyDetailed detailed = new TopologyDetailed();
    detailed.setTopology(topology);
    detailed.setNamespaceName("Dover");
    detailed.setRunning(TopologyDetailed.TopologyRunningStatus.NOT_RUNNING);
    return respond(detailed, HttpStatus.OK);
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
    return respondEntity(namespaces.iterator().next(), HttpStatus.OK);
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
    Collection<TopologySource> sources = this.topologyTableManager.listSources(topologyId, 1L);
    return respondEntity(sources, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sources by version", notes = "Returns a list of sources in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sources", method = RequestMethod.GET)
  public ResponseEntity listTopologySourcesForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologySource> sources = this.topologyTableManager
        .listSources(topologyId, versionId);
    return respondEntity(sources, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology processors", notes = "Returns a list of processors in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/processors", method = RequestMethod.GET)
  public ResponseEntity listTopologyProcessors(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyProcessor> processors = this.topologyTableManager
        .listProcessors(topologyId, 1L);
    return respondEntity(processors, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology processors by version", notes = "Returns a list of processors in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/processors", method = RequestMethod.GET)
  public ResponseEntity listTopologyProcessorsForVersion(
      @PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologyProcessor> processors = this.topologyTableManager
        .listProcessors(topologyId, versionId);
    return respondEntity(processors, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sinks", notes = "Returns a list of sinks in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/sinks", method = RequestMethod.GET)
  public ResponseEntity listTopologySinks(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologySink> sinks = this.topologyTableManager.listSinks(topologyId, 1L);
    return respondEntity(sinks, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology sinks by version", notes = "Returns a list of sinks in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sinks", method = RequestMethod.GET)
  public ResponseEntity listTopologySinksForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologySink> sinks = this.topologyTableManager.listSinks(topologyId, versionId);
    return respondEntity(sinks, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology edges", notes = "Returns a list of edges in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/edges", method = RequestMethod.GET)
  public ResponseEntity listTopologyEdges(@PathVariable("topologyId") Long topologyId) {
    Collection<TopologyEdge> edges = this.topologyTableManager.listEdges(topologyId, 1L);
    return respondEntity(edges, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology edges by version", notes = "Returns a list of edges in use in a topology.")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/edges", method = RequestMethod.GET)
  public ResponseEntity listTopologyEdgesForVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId) {
    Collection<TopologyEdge> edges = this.topologyTableManager.listEdges(topologyId, versionId);
    return respondEntity(edges, HttpStatus.OK);
  }

  @ApiOperation(value = "Get editor meta data", notes = "Returns meta data required to edit a topology.")
  @RequestMapping(value = "/system/topologyeditormetadata/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyEditorMetadataByTopologyId(
      @PathVariable("topologyId") Long topologyId) {
    TopologyEditorMetadata metadata = this.topologyTableManager
        .getTopologyEditorMetadata(topologyId, 1L);
    return respond(metadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Get editor meta data by version", notes = "Returns meta data required to edit a topology.")
  @RequestMapping(value = "/system/versions/{versionId}/topologyeditormetadata/{topologyId}", method = RequestMethod.GET)
  public ResponseEntity getTopologyEditorMetadataByTopologyIdAndVersionId(
      @PathVariable("versionId") Long versionId,
      @PathVariable("topologyId") Long topologyId) {
    TopologyEditorMetadata metadata = this.topologyTableManager
        .getTopologyEditorMetadata(topologyId, versionId);
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
    topologySource = this.topologyTableManager.addTopologySource(topologyId, 1L, topologySource);
    return respond(topologySource, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get topology source by id", notes = "Returns topology source by id.")
  @RequestMapping(value = "/topologies/{topologyId}/sources/{sourceId}", method = RequestMethod.GET)
  public ResponseEntity getTopologySourceById(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sourceId") Long sourceId) {
    TopologySource source = (TopologySource) this.topologyTableManager
        .getTopologyComponent(topologyId, 1L, sourceId);
    return respond(source, HttpStatus.OK);
  }

  @ApiOperation(value = "Get topology source by id and version", notes = "Returns topology source by id and its version")
  @RequestMapping(value = "/topologies/{topologyId}/versions/{versionId}/sources/{sourceId}")
  public ResponseEntity getTopologySourceByIdAndVersion(@PathVariable("topologyId") Long topologyId,
      @PathVariable("versionId") Long versionId,
      @PathVariable("sourceId") Long sourceId) {
    TopologyComponent component = this.topologyTableManager
        .getTopologyComponent(topologyId, versionId, sourceId);
    return respond(component, HttpStatus.OK);
  }

  @ApiOperation(value = "Update topology source", notes = "Updates a topology source.")
  @RequestMapping(value = "/topologies/{topologyId}/sources/{sourceId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateTopologySource(@PathVariable("topologyId") Long topologyId,
      @PathVariable("sourceId") Long sourceId,
      @RequestBody TopologySource topologySource) {
    topologySource = this.topologyTableManager
        .addOrUpdateTopologySource(topologyId, sourceId, topologySource);
    return respond(topologySource, HttpStatus.CREATED);
  }


  @ApiOperation(value = "Get field hints", notes = "Returns field hints of a component.")
  @RequestMapping(value = "/streams/componentbundles/{component}/{id}/hints/namespaces/{namespaceId}", method = RequestMethod.GET)
  public ResponseEntity getFieldHints(
      @PathVariable("component") TopologyComponentType componentType,
      @PathVariable("id") Long id, @PathVariable("namespaceId") Long namespaceId) {
    return respondEntity("{}", HttpStatus.OK);
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
