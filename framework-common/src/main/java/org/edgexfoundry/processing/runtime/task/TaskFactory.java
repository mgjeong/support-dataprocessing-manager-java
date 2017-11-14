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
package org.edgexfoundry.processing.runtime.task;

import com.sec.processing.framework.db.TaskTableManager;
import com.sec.processing.framework.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public TaskModel createTaskModelInst(TaskType type, String name, ClassLoader classLoader) throws Exception {
        try {
            LOGGER.info("Creating task model for " + type.name() + " / " + name);

            // Select task from database
            List<Map<String, String>> resArray = TaskTableManager.getInstance().getTaskByName(name);
            if (resArray.size() == 0) {
                throw new RuntimeException("Task jar info for " + name + " does not exist in database.");
            }

            // Get jar file info
            String jarPath = resArray.get(0).get(TaskTableManager.Entry.path.name());
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
