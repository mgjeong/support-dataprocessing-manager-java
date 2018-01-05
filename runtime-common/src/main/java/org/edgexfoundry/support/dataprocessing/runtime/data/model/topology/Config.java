package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class Config {

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
