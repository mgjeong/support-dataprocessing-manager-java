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

import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobInfoFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobGroupResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.ResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.engine.EngineType;
import org.edgexfoundry.support.dataprocessing.runtime.job.JobManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@CrossOrigin(origins = "*")
@RestController
@Api(tags = "Job Manager", description = "API List for Job Managing")
@RequestMapping(value = "/v1/job")
public class JobController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private JobManager jobManager = null;

    public JobController() {
        try {
            jobManager = JobManager.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "Find Job Instances", notes = "Find Job Instances")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public JobGroupResponseFormat getJob(Locale locale, Model model) {
        JobGroupResponseFormat response = jobManager.getAllJobs();

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Find Job Instance by ID", notes = "Find Job Instance by ID")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JobGroupResponseFormat getJobById(Locale locale, Model model, @PathVariable("id") String id) {
        LOGGER.debug("Data : " + id);

        JobGroupResponseFormat response = jobManager.getJob(id);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Find Job description by jobId", notes = "Find Job description by jobId")
    @RequestMapping(value = "/info/{jobid}", method = RequestMethod.GET)
    @ResponseBody
    public JobInfoFormat getJobInfoByJobId(Locale locale, Model model, @PathVariable("jobid") String jid) {
        LOGGER.debug("Data : " + jid);

        JobInfoFormat response = jobManager.getJobInfoByJobId(jid);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Create Job Instance", notes = "Create Job Instance")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public JobResponseFormat createJob(Locale locale, Model model,
                                       @ApiParam(value = "json String",
                                            name = "json")
                                       @RequestBody(required = true)
                                            JobGroupFormat request) {
        LOGGER.debug(request.toString());

        JobResponseFormat response = jobManager.createGroupJob(EngineType.Flink, request);

        LOGGER.debug(response.toString());
        return response;
    }

    @ApiOperation(value = "Delete All Job Instance", notes = "Delete All Job Instance")
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseFormat deleteAllJob(Locale locale, Model model) {
        ErrorFormat response = jobManager.deleteAllJob();

        LOGGER.debug(response.toString());
        return new ResponseFormat(response);
    }

//    @ApiOperation(value = "Create Group Job Instance", notes = "Create Group Job Instance")
//    @RequestMapping(value = "/group", method = RequestMethod.POST)
//    @ResponseBody
//    public JobResponseFormat createGroupJob(Locale locale, Model model,
//                                         @ApiParam(value = "ex)\t\n" + CREATEGROUPJOB_DEFAULT_VALUE,
//                                                 name = "json")
//                                         @RequestBody(required = true)
//                                                 JobGroupFormat request) {
//        LOGGER.debug(request.toString());
//
//        JobResponseFormat response = jobManager.createGroupJob(request);
//
//        LOGGER.debug(response.toString());
//        return response;
//    }

    @ApiOperation(value = "Update Job Instance", notes = "Update Job Instance")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JobResponseFormat updateJob(Locale locale, Model model,
                                       @PathVariable("id") String id,
                                       @ApiParam(value = "json String",
                                            name = "json")
                                       @RequestBody(required = true)
                                            JobGroupFormat request) {
        LOGGER.debug("Id : " + id);
        LOGGER.debug(request.toString());

        JobResponseFormat response = jobManager.updateJob(EngineType.Flink, id, request);

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
