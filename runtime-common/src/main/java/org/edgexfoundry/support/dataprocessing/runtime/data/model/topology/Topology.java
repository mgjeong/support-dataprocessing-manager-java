package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Topology extends Format {

  /**
   * Unique identifier for a topology.
   */
  private Long id;

  /**
   * Name of a topology.
   */
  private String name;

  /**
   * Version id, hard-code it as 1
   */
  private Long versionId = 1L;
  private Long namespaceId;

  private String description;
  private Long timestamp;

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
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.config);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonProperty("config")
  public void setConfigStr(String config) {
    try {
      if (!StringUtils.isEmpty(config)) {
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper
            .readValue(config, new TypeReference<Map<String, Object>>() {
            });
      }
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
    this.config = config;
  }

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public Long getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId(Long namespaceId) {
    this.namespaceId = namespaceId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

}
