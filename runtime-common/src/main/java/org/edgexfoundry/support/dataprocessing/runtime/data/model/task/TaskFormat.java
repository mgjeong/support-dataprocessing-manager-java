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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.task;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModelParam;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskType;

@ApiModel(value = "Process", description = "Process")
public class TaskFormat extends Format {

  @ApiModelProperty(required = false)
  private TaskType type;
  @ApiModelProperty(required = true)
  private String name;
  @ApiModelProperty(required = true, dataType = "json")
  private TaskModelParam params;
  @ApiModelProperty(required = true)
  private List<String> inrecord;
  @ApiModelProperty(required = true)
  private List<String> outrecord;
  @ApiModelProperty(required = false)
  private String jar = null;
  @ApiModelProperty(required = false)
  private String className = null;

  private Long id;

  public TaskFormat() {
    this(TaskType.INVALID, null, (TaskModelParam) null);
  }

  public TaskFormat(TaskFormat task) {
    this(task.getType(), task.getName(), task.getParams());
  }

  public TaskFormat(TaskType type, String name, String params) {
    this(type, name, TaskModelParam.create(params));
  }

  public TaskFormat(TaskType type, String name, TaskModelParam params) {
    setType(type);
    setName(name);
    setParams(params);
    this.inrecord = new ArrayList<>();
    this.outrecord = new ArrayList<>();
  }

  public TaskModelParam getParams() {
    return params;
  }

  public void setParams(TaskModelParam params) {
    this.params = params;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TaskType getType() {
    return type;
  }

  public void setType(TaskType type) {
    this.type = type;
  }

  public List<String> getInrecord() {
    return inrecord;
  }

  public void setInrecord(List<String> inrecord) {
    this.inrecord = inrecord;
  }

  public List<String> getOutrecord() {
    return outrecord;
  }

  public void setOutrecord(List<String> outrecord) {
    this.outrecord = outrecord;
  }

  public String getJar() {
    return this.jar;
  }

  public void setJar(String jar) {
    this.jar = jar;
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
