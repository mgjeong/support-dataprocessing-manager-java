/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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

import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowData;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.KapacitorEngine;

public class EngineFactory {

  /**
   * Create an request generator depending on engine type
   * Generally, it will make a job/request which is runnable on actual engines respectively.
   * @param engineType Type of the engine to use
   * @param host hostname of the engine
   * @param port port number of the engine
   * @return Request generator of specified engine
   */
  public static Engine createEngine(WorkflowData.EngineType engineType, String host, Integer port) {
    final Engine engine;

    switch (engineType) {
      case FLINK:
        engine = new FlinkEngine(host, port);
        break;
      case KAPACITOR:
        engine = new KapacitorEngine(host, port);
        break;
      default:
        throw new RuntimeException("Unsupported engine type selected.");
    }
    return engine;
  }
}

