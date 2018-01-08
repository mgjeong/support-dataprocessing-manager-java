package org.edgexfoundry.support.dataprocessing.runtime.data.model.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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
