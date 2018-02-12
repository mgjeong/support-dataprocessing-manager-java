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

@JsonInclude(Include.NON_NULL)
public class Job extends Format {

  private final String id;
  private final Long workflowId;
  private final JobState state;

  private Map<String, Object> config;
  private WorkflowData workflowData = null;

  public Job(String id, Long workflowId) {
    this.id = id;
    this.workflowId = workflowId;
    this.config = new HashMap<>();
    this.state = new JobState(id);
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

  @JsonIgnore
  public void addConfig(String key, Object value) {
    this.config.put(key, value);
  }

  @JsonProperty("config")
  public String getConfigStr() {
    try {
      return mapper.writeValueAsString(config);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void setConfig(Map<String, Object> config) {
    if (config == null) {
      throw new RuntimeException("Invalid config");
    }
    this.config = config;
  }

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
  public void setWorkflowData(WorkflowData workflowData) {
    this.workflowData = workflowData;
  }

  @JsonIgnore
  public WorkflowData getWorkflowData() {
    return this.workflowData;
  }


  public static Job create(String jobId, Long workflowId) {
    if (StringUtils.isEmpty(jobId) || workflowId == null) {
      throw new RuntimeException("Job id or workflow id is null.");
    }
    return new Job(jobId, workflowId);
  }

  public static Job create(WorkflowData workflowData) {
    if (workflowData == null) {
      throw new RuntimeException("Workflow data is null.");
    }
    Job job = create(UUID.randomUUID().toString(), workflowData.getWorkflowId());
    job.setWorkflowData(workflowData);
    job.getState().setEngineType(workflowData.getEngineType().name());
    return job;
  }
}
