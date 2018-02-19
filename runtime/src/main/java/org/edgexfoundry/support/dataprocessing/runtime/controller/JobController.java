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
package org.edgexfoundry.support.dataprocessing.runtime.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.Job;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.Workflow;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowJobMetric;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.GroupInfo;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowMetric.Work;
import org.edgexfoundry.support.dataprocessing.runtime.db.JobTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.engine.Engine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineManager;
import org.edgexfoundry.support.dataprocessing.runtime.monitor.MonitoringManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Job APIs", description = "API List for jobs")
@RequestMapping("/api/v1/catalog")
public class JobController extends AbstractController {

  private WorkflowTableManager workflowTableManager = null;
  private JobTableManager jobTableManager = null;
  private MonitoringManager monitoringManager = null;

  public JobController() {
    this.workflowTableManager = WorkflowTableManager.getInstance();
    this.jobTableManager = JobTableManager.getInstance();
    this.monitoringManager = MonitoringManager.getInstance();
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
    WorkflowData workflowData;
    Job newJob;

    // create job instance from workflow data
    try {
      Workflow result = this.workflowTableManager.getWorkflow(workflowId);
      workflowData = this.workflowTableManager.doExportWorkflow(result);

      LOGGER.debug("Workflow data: " + workflowData.getConfigStr());

      newJob = Job.create(workflowData);
      this.jobTableManager.addJob(newJob); // add to database
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INTERNAL_ERROR, e.getMessage()),
          HttpStatus.OK);
    }

    // create and run engine job
    try {
      // create engine job
      String targetHost = (String) workflowData.getConfig().get("targetHost");
      Engine engine = getEngine(targetHost, workflowData.getEngineType());

      try {
        // create
        engine.create(newJob);

        // run engine job
        engine.run(newJob);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        newJob.getState().setState(State.ERROR);
        newJob.getState().setState(e.getMessage());
      }

      // update to database
      this.jobTableManager.updateJobState(newJob.getState());

      // add to monitoring
      this.monitoringManager.addJob(newJob);

      return respond(newJob, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INTERNAL_ERROR, e.getMessage()),
          HttpStatus.OK);
    }
  }

  protected Engine getEngine(String targetHost, WorkflowData.EngineType engineType) {
    String[] splits = targetHost.split(":");

    return EngineManager.getInstance()
        .getEngine(splits[0], Integer.parseInt(splits[1]), engineType);
  }

  @ApiOperation(value = "Stop job", notes = "Stop job")
  @RequestMapping(value = "/workflows/{workflowId}/jobs/{jobId}/stop", method = RequestMethod.GET)
  public ResponseEntity stopJob(@PathVariable("workflowId") Long workflowId,
      @PathVariable("jobId") String jobId) {
    try {
      Job job = jobTableManager.getJobById(jobId);
      String targetHost = job.getConfig("targetHost");
      Engine engine = getEngine(targetHost, EngineType.valueOf(job.getState().getEngineType()));

      try {
        engine.stop(job);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        job.getState().setState(State.STOPPED); // Set to stop, anyhow
        job.getState().setErrorMessage(e.getMessage());
      }

      // remove from monitoring
      this.monitoringManager.removeJob(job);

      // update database
      this.jobTableManager.updateJobState(job.getState());

      return respond(job, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Monitor jobs", notes = "Monitor jobs")
  @RequestMapping(value = "/workflows/monitor/", method = RequestMethod.GET)
  public ResponseEntity monitorJobs() {
    try {
      Collection<Job> allJobs = jobTableManager.getJobs();

      Map<Long, GroupInfo> groupInfoMap = new HashMap<>();
      for (Job job : allJobs) {
        GroupInfo groupInfo = groupInfoMap.get(job.getWorkflowId());
        if (groupInfo == null) {
          groupInfo = new GroupInfo();
          groupInfo.setGroupId(String.valueOf(job.getWorkflowId()));
          groupInfo.setWorks(new Work());
          groupInfoMap.put(job.getWorkflowId(), groupInfo);
        }
        if (job.getState().getState() == State.RUNNING) {
          groupInfo.getWorks().setRunning(groupInfo.getWorks().getRunning() + 1);
        } else {
          groupInfo.getWorks().setStop(groupInfo.getWorks().getStop() + 1);
        }
      }

      WorkflowMetric workflowMetric = new WorkflowMetric();
      workflowMetric.setGroups(new ArrayList<>(groupInfoMap.values()));

      return respond(workflowMetric, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Monitor Job", notes = "Monitor job")
  @RequestMapping(value = "/workflows/monitor/{groupId}", method = RequestMethod.GET)
  public ResponseEntity monitorJob(@PathVariable("groupId") Long workflowId) {
    try {
      Collection<Job> jobs = jobTableManager.getJobsByWorkflow(workflowId);
      WorkflowMetric.GroupInfo groupInfo = new GroupInfo();
      groupInfo.setGroupId(String.valueOf(workflowId));
      groupInfo.setWorks(new Work());

      jobs.forEach(job -> {
        if (job.getState().getState() == State.RUNNING) {
          groupInfo.getWorks().setRunning(groupInfo.getWorks().getRunning() + 1);
        } else {
          groupInfo.getWorks().setStop(groupInfo.getWorks().getStop() + 1);
        }
      });

      return respond(groupInfo, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Monitor Job", notes = "Monitor job")
  @RequestMapping(value = "/workflows/monitor/{groupId}/details", method = RequestMethod.GET)
  public ResponseEntity monitorJobDetails(@PathVariable("groupId") Long workflowId) {
    try {
      Collection<Job> jobs = this.jobTableManager.getJobsByWorkflow(workflowId);

      WorkflowJobMetric workflowJobMetric = new WorkflowJobMetric();
      workflowJobMetric.setGroupId(workflowId);

      workflowJobMetric.setJobStates(new ArrayList<>(
          jobs.stream().map(job -> job.getState()).collect(Collectors.toList())
      ));

      return respond(workflowJobMetric, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage()), HttpStatus.OK);
    }
  }
}
