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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.Locale;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskManager;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Task Manager", description = "API List for Task Managing")
@RequestMapping(value = "/analytics/v1/task")
public class TaskController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
  private TaskManager taskManager;

  public TaskController() {
    try {
      taskManager = TaskManager.getInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @ApiOperation(value = "Add New Custom Task", notes = "Add New Custom Task")
  @RequestMapping(value = "", method = RequestMethod.POST, headers = ("content-type=multipart/*"))
  @ResponseBody
  public ResponseFormat addTask(Locale locale, Model model,
      @RequestParam("file") MultipartFile inputFile) {
    ResponseFormat response = new ResponseFormat();
    ErrorFormat result = new ErrorFormat();

    byte[] data = null;

    try {
      data = inputFile.getBytes();
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      result.setResponseMessage(e.getMessage());
      result.setErrorCode(ErrorType.DPFW_ERROR_INVALID_PARAMS);
      response.setError(result);
      return response;
    }

    // TODO:
    //result = taskManager.addTask(inputFile.getOriginalFilename(), data);
    response.setError(result);
    LOGGER.debug(response.toString());
    return response;
  }

  @ApiOperation(value = "Delete Custom Task", notes = "Delete Custom Task")
  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  @ResponseBody
  public ResponseFormat deleteTask(Locale locale, Model model,
      @RequestParam("type") TaskType type,
      @RequestParam("name") String name) {
    LOGGER.debug("Data : " + name + ", " + type);

    ResponseFormat response = new ResponseFormat();
    // TODO:
    //ErrorFormat result = taskManager.deleteTask(type, name);
    //response.setError(result);

    LOGGER.debug(response.toString());
    return response;
  }
}
