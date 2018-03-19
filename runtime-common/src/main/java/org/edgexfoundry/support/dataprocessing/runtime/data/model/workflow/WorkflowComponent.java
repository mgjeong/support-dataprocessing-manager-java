/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowComponent extends Format {

  private Long id;
  private Long workflowId;
  private Long workflowComponentBundleId;
  private String bundleName = StringUtils.EMPTY;
  private String bundleSubType = StringUtils.EMPTY;
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
    if (config == null) {
      throw new RuntimeException("Invalid config.");
    }
    this.config = config;
  }

  public String getBundleName() {
    return bundleName;
  }

  public void setBundleName(String bundleName) {
    this.bundleName = bundleName;
  }

  public String getBundleSubType() {
    return bundleSubType;
  }

  public void setBundleSubType(String bundleSubType) {
    this.bundleSubType = bundleSubType;
  }

  @JsonIgnore
  public String getConfigStr() {
    try {
      return mapper.writeValueAsString(this.config);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void setConfigStr(String config) {
    try {
      if (StringUtils.isEmpty(config)) {
        throw new RuntimeException("Invalid config");
      }

      this.config = mapper
          .readValue(config, new TypeReference<Config>() {
          });
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
  public static class Config extends Format {

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

    public void put(String key, Object value) {
      properties.put(key, value);
    }

  }
}
