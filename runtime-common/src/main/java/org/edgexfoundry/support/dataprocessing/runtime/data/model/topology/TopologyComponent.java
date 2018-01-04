package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
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
  private Config config;

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

  @JsonInclude(Include.NON_NULL)
  static class Config {

    private Map<String, Object> properties = new HashMap<>();

    public Config() {

    }

    public Map<String, Object> getProperties() {
      return properties;
    }

    public void setProperties(Map<String, Object> properties) {
      this.properties = properties;
    }
  }
}
