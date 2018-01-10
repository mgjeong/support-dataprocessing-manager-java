package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyComponent extends Format {

  private Long id;
  private Long topologyId;
  private Long versionId;
  private Long topologyComponentBundleId;
  private String name = StringUtils.EMPTY;
  private String description = StringUtils.EMPTY;
  private Boolean reconfigure = false;
  private Long timestamp;
  private Config config = new Config();

  public TopologyComponent() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTopologyId() {
    return topologyId;
  }

  public void setTopologyId(Long topologyId) {
    this.topologyId = topologyId;
  }

  public Long getVersionId() {
    return versionId;
  }

  public void setVersionId(Long versionId) {
    this.versionId = versionId;
  }

  public Long getTopologyComponentBundleId() {
    return topologyComponentBundleId;
  }

  public void setTopologyComponentBundleId(Long topologyComponentBundleId) {
    this.topologyComponentBundleId = topologyComponentBundleId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getReconfigure() {
    return reconfigure;
  }

  public void setReconfigure(Boolean reconfigure) {
    this.reconfigure = reconfigure;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  @JsonIgnore
  public String getConfigStr() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this.config);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void setConfigStr(String config) {
    try {
      if (!StringUtils.isEmpty(config)) {
        ObjectMapper mapper = new ObjectMapper();
        this.config = mapper
            .readValue(config, new TypeReference<Config>() {
            });
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void addConfig(String key, Object value) {
    if (this.config != null) {
      this.config.put(key, value);
    }
  }

  public <T> T getConfig(String key) {
    if (this.config != null) {
      return (T) this.config.get(key);
    } else {
      return null;
    }
  }
}
