package org.edgexfoundry.support.dataprocessing.runtime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.Part;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.EngineTypeResponse;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowDetailed;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowDetailed.RunningStatus;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEdge;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorMetadata;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowEditorToolbar;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSource;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowStream;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.pharos.EdgeInfo;
import org.springframework.http.HttpStatus;
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
@Api(tags = "Workflow UI", description = "API List for Workflow UI")
@RequestMapping("/api/v1/catalog")
public class WorkflowController extends AbstractController {

  private WorkflowTableManager workflowTableManager = null;
  private EdgeInfo edgeInfo = null;

  public WorkflowController() {
    this.workflowTableManager = WorkflowTableManager.getInstance();
    this.edgeInfo = new EdgeInfo();
  }

  @ApiOperation(value = "Get workflows", notes = "Returns a list of all workflows.")
  @RequestMapping(value = "/workflows", method = RequestMethod.GET)
  public ResponseEntity listWorkflows(
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "sort", required = false) String sortType,
      @RequestParam(value = "ascending", required = false) Boolean ascending,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Collection<Workflow> workflows = this.workflowTableManager.listWorkflows();
    Collection<WorkflowDetailed> workflowDetailedList = new ArrayList<>();
    for (Workflow workflow : workflows) {
      WorkflowDetailed detailed = new WorkflowDetailed();
      detailed.setWorkflow(workflow);
      detailed.setRunning(RunningStatus.NOT_RUNNING);
      workflowDetailedList.add(detailed);
    }
    return respondEntity(workflowDetailedList, HttpStatus.OK);
  }

  @ApiOperation(value = "Save workflow", notes = "Saves workflow detail.")
  @RequestMapping(value = "/workflows/{workflowId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflow(@PathVariable("workflowId") Long workflowId,
      @RequestBody Workflow workflow) {
    workflow = this.workflowTableManager.addOrUpdateWorkflow(workflowId, workflow);
    return respond(workflow, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow by Id", notes = "Returns workflow detail by Id.")
  @RequestMapping(value = "/workflows/{workflowId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowById(@PathVariable("workflowId") Long workflowId,
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Workflow workflow = this.workflowTableManager.getWorkflow(workflowId);
    if (workflow == null) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, "Workflow does not exist."),
          HttpStatus.NOT_FOUND);
    }

    if (detail != null && detail) {
      // Enrich workflow
      WorkflowDetailed detailed = new WorkflowDetailed();
      detailed.setWorkflow(workflow);
      detailed.setRunning(RunningStatus.NOT_RUNNING);
      return respond(detailed, HttpStatus.OK);
    } else {
      return respond(workflow, HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Remove workflow", notes = "Removes a workflow")
  @RequestMapping(value = "/workflows/{workflowId}", method = RequestMethod.DELETE)
  public ResponseEntity removeWorkflow(@PathVariable("workflowId") Long workflowId,
      @RequestParam(value = "onlyCurrent", required = false) boolean onlyCurrent,
      @RequestParam(value = "force", required = false) boolean force) {
    Workflow workflow = this.workflowTableManager.removeWorkflow(workflowId);
    return respond(workflow, HttpStatus.OK);
  }

  @ApiOperation(value = "Get component bundles", notes = "Returns a list of bundles by component.")
  @RequestMapping(value = "/streams/componentbundles/{component}", method = RequestMethod.GET)
  public ResponseEntity listWorkflowComponentBundles(
      @PathVariable("component") WorkflowComponentBundleType componentType
  ) {
    switch (componentType) {
      case SOURCE:
        return listWorkflowComponentSourceBundles();
      case SINK:
        return listWorkflowComponentSinkBundles();
      case PROCESSOR:
        return listWorkflowComponentProcessorBundles();
      case WORKFLOW:
        return listWorkflowComponentWorkflowBundles();
      case LINK:
        return listWorkflowComponentLinkBundles();
      default:
        return respondEntity(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS),
            HttpStatus.BAD_REQUEST);
    }
  }

  @ApiOperation(value = "Add workflow", notes = "Adds a workflow.")
  @RequestMapping(value = "/workflows", method = RequestMethod.POST)
  public ResponseEntity addWorkflow(@RequestBody Workflow workflow) {
    Workflow createdWorkflow = this.workflowTableManager.addWorkflow(workflow);
    return respond(createdWorkflow, HttpStatus.OK);
  }

  @ApiOperation(value = "Search workflow", notes = "Searches workflow.")
  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public ResponseEntity searchWorkflow(
      @RequestParam(value = "sort", required = false) String sortType,
      @RequestParam(value = "desc", required = false) Boolean desc,
      @RequestParam(value = "namespace", required = false) String namespace,
      @RequestParam(value = "queryString", required = false) String queryString,
      @RequestParam(value = "detail", required = false) Boolean detail,
      @RequestParam(value = "latencyTopN", required = false) Integer latencyTopN) {
    Collection<Workflow> workflows = this.workflowTableManager.listWorkflows();
    Collection<WorkflowDetailed> workflowDetails = workflows.stream()
        .filter(workflow -> { // filter by query string
          if (queryString != null) {
            return workflow.getName().toLowerCase().contains(queryString.toLowerCase());
          } else {
            return true;
          }
        })
        .sorted((o1, o2) -> { // sort
          if (sortType == null || sortType.equals("name")) {
            return o1.getName().compareTo(o2.getName());
          } else {
            return o1.getId().compareTo(o2.getId());
          }
        })
        .map(workflow -> { // map
          WorkflowDetailed detailed = new WorkflowDetailed();
          detailed.setWorkflow(workflow);
          detailed.setRunning(RunningStatus.UNKNOWN);
          return detailed;
        })
        .collect(Collectors.toSet());
    return respondEntity(workflowDetails, HttpStatus.OK);
  }

  @ApiOperation(value = "Add workflow editor metadata", notes = "Adds metadata required to edit a workflow.")
  @RequestMapping(value = "/system/workfloweditormetadata", method = RequestMethod.POST)
  public ResponseEntity addWorkflowEditorMetadata(
      @RequestBody WorkflowEditorMetadata workflowEditorMetadata) {
    WorkflowEditorMetadata createdWorkflowEditorMetadata = this.workflowTableManager
        .addWorkflowEditorMetadata(workflowEditorMetadata);
    return respond(createdWorkflowEditorMetadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Add or update workfloweditor metadata", notes = "Adds or updates metdata required to edit a workflow.")
  @RequestMapping(value = "/system/workfloweditormetadata/{workflowId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowEditorMetaData(
      @PathVariable("workflowId") Long workflowId,
      @RequestBody WorkflowEditorMetadata metaData
  ) {
    metaData = this.workflowTableManager.addOrUpdateWorkflowEditorMetadata(workflowId, metaData);
    return respond(metaData, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow sources", notes = "Returns a list of sources in use in a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/sources", method = RequestMethod.GET)
  public ResponseEntity listWorkflowSources(@PathVariable("workflowId") Long workflowId) {
    Collection<WorkflowSource> sources = this.workflowTableManager.listSources(workflowId);
    return respondEntity(sources, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow processors", notes = "Returns a list of processors in use in a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/processors", method = RequestMethod.GET)
  public ResponseEntity listWorkflowProcessors(@PathVariable("workflowId") Long workflowId) {
    Collection<WorkflowProcessor> processors = this.workflowTableManager
        .listProcessors(workflowId);
    return respondEntity(processors, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow sinks", notes = "Returns a list of sinks in use in a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/sinks", method = RequestMethod.GET)
  public ResponseEntity listWorkflowSinks(@PathVariable("workflowId") Long workflowId) {
    Collection<WorkflowSink> sinks = this.workflowTableManager.listSinks(workflowId);
    return respondEntity(sinks, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow edges", notes = "Returns a list of edges in use in a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/edges", method = RequestMethod.GET)
  public ResponseEntity listWorkflowEdges(@PathVariable("workflowId") Long workflowId) {
    Collection<WorkflowEdge> edges = this.workflowTableManager.listWorkflowEdges(workflowId);
    return respondEntity(edges, HttpStatus.OK);
  }

  @ApiOperation(value = "Get editor meta data", notes = "Returns meta data required to edit a workflow.")
  @RequestMapping(value = "/system/workfloweditormetadata/{workflowId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowEditorMetadataByWorkflowId(
      @PathVariable("workflowId") Long workflowId) {
    WorkflowEditorMetadata metadata = this.workflowTableManager
        .getWorkflowEditorMetadata(workflowId);
    return respond(metadata, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow editor toolbar", notes = "Returns a list of components for editor toolbar.")
  @RequestMapping(value = "/system/workfloweditortoolbar", method = RequestMethod.GET)
  public ResponseEntity listWorkflowEditorToolbar() {
    WorkflowEditorToolbar toolbar = this.workflowTableManager
        .getWorkflowEditorToolbar();
    return respond(toolbar, HttpStatus.OK);
  }

  @ApiOperation(value = "Add or update workflow editor toolbar", notes = "Adds or updates workflow editor toolbar.")
  @RequestMapping(value = "/system/workfloweditortoolbar", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowEditorToolbar(
      @RequestBody WorkflowEditorToolbar toolbar) {
    toolbar = this.workflowTableManager.addOrUpdateWorkflowEditorToolbar(toolbar);
    return respondEntity(toolbar, HttpStatus.OK);
  }

  @ApiOperation(value = "Add workflow source", notes = "Adds a source to a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/sources", method = RequestMethod.POST)
  public ResponseEntity addWorkflowSource(@PathVariable("workflowId") Long workflowId,
      @RequestBody WorkflowSource workflowSource) {
    workflowSource = this.workflowTableManager.addWorkflowComponent(workflowId, workflowSource);
    return respond(workflowSource, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get workflow source by id", notes = "Returns workflow source by id.")
  @RequestMapping(value = "/workflows/{workflowId}/sources/{sourceId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowSourceById(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sourceId") Long sourceId) {
    WorkflowSource source = (WorkflowSource) this.workflowTableManager
        .getWorkflowComponent(workflowId, sourceId);
    return respond(source, HttpStatus.OK);
  }

  @ApiOperation(value = "Update workflow source", notes = "Updates a workflow source.")
  @RequestMapping(value = "/workflows/{workflowId}/sources/{sourceId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowSource(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sourceId") Long sourceId,
      @RequestBody WorkflowSource workflowSource) {
    workflowSource = this.workflowTableManager
        .addOrUpdateWorkflowComponent(workflowId, sourceId, workflowSource);
    return respond(workflowSource, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove workflow source", notes = "Removes a workflow source.")
  @RequestMapping(value = "/workflows/{workflowId}/sources/{sourceId}", method = RequestMethod.DELETE)
  public ResponseEntity removeWorkflowSource(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sourceId") Long sourceId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    WorkflowSource removed = this.workflowTableManager
        .removeWorkflowComponent(workflowId, sourceId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Add workflow processor", notes = "Adds a processor to a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/processors", method = RequestMethod.POST)
  public ResponseEntity addWorkflowProcessor(@PathVariable("workflowId") Long workflowId,
      @RequestBody WorkflowProcessor workflowProcessor) {
    workflowProcessor = this.workflowTableManager
        .addWorkflowComponent(workflowId, workflowProcessor);
    return respond(workflowProcessor, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get workflow processor by id", notes = "Returns workflow processor by id.")
  @RequestMapping(value = "/workflows/{workflowId}/processors/{processorId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowProcessorById(@PathVariable("workflowId") Long workflowId,
      @PathVariable("processorId") Long processorId) {
    WorkflowProcessor processor = (WorkflowProcessor) this.workflowTableManager
        .getWorkflowComponent(workflowId, processorId);
    return respond(processor, HttpStatus.OK);
  }

  @ApiOperation(value = "Update workflow processor", notes = "Updates a workflow processor.")
  @RequestMapping(value = "/workflows/{workflowId}/processors/{processorId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowProcessor(@PathVariable("workflowId") Long workflowId,
      @PathVariable("processorId") Long processorId,
      @RequestBody WorkflowProcessor workflowProcessor) {
    workflowProcessor = this.workflowTableManager
        .addOrUpdateWorkflowComponent(workflowId, processorId, workflowProcessor);
    return respond(workflowProcessor, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove workflow processor", notes = "Removes a workflow processor.")
  @RequestMapping(value = "/workflows/{workflowId}/processors/{processorId}", method = RequestMethod.DELETE)
  public ResponseEntity removeWorkflowProcessor(@PathVariable("workflowId") Long workflowId,
      @PathVariable("processorId") Long processorId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    WorkflowProcessor removed = this.workflowTableManager
        .removeWorkflowComponent(workflowId, processorId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Add workflow sink", notes = "Adds a sink to a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/sinks", method = RequestMethod.POST)
  public ResponseEntity addWorkflowSink(@PathVariable("workflowId") Long workflowId,
      @RequestBody WorkflowSink workflowSink) {
    workflowSink = this.workflowTableManager
        .addWorkflowComponent(workflowId, workflowSink);
    return respond(workflowSink, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove workflow sink", notes = "Removes a workflow sink.")
  @RequestMapping(value = "/workflows/{workflowId}/sinks/{sinkId}", method = RequestMethod.DELETE)
  public ResponseEntity removeWorkflowSink(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sinkId") Long sinkId,
      @RequestParam(value = "removeEdges", required = false) boolean removeEdges) {
    WorkflowSink removed = this.workflowTableManager
        .removeWorkflowComponent(workflowId, sinkId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Get workflow sink by id", notes = "Returns workflow sink by id.")
  @RequestMapping(value = "/workflows/{workflowId}/sinks/{sinkId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowSinkById(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sinkId") Long sinkId) {
    WorkflowSink sink = (WorkflowSink) this.workflowTableManager
        .getWorkflowComponent(workflowId, sinkId);
    return respond(sink, HttpStatus.OK);
  }

  @ApiOperation(value = "Update workflow sink", notes = "Updates a workflow sink.")
  @RequestMapping(value = "/workflows/{workflowId}/sinks/{sinkId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowSink(@PathVariable("workflowId") Long workflowId,
      @PathVariable("sinkId") Long sinkId,
      @RequestBody WorkflowSink workflowSink) {
    workflowSink = this.workflowTableManager
        .addOrUpdateWorkflowComponent(workflowId, sinkId, workflowSink);
    return respond(workflowSink, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Add workflow edge", notes = "Adds an edge to a workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/edges", method = RequestMethod.POST)
  public ResponseEntity addWorkflowEdge(@PathVariable("workflowId") Long workflowId,
      @RequestBody WorkflowEdge workflowEdge) {
    workflowEdge = this.workflowTableManager
        .addWorkflowEdge(workflowId, workflowEdge);
    return respond(workflowEdge, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Get workflow edge by id", notes = "Returns workflow edge by id.")
  @RequestMapping(value = "/workflows/{workflowId}/edges/{edgeId}", method = RequestMethod.GET)
  public ResponseEntity getWorkflowEdgeById(@PathVariable("workflowId") Long workflowId,
      @PathVariable("edgeId") Long edgeId) {
    WorkflowEdge edge = this.workflowTableManager.getWorkflowEdge(workflowId, edgeId);
    return respond(edge, HttpStatus.OK);
  }

  @ApiOperation(value = "Update workflow edge", notes = "Updates a workflow edge.")
  @RequestMapping(value = "/workflows/{workflowId}/edges/{edgeId}", method = RequestMethod.PUT)
  public ResponseEntity addOrUpdateWorkflowEdge(@PathVariable("workflowId") Long workflowId,
      @PathVariable("edgeId") Long edgeId,
      @RequestBody WorkflowEdge workflowEdge) {
    workflowEdge = this.workflowTableManager
        .addOrUpdateWorkflowEdge(workflowId, edgeId, workflowEdge);
    return respond(workflowEdge, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Remove workflow edge", notes = "Removes a workflow edge.")
  @RequestMapping(value = "/workflows/{workflowId}/edges/{edgeId}", method = RequestMethod.DELETE)
  public ResponseEntity removeWorkflowEdge(@PathVariable("workflowId") Long workflowId,
      @PathVariable("edgeId") Long edgeId) {
    WorkflowEdge removed = this.workflowTableManager.removeWorkflowEdge(workflowId, edgeId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Get streams", notes = "Returns a list of streams for workflow.")
  @RequestMapping(value = "/workflows/{workflowId}/streams", method = RequestMethod.GET)
  public ResponseEntity listStreamInfos(@PathVariable("workflowId") Long workflowId) {
    Collection<WorkflowStream> streams = this.workflowTableManager.listWorkflowStreams(workflowId);
    return respondEntity(streams, HttpStatus.OK);
  }

  @ApiOperation(value = "Get stream", notes = "Returns a stream info.")
  @RequestMapping(value = "/workflows/{workflowId}/streams/{streamId}", method = RequestMethod.GET)
  public ResponseEntity getStreamInfoById(@PathVariable("workflowId") Long workflowId,
      @PathVariable("streamId") Long streamId) {
    WorkflowStream stream = this.workflowTableManager.getWorkflowStream(workflowId, streamId);
    return respond(stream, HttpStatus.OK);
  }

  @ApiOperation(value = "Remove stream by id", notes = "Removes a stream")
  @RequestMapping(value = "/workflows/{workflowId}/streams/{streamId}", method = RequestMethod.DELETE)
  public ResponseEntity removeStreamInfo(@PathVariable("workflowId") Long workflowId,
      @PathVariable("streamId") Long streamId) {
    WorkflowStream removed = this.workflowTableManager.removeWorkflowStream(workflowId, streamId);
    return respond(removed, HttpStatus.OK);
  }

  @ApiOperation(value = "Check workflow whether unique engine type", notes = "Check workflow whether unique engine type")
  @RequestMapping(value = "/workflows/{workflowId}/actions/enginetype", method = RequestMethod.GET)
  public ResponseEntity checkEngineType(@PathVariable("workflowId") Long workflowId) {
    Workflow result = this.workflowTableManager.getWorkflow(workflowId);
    WorkflowData workflowData = this.workflowTableManager.doExportWorkflow(result);
    WorkflowData.EngineType engineType = workflowData.getEngineType();

    if (engineType == WorkflowData.EngineType.FLINK
        || engineType == WorkflowData.EngineType.KAPACITOR) {
      EngineTypeResponse engineTypeResponse = null;
      if (engineType == WorkflowData.EngineType.FLINK) {
        engineTypeResponse = new EngineTypeResponse("flink");
      } else {
        engineTypeResponse = new EngineTypeResponse("kapacitor");
      }
      return respond(engineTypeResponse, HttpStatus.OK);
    } else {
      return respond(
          new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_TYPE,
              "Query and Algorithm task can not be in the same workflow"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ApiOperation(value = "Export workflow", notes = "Exports a workflow")
  @RequestMapping(value = "/workflows/{workflowId}/actions/export", method = RequestMethod.GET)
  public ResponseEntity exportWorkflow(@PathVariable("workflowId") Long workflowId) {
    try {
      Workflow workflow = this.workflowTableManager.getWorkflow(workflowId);
      String exportedWorkflow = this.workflowTableManager.exportWorkflow(workflow);
      return respond(exportedWorkflow, HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ApiOperation(value = "Import workflow", notes = "Imports a workflow")
  @RequestMapping(value = "/workflows/actions/import", method = RequestMethod.POST)
  public ResponseEntity importWorkflow(@RequestParam("file") Part part,
      @RequestParam("workflowName") final String workflowName) {
    try {
      WorkflowData workflowData = new ObjectMapper()
          .readValue(part.getInputStream(), WorkflowData.class);
      Workflow imported = this.workflowTableManager
          .importWorkflow(workflowName, workflowData);
      return respond(imported, HttpStatus.OK);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ApiOperation(value = "Get edge group list", notes = "Get edge group list")
  @RequestMapping(value = "/edge/groups", method = RequestMethod.GET)
  public ResponseEntity getGroupList() {
    return respondEntity(edgeInfo.getGroupList(), HttpStatus.OK);
  }

  @ApiOperation(value = "Get engine list", notes = "Get engine list")
  @RequestMapping(value = "/edge/groups/{groupId}", method = RequestMethod.GET)
  public ResponseEntity getEngineList(@PathVariable("groupId") String groupId,
      @RequestParam(value = "engineType", required = false) String engineType) {
    List<String> engineList;

    if (engineType == null) {
      engineList = edgeInfo.getEngineList(groupId, "ANY");
    } else {
      engineList = edgeInfo.getEngineList(groupId, engineType);
    }

    JsonArray response = new JsonArray();

    for (String engine : engineList) {
      response.add(engine);
    }

    return respondEntity(response, HttpStatus.OK);
  }

  private ResponseEntity listWorkflowComponentWorkflowBundles() {
    // TEMP
    Collection<WorkflowComponentBundle> workflows
        = this.workflowTableManager
        .listWorkflowComponentBundles(WorkflowComponentBundleType.WORKFLOW);
    return respondEntity(workflows, HttpStatus.OK);
  }

  private ResponseEntity listWorkflowComponentLinkBundles() {
    // TEMP
    Collection<WorkflowComponentBundle> workflows
        = this.workflowTableManager
        .listWorkflowComponentBundles(WorkflowComponentBundleType.LINK);
    return respondEntity(workflows, HttpStatus.OK);
  }

  private ResponseEntity listWorkflowComponentProcessorBundles() {
    // TEMP
    Collection<WorkflowComponentBundle> processors
        = this.workflowTableManager
        .listWorkflowComponentBundles(WorkflowComponentBundleType.PROCESSOR);
    return respondEntity(processors, HttpStatus.OK);
  }

  private ResponseEntity listWorkflowComponentSinkBundles() {
    // TEMP
    Collection<WorkflowComponentBundle> sinks
        = this.workflowTableManager
        .listWorkflowComponentBundles(WorkflowComponentBundleType.SINK);
    return respondEntity(sinks, HttpStatus.OK);
  }

  private ResponseEntity listWorkflowComponentSourceBundles() {
    // TEMP
    Collection<WorkflowComponentBundle> sources
        = this.workflowTableManager
        .listWorkflowComponentBundles(WorkflowComponentBundleType.SOURCE);
    return respondEntity(sources, HttpStatus.OK);
  }
}
