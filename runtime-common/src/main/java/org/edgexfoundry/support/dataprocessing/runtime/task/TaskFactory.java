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
package org.edgexfoundry.support.dataprocessing.runtime.task;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.TaskResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.TaskTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public final class TaskFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);

    private static TaskFactory instance = null;

    private static HTTP httpClient = null;

    public static synchronized TaskFactory getInstance() {
        if (instance == null) {
            instance = new TaskFactory();
        }
        return instance;
    }

    private TaskFactory() {

    }

    /*fromWeb*/
    private TaskResponseFormat getTaskJarInfo(TaskType type, String name) {

        // Set the arguments.
        Map<String, String> args = new HashMap<>();
        args.put(TaskTableManager.Entry.type.name(), type.name());
        args.put(TaskTableManager.Entry.name.name(), name);

        LOGGER.debug(TaskTableManager.Entry.type.name() + " " + type.name());
        LOGGER.debug(TaskTableManager.Entry.name.name() + " " + name);

        JsonElement element = null;
        // Send Request.
        if(null != this.httpClient) {
            element = this.httpClient.get("/analytics/v1/task/info/", args);
            LOGGER.debug(element.toString());
            TaskResponseFormat response = new Gson().fromJson(element.toString(), TaskResponseFormat.class);

            return response;
        } else {
            LOGGER.error("Error : host cannot reachable.", httpClient);
            return null;
        }
    }

    private boolean getTaskJarFile(String jar) {

        if(null == jar)
            return false;

        return this.httpClient.get("/analytics/v1/task/jar/" + jar, null, "/runtime/ha/", jar);
    }



    public TaskModel createTaskModelInst(TaskType type, String name, ClassLoader classLoader, String host) throws Exception {

        if(null == host)
        {
            throw new Exception("Host cannot reachable.");
        } else {
            this.httpClient = new HTTP().initialize(host, "http");
        }

        try {
            TaskResponseFormat response = getTaskJarInfo(type, name);
            String jar = response.getTask().get(0).getJar();

            if(null == jar) {
                return null;
            }
            if(false == getTaskJarFile(jar)) {
                return null;
            }

            LOGGER.info("Creating task model for " + type.name() + " / " + name);

            // Get jar file info
            String jarPath = "/runtime/ha/" + jar;
            String className = response.getTask().get(0).getClassName();

            // Make instance
            TaskModelLoader modelLoader = new TaskModelLoader(jarPath, classLoader);
            return modelLoader.newInstance(className);
        } catch (Exception e) {
            LOGGER.error("Failed to create new model " + type.name() + " / " + name + ".");
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
