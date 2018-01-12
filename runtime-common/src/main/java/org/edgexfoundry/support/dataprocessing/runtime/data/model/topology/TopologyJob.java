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
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class TopologyJob extends Format {

  private String id;
  private Long topologyId;
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

  @JsonProperty("jobId")
  public String getId() {
    return id;
  }

  @JsonProperty("jobId")
  public void setId(String id) {
    this.id = id;
  }

  public Long getTopologyId() {
    return topologyId;
  }

  public void setTopologyId(Long topologyId) {
    this.topologyId = topologyId;
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

  public static TopologyJob create(Long topologyId, String jobId) {
    TopologyJob job = new TopologyJob();
    job.setState(new TopologyJobState());
    job.setTopologyId(topologyId);
    job.setId(jobId);
    return job;
  }
}
