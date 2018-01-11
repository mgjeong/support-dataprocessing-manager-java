package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

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

@JsonInclude(Include.NON_NULL)
public class TopologyJob extends Format {

  private String id;
  private String groupId;
  private String engineId;
  private Map<String, Object> config = new HashMap<>();

  private TopologyJobState state;

  public TopologyJob() {

  }

  public TopologyJobState getState() {
    return state;
  }

  public void setState(
      TopologyJobState state) {
    this.state = state;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getEngineId() {
    return engineId;
  }

  public void setEngineId(String engineId) {
    this.engineId = engineId;
  }

  @JsonIgnore
  public Map<String, Object> getConfig() {
    return config;
  }

  @JsonIgnore
  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  @JsonIgnore
  public Object getConfig(String key) {
    return config.get(key);
  }

  @JsonIgnore
  public void addConfig(String key, Object value) {
    this.config.put(key, value);
  }

  @JsonProperty("config")
  public String getConfigStr() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(config);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @JsonProperty("config")
  public void setConfigStr(String configStr) {
    try {
      if (!StringUtils.isEmpty(configStr)) {
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper
            .readValue(configStr, new TypeReference<Map<String, Object>>() {
            });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static TopologyJob create(String jobGroupId, String jobId) {
    TopologyJob job = new TopologyJob();
    job.setState(new TopologyJobState());
    job.setId(jobId);
    job.setGroupId(jobGroupId);
    return job;
  }
}
