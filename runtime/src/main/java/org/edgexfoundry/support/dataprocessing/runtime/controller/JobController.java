package org.edgexfoundry.support.dataprocessing.runtime.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.MonitoringManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.KapacitorEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Job APIs", description = "API List for jobs")
@RequestMapping("/api/v1/catalog")
public class JobController extends AbstractController {

  private WorkflowTableManager workflowTableManager = null;
  private JobTableManager jobTableManager = null;

  public JobController() {
    this.workflowTableManager = WorkflowTableManager.getInstance();
    this.jobTableManager = JobTableManager.getInstance();
  }

  @ApiOperation(value = "Validate workflow", notes = "Validates a workflow")
  @RequestMapping(value = "/workflows/{workflowId}/actions/validate", method = RequestMethod.POST)
  public ResponseEntity validateWorkflow(@PathVariable("workflowId") Long workflowId) {
    Workflow result = this.workflowTableManager.getWorkflow(workflowId);

    return respond(result, HttpStatus.OK);
  }

  @ApiOperation(value = "Deploy workflow", notes = "Deploys a workflow")
  @RequestMapping(value = "/workflows/{workflowId}/actions/deploy", method = RequestMethod.POST)
  public ResponseEntity deployWorkflow(@PathVariable("workflowId") Long workflowId) {
    Workflow result = this.workflowTableManager.getWorkflow(workflowId);

    // flink result
    WorkflowData workflowData = this.workflowTableManager.doExportWorkflow(result);

    LOGGER.info("WorkflowData: " + workflowData.getConfigStr());

    // Create
    String targetHost = (String) workflowData.getConfig().get("targetHost");
    // targetHost = "localhost:8081";
    workflowData.getConfig().put("targetHost", targetHost);
    String[] splits = targetHost.split(":");
//    FlinkEngine engine = new FlinkEngine(splits[0], Integer.parseInt(splits[1]));
    Engine engine = EngineManager.getEngine(targetHost, workflowData.getEngineType());

    Job job;

    if(null == engine) {
      // Todo : define what type error will response.
      return respond(job, HttpStatus.NO_CONTENT);
    }

    try {

      job = engine.create(workflowData);
      if (job == null) {
        throw new Exception("Failed to create job.");
      }
      job = jobTableManager.addOrUpdateWorkflowJob(job); // add to database

      // Run job
      job = engine.run(job);
      jobTableManager.addOrUpdateWorkflowJobState(job.getId(), job.getState());

      // Todo : define how to handle exception when failed to run for monitoring.
      MonitoringManager.getInstance()sssss.addJob(job);

      // Failed to run job
      if (job.getState().getState() != State.RUNNING) {
        throw new Exception(job.getState().getErrorMessage());
      }

      return respond(job, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_ENGINE_FLINK, e.getMessage()), HttpStatus.OK);
    }
  }

  protected Engine createEngine(String targetHost, WorkflowData.EngineType engineType) {
    String[] splits = targetHost.split(":");
    if (engineType == WorkflowData.EngineType.FLINK) {
      return new FlinkEngine(splits[0], Integer.parseInt(splits[1]));
    } else if (engineType == WorkflowData.EngineType.KAPACITOR) {
      return new KapacitorEngine(splits[0], Integer.parseInt(splits[1]));
    } else {
      throw new RuntimeException("Unsupported operation.");
    }
  }

  /**
   * TEMPORARY
   **/
  @ApiOperation(value = "Get workflow jobs", notes = "Get workflow jos")
  @RequestMapping(value = "/workflows/{workflowId}/jobs", method = RequestMethod.GET)
  public ResponseEntity getWorkflowJobs(@PathVariable("workflowId") Long workflowId) {
    try {
      Collection<Job> jobs = jobTableManager.listWorkflowJobs(workflowId);
      jobs = jobs.stream().filter(workflowJob -> workflowJob.getState().getState() == State.RUNNING).collect(Collectors.toSet());
      return respondEntity(jobs, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  /**
   * TEMPORARY
   **/
  @ApiOperation(value = "Stop job", notes = "Stop job")
  @RequestMapping(value = "/workflows/{workflowId}/jobs/{jobId}/stop", method = RequestMethod.GET)
  public ResponseEntity stopJob(@PathVariable("workflowId") Long workflowId, @PathVariable("jobId") String jobId) {
    try {
      Job job = jobTableManager.getWorkflowJob(jobId);
      String targetHost = job.getConfig("targetHost");
      Engine engine = createEngine(targetHost, WorkflowData.EngineType.FLINK);

      try {
        job = engine.stop(job);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        job.getState().setState(State.STOPPED); // Set to stop, anyhow
      }
      jobTableManager.addOrUpdateWorkflowJobState(job.getId(), job.getState());
      return respond(job, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Monitor jobs", notes = "Monitor jobs")
  @RequestMapping(value = "/workflows/monitor/", method = RequestMethod.GET)
  public ResponseEntity monitorJobs() {
    try {
      return respond(MonitoringManager.getInstance().getWorkflowsState(), HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Monitor Job", notes = "Monitor job")
  @RequestMapping(value = "/workflows/monitor/{groupId}", method = RequestMethod.GET)
  public ResponseEntity monitorJob(@PathVariable("groupId") String groupId) {
    try {
      return respond(MonitoringManager.getInstance().getGroupState(groupId), HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }
}
