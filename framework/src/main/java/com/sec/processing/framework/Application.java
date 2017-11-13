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
package com.sec.processing.framework;

import com.sec.processing.framework.job.JobManager;
import com.sec.processing.framework.task.TaskManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private static TaskManager taskManager = null;
    private static JobManager jobManager = null;

    private static void initialize() {

        try {
            taskManager = TaskManager.getInstance();

            taskManager.scanTaskModel(Settings.FW_JAR_PATH);

            jobManager = JobManager.getInstance();
            jobManager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void terminate() {
        // TaskManager related
        if (taskManager != null) {
            taskManager.terminate();
        }
    }

    public static void main(String[] args) throws Exception {
        initialize();

        SpringApplication.run(Application.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                terminate();
            }
        });
    }
}
