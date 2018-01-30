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
