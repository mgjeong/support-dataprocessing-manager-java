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

import io.swagger.annotations.ApiParam;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobGroupResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.QueryResponseFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.job.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@Api(tags = "Query Manager", description = "API List for Query Managing")
@RequestMapping(value = "/v1/query")
public class QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private JobManager jobManager = null;

    public QueryController()
    {
        try {
            jobManager = JobManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "Find Supporting Querying Job", notes = "Find Supporting Querying Job")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public JobGroupResponseFormat getQueries(Locale locale, Model model) {

        JobGroupResponseFormat response = jobManager.getAllJobs();

        LOGGER.debug(response.toString());

        return response;

    }

    @ApiOperation(value = "Add query Operation", notes = "Add query Operation")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public JobResponseFormat addQuery(Locale locale, Model model,
                                      @ApiParam(value = "json String",
                                            name = "json")
                                       @RequestBody(required = true)
                                              JobGroupFormat request) {

        LOGGER.debug(request.toString());

        JobResponseFormat response = jobManager.createGroupJob(EngineType.Kapacitor, request);

        LOGGER.debug(response.toString());

        return response;
    }

    @ApiOperation(value = "Delete All Job Instance", notes = "Delete All Job Instance")
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseFormat deleteAllQuery(Locale locale, Model model) {
        ErrorFormat response = jobManager.deleteAllQuery();

        LOGGER.debug(response.toString());

        return new ResponseFormat(response);
    }

    @ApiOperation(value = "Update Job Instance", notes = "Update Job Instance")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JobResponseFormat updateQuery(Locale locale, Model model,
                                       @PathVariable("id") String id,
                                       @ApiParam(value = "json String",
                                            name = "json")
                                       @RequestBody(required = true)
                                            JobGroupFormat request) {
        LOGGER.debug("Id : " + id);
        LOGGER.debug(request.toString());

        JobResponseFormat response = jobManager.updateJob(EngineType.Kapacitor, id, request);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Delete Job Instance", notes = "Delete Job Instance")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public JobResponseFormat deleteJob(Locale locale, Model model, @PathVariable("id") String id) {
        LOGGER.debug("Id : " + id);

        JobResponseFormat response = jobManager.deleteJob(id);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Execute Job Instance", notes = "Execute Job Instance")
    @RequestMapping(value = "/{id}/execute", method = RequestMethod.POST)
    @ResponseBody
    public JobResponseFormat executeJob(Locale locale, Model model, @PathVariable("id") String id) {
        LOGGER.debug("Id : " + id);

        JobResponseFormat response = jobManager.executeJob(id);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Stop Job Instance", notes = "Stop Job Instance")
    @RequestMapping(value = "/{id}/stop", method = RequestMethod.POST)
    @ResponseBody
    public JobResponseFormat stopJob(Locale locale, Model model, @PathVariable("id") String id) {
        LOGGER.debug("Id : " + id);

        JobResponseFormat response = jobManager.stopJob(id);

        LOGGER.debug(response.toString());
        return response;
    }
}
