package org.edgexfoundry.support.dataprocessing.runtime.data.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.job.JobState.State;

@JsonInclude(Include.NON_NULL)
public class Job extends Format {

  private String id;
  private Long workflowId;
  private Map<String, Object> config = new HashMap<>();

  private JobState state;

  private final ObjectMapper mapper = new ObjectMapper();

  public Job() {

  }

  public JobState getState() {
    return state;
  }

  public void setState(
      JobState state) {
    this.state = state;
  }

  @JsonProperty("jobId")
  public String getId() {
    return id;
  }

  @JsonProperty("jobId")
  public void setId(String id) {
    this.id = id;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Invalid workflow id");
    }
    this.workflowId = workflowId;
  }

  @JsonIgnore
  public Map<String, Object> getConfig() {
    return config;
  }

  @JsonIgnore
  public void setConfig(Map<String, Object> config) {
    if (config == null) {
      throw new RuntimeException("Invalid config");
    }
    this.config = config;
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

  @JsonProperty("config")
  public void setConfigStr(String configStr) {
    try {
      if (StringUtils.isEmpty(configStr)) {
        throw new RuntimeException("Invalid config");
      }
      this.config = mapper
          .readValue(configStr, new TypeReference<Map<String, Object>>() {
          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Job create(Long workflowId) {
    Job job = new Job();
    job.setState(new JobState());
    job.getState().setState(State.CREATED);
    job.setWorkflowId(workflowId);
    job.setId(UUID.randomUUID().toString());
    return job;
  }
}
