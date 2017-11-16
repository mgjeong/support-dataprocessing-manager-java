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
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobGroupFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.JobResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.QueryResponseFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    public QueryController()
    {

    }

    @ApiOperation(value = "Find Supporting Task", notes = "Find Supporting Task")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public QueryResponseFormat getQueries(Locale locale, Model model) {

        QueryResponseFormat response = null;

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





        return null;
    }
}
