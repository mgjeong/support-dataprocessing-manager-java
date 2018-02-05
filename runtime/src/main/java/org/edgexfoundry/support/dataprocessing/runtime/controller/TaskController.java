/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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

import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Task Manager", description = "API List for Task Managing")
@RequestMapping("/api/v1/catalog")
public class TaskController extends AbstractController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

  private TaskManager taskManager = null;

  public TaskController() {
    this.taskManager = TaskManager.getInstance();
  }

  @ApiOperation(value = "Add custom task", notes = "Adds a new custom task jar")
  @RequestMapping(value = "/upload/task", method = RequestMethod.POST)
  public ResponseEntity addCustomTask(@RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return respond(
          new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "Uploaded file is empty."),
          HttpStatus.OK);
    }

    try {
      // make file
      int added = this.taskManager
          .addCustomTask(file.getOriginalFilename(), file.getInputStream());

      // TODO: format response
      JsonObject response = new JsonObject();
      response.addProperty("status", "Success");
      response.addProperty("filename", file.getOriginalFilename());
      response.addProperty("added", added);
      return respond(response, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Update custom task", notes = "Updates an existing custom task jar")
  @RequestMapping(value = "/upload/task", method = RequestMethod.PUT)
  public ResponseEntity updateCustomTask(@RequestParam("name") String taskName,
      @RequestParam("type") TaskType taskType,
      @RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return respond(
          new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "Uploaded file is empty."),
          HttpStatus.OK);
    } else if (StringUtils.isEmpty(taskName) || taskType == null) {
      return respond(
          new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "Invalid task name or type."),
          HttpStatus.OK);
    }

    try {
      // make file
      int updated = this.taskManager
          .updateCustomTask(taskName, taskType, file.getOriginalFilename(), file.getInputStream());

      // TODO: format response
      JsonObject response = new JsonObject();
      response.addProperty("status", "Success");
      response.addProperty("filename", file.getOriginalFilename());
      response.addProperty("updated", updated);
      return respond(response, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.OK);
    }
  }

  @ApiOperation(value = "Remove custom task", notes = "Removes an existing custom task")
  @RequestMapping(value = "/upload/task", method = RequestMethod.DELETE)
  public ResponseEntity removeCustomTask(@RequestParam("name") String taskName,
      @RequestParam("type") TaskType taskType) {
    try {
      this.taskManager.removeCustomTask(taskName, taskType);

      // TODO: format response
      JsonObject response = new JsonObject();
      response.addProperty("status", "Success");
      return respond(response, HttpStatus.OK);
    } catch (Exception e) {
      return respond(new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage()),
          HttpStatus.OK);
    }
  }

}
