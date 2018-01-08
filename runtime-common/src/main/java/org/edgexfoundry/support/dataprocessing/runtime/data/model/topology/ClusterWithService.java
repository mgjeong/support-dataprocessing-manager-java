package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(Include.NON_NULL)
public class ClusterWithService extends Format {

  private Long id;

  private Cluster cluster;
  private List<ServiceConfiguration> serviceConfigurations;

  public ClusterWithService() {

  }

  @JsonIgnore
  public Long getId() {
    return id;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(
      Cluster cluster) {
    this.cluster = cluster;
  }

  public List<ServiceConfiguration> getServiceConfigurations() {
    return serviceConfigurations;
  }

  public void setServiceConfigurations(
      List<ServiceConfiguration> serviceConfigurations) {
    this.serviceConfigurations = serviceConfigurations;
  }

  public void setId(long id) {
    this.id = id;
  }

  @JsonInclude(Include.NON_NULL)
  public static class Cluster {

    private Long id;
    private String name;
    private String description;
    private Long timestamp;

    public Cluster() {

    }

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

  @JsonInclude(Include.NON_NULL)
  public static class ServiceConfiguration {

    private Service service;
    private List<Configuration> configurations;

    public ServiceConfiguration() {

    }

    public Service getService() {
      return service;
    }

    public void setService(
        Service service) {
      this.service = service;
    }

    public List<Configuration> getConfigurations() {
      return configurations;
    }

    public void setConfigurations(
        List<Configuration> configurations) {
      this.configurations = configurations;
    }

  }

  @JsonInclude(Include.NON_NULL)
  public static class Service {

    private Long id;
    private Long clusterId;
    private String name;
    private String description;
    private Long timestamp;


    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Long getClusterId() {
      return clusterId;
    }

    public void setClusterId(Long clusterId) {
      this.clusterId = clusterId;
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

    public Long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
    }
  }

  @JsonInclude(Include.NON_NULL)
  public static class Configuration {

    private Long id;
    private Long serviceId;
    private String name;
    private String configuration;
    private String description;
    private String filename;
    private Long timestamp;
    private Map<String, Object> configurationMap;

    public Configuration() {

    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Long getServiceId() {
      return serviceId;
    }

    public void setServiceId(Long serviceId) {
      this.serviceId = serviceId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getConfiguration() {
      return configuration;
    }

    public void setConfiguration(String configuration) {
      this.configuration = configuration;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(String filename) {
      this.filename = filename;
    }

    public Long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
    }

    public Map<String, Object> getConfigurationMap() {
      return configurationMap;
    }

    public void setConfigurationMap(Map<String, Object> configurationMap) {
      this.configurationMap = configurationMap;
    }
  }
}
