package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Collection;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Namespace extends Format {

  private Info namespace;
  private Collection<ServiceClusterMap> mappings = new ArrayList<>();

  public Namespace() {

  }

  public void addMapping(ServiceClusterMap map) {
    this.mappings.add(map);
  }

  public Info getNamespace() {
    return namespace;
  }

  public void setNamespace(Info namespace) {
    this.namespace = namespace;
  }

  public Collection<ServiceClusterMap> getMappings() {
    return mappings;
  }

  public void setMappings(Collection<ServiceClusterMap> mappings) {
    this.mappings = mappings;
  }


  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Info {

    private Long id;
    private String name;
    private String streamingEngine;
    private String timeSeriesDB;
    private String description;
    private Long timestamp;

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

    public String getStreamingEngine() {
      return streamingEngine;
    }

    public void setStreamingEngine(String streamingEngine) {
      this.streamingEngine = streamingEngine;
    }

    public String getTimeSeriesDB() {
      return timeSeriesDB;
    }

    public void setTimeSeriesDB(String timeSeriesDB) {
      this.timeSeriesDB = timeSeriesDB;
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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ServiceClusterMap {

    private Long namespaceId;
    private String serviceName;
    private Long clusterId;

    public ServiceClusterMap() {

    }

    public Long getNamespaceId() {
      return namespaceId;
    }

    public void setNamespaceId(Long namespaceId) {
      this.namespaceId = namespaceId;
    }

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public Long getClusterId() {
      return clusterId;
    }

    public void setClusterId(Long clusterId) {
      this.clusterId = clusterId;
    }
  }
}
