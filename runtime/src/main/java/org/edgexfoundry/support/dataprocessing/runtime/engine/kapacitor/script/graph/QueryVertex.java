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
package org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.script.graph;

import java.util.Map;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowProcessor;

public class QueryVertex implements ScriptVertex {

  WorkflowProcessor config;

  public QueryVertex(WorkflowProcessor config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public String getScript() {
    if (!this.config.getName().equalsIgnoreCase("query")) {
      throw new IllegalStateException("Cannot handle types except query");
    }
    Map<String, Object> properties = this.config.getConfig().getProperties();
    StringBuilder builder = new StringBuilder();
    builder.append("var id");
    builder.append(getId());
    builder.append('=');

    Object scriptBodyObject = properties.get("request");
    if (scriptBodyObject instanceof String) {
      builder.append(((String) scriptBodyObject).replace("<", "\'").replace(">", "\'") + "\n");
    } else {
      throw new RuntimeException("Request should be String type");
    }
    return builder.toString();
  }
}