package org.edgexfoundry.support.dataprocessing.runtime.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.edgexfoundry.support.dataprocessing.runtime.controller.WorkflowController.ImportType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Developer APIs", description = "API List for developers")
@RequestMapping("/api/v1/developer/")
public class DeveloperApiController extends AbstractController {

  private final WorkflowController workflowController;
  private final TaskController taskController;

  public DeveloperApiController() {
    this.workflowController = new WorkflowController();
    this.taskController = new TaskController();
  }

  @ApiOperation(value = "Create workflow", notes = "Creates a new workflow from Json file or string.")
  @RequestMapping(value = "/workflows/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity createWorkflow(
      @RequestParam(value = "type") ImportType type,
      @RequestParam(value = "file", required = false) MultipartFile part,
      @RequestParam(value = "json", required = false) String json,
      @RequestParam("workflowName") final String workflowName) {
    return this.workflowController.importWorkflow(type, part, json, workflowName);
  }

  @ApiOperation(value = "Run workflow",
      notes = "Deploys workflow to an edge device for processing. A newly generated job id is returned on success.")
  @RequestMapping(value = "/workflows/{workflowId}/run", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity runWorkflow(@PathVariable("workflowId") Long workflowId) {
    return this.workflowController.deployWorkflow(workflowId);
  }

  @ApiOperation(value = "Stop job", notes = "Stops a running job instance.")
  @RequestMapping(value = "/workflows/{workflowId}/jobs/{jobId}/stop", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity stopJob(@PathVariable("workflowId") Long workflowId,
      @PathVariable("jobId") String jobId) {
    return this.workflowController.stopJob(workflowId, jobId);
  }

  @ApiOperation(value = "Remove workflow", notes = "Removes a workflow completely.")
  @RequestMapping(value = "/workflows/{workflowId}/remove", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeWorkflow(@PathVariable("workflowId") Long workflowId) {
    return this.workflowController.removeWorkflow(workflowId, false, true);
  }

  @ApiOperation(value = "List workflows", notes = "Returns a full list of workflows.")
  @RequestMapping(value = "/workflows", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity listWorkflows() {
    return this.workflowController.listWorkflows(true, "name", true, -1);
  }

  @ApiOperation(value = "Get workflow job details", notes = "Returns detailed monitoring states of workflow jobs.")
  @RequestMapping(value = "/workflows/{workflowId}/details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity monitorWorkflow(@PathVariable("workflowId") Long workflowId) {
    return this.workflowController.monitorJobDetails(workflowId);
  }

  @ApiOperation(value = "Add custom task", notes = "Adds a new custom task jar.")
  @RequestMapping(value = "/tasks/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity uploadCustomTask(@RequestParam("file") MultipartFile file) {
    return this.taskController.addCustomTask(file);
  }

  @ApiOperation(value = "Update custom task", notes = "Updates an existing custom task jar.")
  @RequestMapping(value = "/tasks/upload", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateCustomTask(@RequestParam("name") String taskName,
      @RequestParam("type") TaskType taskType,
      @RequestParam("file") MultipartFile file) {
    return this.taskController.updateCustomTask(taskName, taskType, file);
  }

  @ApiOperation(value = "Remove custom task", notes = "Removes an existing custom task.")
  @RequestMapping(value = "/tasks/remove", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity removeCustomTask(@RequestParam("name") String taskName,
      @RequestParam("type") TaskType taskType) {
    return this.taskController.removeCustomTask(taskName, taskType);
  }

  @ApiOperation(value = "Lists available tasks", notes = "Returns a list of available tasks.")
  @RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity listTasks() {
    return this.workflowController
        .listWorkflowComponentBundles(WorkflowComponentBundleType.PROCESSOR);
  }
}
