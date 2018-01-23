package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workflow extends Format {

  /**
   * Unique identifier for a workflow.
   */
  private Long id;

  /**
   * Name of a workflow.
   */
  private String name;

  private Map<String, Object> config = new HashMap<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("config")
  public String getConfigStr() {
    if (this.config.isEmpty()) {
      return "{}";
    } else {
      try {
        return mapper.writeValueAsString(this.config);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonProperty("config")
  public void setConfigStr(String config) {
    try {
      if (config == null || StringUtils.isEmpty(config)) {
        throw new RuntimeException("Config is invalid.");
      }

      this.config = mapper
          .readValue(config, new TypeReference<Map<String, Object>>() {
          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public Map<String, Object> getConfig() {
    return config;
  }

  @JsonIgnore
  public void setConfig(Map<String, Object> config) {
    if (config == null) {
      throw new RuntimeException("Config is invalid.");
    }
    this.config = config;
  }

  @JsonIgnore
  public void addConfig(String key, Object value) {
    if (this.config != null) {
      this.config.put(key, value);
    }
  }

  @JsonIgnore
  public <T> T getConfig(String key) {
    if (this.config != null) {
      return (T) this.config.get(key);
    } else {
      return null;
    }
  }
}
