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

import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.FlinkEngine;
import org.edgexfoundry.support.dataprocessing.runtime.engine.kapacitor.KapacitorEngine;

public class EngineFactory {

//    protected EngineFactory() { }

    public static Engine createEngine(EngineType engineType) {
        final Engine engine;
        final Integer port = 8081;

        switch (engineType) {
            case Flink:
                engine = new KapacitorEngine("localhost", 9092);   // TODO: Remove hard-code
                break;
            case Spark: // TODO
            default:
                throw new RuntimeException("Unsupported engine type selected.");
        }
        return engine;
    }
}

