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
package org.edgexfoundry.support.dataprocessing.runtime.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData.EngineType;

public final class EngineManager {

  private final Map<String, Engine> engineMap;

  private static EngineManager instance = null;

  public synchronized static EngineManager getInstance() {
    if (instance == null) {
      instance = new EngineManager();
    }
    return instance;
  }

  private EngineManager() {
    this.engineMap = new ConcurrentHashMap<>();
  }

  public synchronized Engine getEngine(String host, int port, EngineType engineType) {
    String key = getKey(host, port, engineType);
    Engine engine = engineMap.get(key);
    if (engine == null) {
      engine = EngineFactory.createEngine(engineType, host, port);
      engineMap.put(key, engine);
    }
    return engine;
  }

  private String getKey(String host, int port, EngineType engineType) {
    return String.format("%s_%s_%d", engineType.name(), host, port);
  }
}
