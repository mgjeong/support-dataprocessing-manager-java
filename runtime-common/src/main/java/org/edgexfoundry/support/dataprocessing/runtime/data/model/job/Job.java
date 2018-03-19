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

package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;

@JsonInclude(Include.NON_NULL)
public class Job extends Format {

  private static final long serialVersionUID = 1L;

  private final String id;
  private final Long workflowId;
  private final JobState state;

  private Map<String, Object> config;
  private transient WorkflowData workflowData = null;

  /**
   * Constructs an Job object to deploy the workflow on an engine.
   *
   * @param id Job ID
   * @param workflowId Workflow ID
   */
  public Job(String id, Long workflowId) {
    this.id = id;
    this.workflowId = workflowId;
    this.config = new HashMap<>();
    this.state = new JobState(id);
  }

  /**
   * Returns an Job object with id validation.
   *
   * @param jobId Job ID to use
   * @param workflowId Workflow ID to use
   * @return Initialized engine-level object corresponding to workflow ID
   */
  public static Job create(String jobId, Long workflowId) {
    if (StringUtils.isEmpty(jobId) || workflowId == null) {
      throw new RuntimeException("Job id or workflow id is null.");
    }
    return new Job(jobId, workflowId);
  }

  /**
   * Returns an Job object with workflow specifications.
   * The workflowData argument must describe the workflow to deploy with engine to use.
   *
   * @param workflowData Input workflow
   * @return Engine-level object to deploy the input workflow as a job
   */
  public static Job create(WorkflowData workflowData) {
    if (workflowData == null) {
      throw new RuntimeException("Workflow data is null.");
    }
    Job job = create(UUID.randomUUID().toString(), workflowData.getWorkflowId());
    job.setWorkflowData(workflowData);
    job.setConfig(workflowData.getConfig());
    try {
      job.getState().setEngineType(workflowData.getEngineType().name());
    } catch (Exception e) {
      job.getState().setEngineType(EngineType.UNKNOWN.name());
    }
    return job;
  }

  public JobState getState() {
    return state;
  }

  @JsonProperty("jobId")
  public String getId() {
    return id;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  @JsonIgnore
  public Map<String, Object> getConfig() {
    return config;
  }

  @JsonIgnore
  public <T> T getConfig(String key) {
    return (T) config.get(key);
  }

  /**
   * Sets configurations of the job.
   *
   * @param config Key-value sets of configurations
   */
  @JsonIgnore
  public void setConfig(Map<String, Object> config) {
    if (config == null) {
      throw new RuntimeException("Invalid config");
    }
    this.config = config;
  }

  @JsonIgnore
  public void addConfig(String key, Object value) {
    this.config.put(key, value);
  }

  /**
   * Returns stringified of configurations.
   *
   * @return JSON string of the map object of configurations
   */
  @JsonProperty("config")
  public String getConfigStr() {
    try {
      return mapper.writeValueAsString(config);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets stringified configurations into map.
   *
   * @param configStr JSON string of configurations
   */
  @JsonProperty("config")
  public void setConfigStr(String configStr) {
    try {
      if (StringUtils.isEmpty(configStr)) {
        throw new RuntimeException("Invalid config");
      }
      this.config = mapper.readValue(configStr, new TypeReference<Map<String, Object>>() {
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public WorkflowData getWorkflowData() {
    return this.workflowData;
  }

  @JsonIgnore
  public void setWorkflowData(WorkflowData workflowData) {
    this.workflowData = workflowData;
  }

  @JsonIgnore
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Job)) {
      return false;
    }

    Job other = (Job) obj;
    return other.getId().equals(this.getId());
  }

  @JsonIgnore
  @Override
  public int hashCode() {
    return this.id == null ? 0 : this.id.hashCode();
  }
}
