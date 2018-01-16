package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopologyComponentBundle extends Format {

  public enum TopologyComponentType {
    SOURCE, PROCESSOR, SINK, TOPOLOGY, LINK;

    public static TopologyComponentType toTopologyComponentType(String type) {
      for (TopologyComponentType t : TopologyComponentType.values()) {
        if (t.name().equalsIgnoreCase(type)) {
          return t;
        }
      }
      return null;
    }
  }

  private Long id;
  private String name;
  private TopologyComponentType type;
  private Long timestamp;
  private String streamingEngine;
  private String subType;
  private String bundleJar;

  private ComponentUISpecification topologyComponentUISpecification;
  private String fieldHintProviderClass;
  private String transformationClass;
  private Boolean builtin;
  private String mavenDeps;

  public TopologyComponentBundle() {

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

  public TopologyComponentType getType() {
    return type;
  }

  public void setType(TopologyComponentType type) {
    this.type = type;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getStreamingEngine() {
    return streamingEngine;
  }

  public void setStreamingEngine(String streamingEngine) {
    this.streamingEngine = streamingEngine;
  }

  public String getSubType() {
    return subType;
  }

  public void setSubType(String subType) {
    this.subType = subType;
  }

  public String getBundleJar() {
    return bundleJar;
  }

  public void setBundleJar(String bundleJar) {
    this.bundleJar = bundleJar;
  }

  public ComponentUISpecification getTopologyComponentUISpecification() {
    return topologyComponentUISpecification;
  }

  public void setTopologyComponentUISpecification(
      ComponentUISpecification topologyComponentUISpecification) {
    this.topologyComponentUISpecification = topologyComponentUISpecification;
  }

  public String getFieldHintProviderClass() {
    return fieldHintProviderClass;
  }

  public void setFieldHintProviderClass(String fieldHintProviderClass) {
    this.fieldHintProviderClass = fieldHintProviderClass;
  }

  public String getTransformationClass() {
    return transformationClass;
  }

  public void setTransformationClass(String transformationClass) {
    this.transformationClass = transformationClass;
  }

  public Boolean isBuiltin() {
    return builtin;
  }

  public void setBuiltin(Boolean builtin) {
    this.builtin = builtin;
  }

  public String getMavenDeps() {
    return mavenDeps;
  }

  public void setMavenDeps(String mavenDeps) {
    this.mavenDeps = mavenDeps;
  }
}
