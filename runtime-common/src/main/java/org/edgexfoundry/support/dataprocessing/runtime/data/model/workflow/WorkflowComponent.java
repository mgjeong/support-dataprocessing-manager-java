package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowComponent extends Format {

  private Long id;
  private Long workflowId;
  private Long workflowComponentBundleId;
  private String name = StringUtils.EMPTY;
  private String description = StringUtils.EMPTY;
  private String engineType = StringUtils.EMPTY;
  private String path = StringUtils.EMPTY;
  private String classname = StringUtils.EMPTY;
  private Config config = new Config();

  public WorkflowComponent() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    this.workflowId = workflowId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getClassname() {
    return classname;
  }

  public void setClassname(String classname) {
    this.classname = classname;
  }

  public Long getWorkflowComponentBundleId() {
    return workflowComponentBundleId;
  }

  public void setWorkflowComponentBundleId(Long workflowComponentBundleId) {
    this.workflowComponentBundleId = workflowComponentBundleId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
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

  @JsonInclude(Include.NON_NULL)
  public static class Config {

    private Map<String, Object> properties = new HashMap<>();

    public Config() {

    }

    public Map<String, Object> getProperties() {
      return properties;
    }

    public void setProperties(Map<String, Object> properties) {
      this.properties = properties;
    }

    public Object get(String key) {
      return getObject(key);
    }

    private Object getObject(String key) {
      Object value = properties.get(key);
      if (value != null) {
        return value;
      } else {
        throw new NoSuchElementException(key);
      }
    }

    public <T> T getAny(String key) {
      return (T) getObject(key);
    }

    public <T> Optional<T> getAnyOptional(String key) {
      return Optional.ofNullable((T) properties.get(key));
    }

    // for unit tests
    public void setAny(String key, Object value) {
      properties.put(key, value);
    }

    public void putAll(Map<String, Object> properties) {
      this.properties.putAll(properties);
    }

    public Object get(String key, Object defaultValue) {
      Object value = properties.get(key);
      return value != null ? value : defaultValue;
    }

    public void put(String key, Object value) {
      properties.put(key, value);
    }

    public String getString(String key) {
      return (String) get(key);
    }

    public String getString(String key, String defaultValue) {
      return (String) get(key, defaultValue);
    }

    public int getInt(String key) {
      return (int) get(key);
    }

    public int getInt(String key, int defaultValue) {
      return (int) get(key, defaultValue);
    }

    public long getLong(String key) {
      return (long) get(key);
    }

    public long getLong(String key, long defaultValue) {
      return (long) get(key, defaultValue);
    }

    public double getDouble(String key) {
      return (double) get(key);
    }

    public double getDouble(String key, double defaultValue) {
      return (double) get(key, defaultValue);
    }

    public boolean getBoolean(String key) {
      return (boolean) get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
      return (boolean) get(key, defaultValue);
    }

    public boolean contains(String key) {
      return properties.containsKey(key);
    }
  }
}
