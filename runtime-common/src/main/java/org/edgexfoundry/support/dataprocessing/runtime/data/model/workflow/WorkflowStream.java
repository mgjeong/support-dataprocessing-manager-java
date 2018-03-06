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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.Format;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowStream extends Format {

  private static final long serialVersionUID = 1L;

  private Long id;
  private Long componentId;
  private String streamId;
  private String description;
  private Long workflowId;
  private List<Field> fields = new ArrayList<>();

  public WorkflowStream() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStreamId() {
    return streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(Long workflowId) {
    if (workflowId == null) {
      throw new RuntimeException("Invalid workflow id");
    }
    this.workflowId = workflowId;
  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    if (fields == null) {
      throw new RuntimeException("Invalid fields");
    }
    this.fields = fields;
  }

  @JsonIgnore
  public Long getComponentId() {
    return componentId;
  }

  @JsonIgnore
  public void setComponentId(Long componentId) {
    this.componentId = componentId;
  }

  @JsonIgnore
  public String getFieldsStr() {
    if (this.fields.isEmpty()) {
      return "[]";
    } else {
      try {
        return mapper.writeValueAsString(this.fields);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @JsonIgnore
  public void setFieldsStr(String fields) {
    try {
      if (StringUtils.isEmpty(fields)) {
        throw new RuntimeException("Invalid fields");
      }
      this.fields = mapper
          .readValue(fields, new TypeReference<List<Field>>() {
          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonIgnore
  public void addField(Field field) {
    if (field == null) {
      throw new RuntimeException("Invalid field");
    }

    fields.add(field);
  }

  public enum Grouping {
    SHUFFLE, FIELDS
  }

  public enum SchemaType {
    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    SHORT(Short.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    STRING(String.class),
    BINARY(byte[].class),
    NESTED(Map.class),
    ARRAY(List.class),
    BLOB(InputStream.class);

    private final Class<?> javaType;

    SchemaType(Class<?> javaType) {
      this.javaType = javaType;
    }

    public Class<?> getJavaType() {
      return this.javaType;
    }
  }

  @JsonInclude(Include.NON_NULL)
  public static class Field extends Format {

    private String name;
    private SchemaType type;
    private boolean optional;

    public Field() {

    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public SchemaType getType() {
      return type;
    }

    public void setType(SchemaType type) {
      this.type = type;
    }

    public boolean isOptional() {
      return optional;
    }

    public void setOptional(boolean optional) {
      this.optional = optional;
    }
  }
}
