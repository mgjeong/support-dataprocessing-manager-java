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
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.connection.HTTP;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.response.TaskResponseFormat;
import org.edgexfoundry.support.dataprocessing.runtime.db.TaskTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TaskFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);

    private static TaskFactory instance = null;

    public static synchronized TaskFactory getInstance() {
        if (instance == null) {
            instance = new TaskFactory();
        }
        return instance;
    }

    private TaskFactory() {

    }

    /*fromWeb*/
    private String getTaskJarFilename(TaskType type, String name) {

        // Set the arguments.
        Map<String, String> args = new HashMap<>();
        args.put(TaskTableManager.Entry.type.name(), type.name());;
        args.put(TaskTableManager.Entry.name.name(), name);

        // Initialize HTTP Conenction.
        HTTP httpClient = new HTTP();
        httpClient.initialize(Settings.WEB_ADDRESS, Settings.WEB_PORT, Settings.WEB_SCHEMA);

        // Send Request.
        JsonElement element = httpClient.get("/analytics/v1/task/jar/", args);
        TaskResponseFormat response = new Gson().fromJson(element.toString(), TaskResponseFormat.class);

        return response.getTask().get(0).getJar();

    }

    private boolean getTaskJarFile(String jar) {

        if(null == jar)
            return false;

        HTTP httpClient = new HTTP();
        httpClient.initialize(Settings.WEB_ADDRESS, Settings.WEB_PORT, Settings.WEB_SCHEMA);
        return httpClient.get("/analytics/v1/task/jar/get/" + jar, null, "/runtime/ha/", jar);
    }

    public TaskModel createTaskModelInst(TaskType type, String name, ClassLoader classLoader) throws Exception {
        try {
            String jar = null;
            if(null == (jar = getTaskJarFilename(type, name))) {
                return null;
            }
            if(false == getTaskJarFile(jar)) {
                return null;
            }

            LOGGER.info("Creating task model for " + type.name() + " / " + name);

            // Select task from database
            List<Map<String, String>> resArray = TaskTableManager.getInstance().getTaskByName(name);
            if (resArray.size() == 0) {
                throw new RuntimeException("Task jar info for " + name + " does not exist in database.");
            }

            // Get jar file info
            String jarPath = "/runtime/ha/" + jar;
//            String jarPath = resArray.get(0).get(TaskTableManager.Entry.path.name());
            String className = resArray.get(0).get(TaskTableManager.Entry.classname.name());

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
