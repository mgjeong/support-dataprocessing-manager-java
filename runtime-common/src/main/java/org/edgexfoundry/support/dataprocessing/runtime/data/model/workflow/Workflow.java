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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workflow extends Format {

  /**
   * Unique identifier for a workflow.
   */
  private Long id;

  /**
   * Name of a workflow.
   */
  private String name;

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
        return mapper.writeValueAsString(this.config);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonProperty("config")
  public void setConfigStr(String config) {
    try {
      if (StringUtils.isEmpty(config)) {
        throw new RuntimeException("Config is invalid.");
      }

      this.config = mapper
          .readValue(config, new TypeReference<Map<String, Object>>() {
          });
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
    if (config == null) {
      throw new RuntimeException("Config is invalid.");
    }
    this.config = config;
  }

  @JsonIgnore
  public void addConfig(String key, Object value) {
    if (this.config != null) {
      this.config.put(key, value);
    }
  }

  @JsonIgnore
  public <T> T getConfig(String key) {
    if (this.config != null) {
      return (T) this.config.get(key);
    } else {
      return null;
    }
  }
}
