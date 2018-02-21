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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class AbstractController {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

  private static final JsonParser jsonParser = new JsonParser();

  protected static ResponseEntity respondEntity(Object obj, HttpStatus httpStatus) {
    JsonElement entity = jsonParser.parse(obj.toString());
    JsonObject parent = new JsonObject();
    parent.add("entities", entity);
    return respond(parent, httpStatus);
  }

  protected static ResponseEntity respond(Object obj, HttpStatus httpStatus) {
    return ResponseEntity
        .status(httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(obj.toString());
  }
}
