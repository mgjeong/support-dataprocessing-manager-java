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
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowSink;

public class DeliverVertex implements ScriptVertex {
  WorkflowSink config;

  public DeliverVertex(WorkflowSink config) {
    this.config = config;
  }

  @Override
  public Long getId() {
    return this.config.getId();
  }

  @Override
  public String getScript() {
    Map<String, Object> properties = this.config.getConfig().getProperties();
    String dataType = ((String) properties.get("dataType")).toLowerCase();
    String dataSink = ((String) properties.get("dataSink")).replaceAll("\\s", "");

    if (!dataType.equals("ezmq") && !dataType.equals("f") && !dataType.equals("mongodb")) {
      throw new RuntimeException("Unsupported output data type" + dataType);
    }
    String[] sinkSplits = dataSink.split(":", 3);
    String[] topics = null;

    String[] names = null;

    if (sinkSplits.length == 3) {
      topics = sinkSplits[2].split(",");
    }

    String deliverAddress = sinkSplits[0] + ':' + sinkSplits[1];
    if (properties.get("name") != null) {
      names = ((String) properties.get("name")).trim().split(",");
    }

    if (names != null) {
      String result = "";
      for (String name : names) {
        result += generateScriptTailByTopic(dataType, deliverAddress, name);
      }
      return result;
    }

    if (topics == null) {
      return generateScriptTailByTopic(dataType, deliverAddress, null);
    } else {
      String scriptTail = "";
      for (String topic : topics) {
        scriptTail += generateScriptTailByTopic(dataType, deliverAddress, topic);
      }
      return scriptTail;
    }
  }

  private String generateScriptTailByTopic(String dataType, String deliverAddress, String topic) {
    String scriptTail =
        String.format("@deliver().sink(\'%s\').address(\'%s\')", dataType, deliverAddress);
    if (topic != null) {
      scriptTail += String.format(".topic(\'%s\')", topic) + '\n';
    }
    return scriptTail;
  }
}
