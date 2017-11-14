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

import com.sec.processing.framework.Settings;
import com.sec.processing.framework.data.model.error.ErrorFormat;
import com.sec.processing.framework.data.model.error.ErrorType;
import com.sec.processing.framework.data.model.task.TaskFormat;
import com.sec.processing.framework.db.TaskTableManager;
import com.sec.processing.framework.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class TaskManager implements DirectoryChangeEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
    private static TaskManager instance = null;

    private static DirectoryWatcher directoryWatcher = null;
    private static TaskTableManager taskTable = null;

    private static final int DEFAULTTASK = 0;
    private static final int USERTASK = 1;

    private TaskManager() {
    }

    public static TaskManager getInstance() {

        if (null == instance) {
            instance = new TaskManager();
            instance.initialize();
        }
        return instance;
    }

    private void startDirectoryWatcher(String absPath) throws InvalidParameterException {
        if (null == absPath) {
            throw new InvalidParameterException("Path is null.");
        }

        if (null != this.directoryWatcher) {
            stopDirectoryWatcher();
        }

        this.directoryWatcher = new DirectoryWatcher(absPath, this);
    }

    public void initialize() {
        try {
            taskTable = TaskTableManager.getInstance();
            startDirectoryWatcher(Settings.CUSTOM_JAR_PATH);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void stopDirectoryWatcher() {
        this.directoryWatcher.stopWatcher();
        try {
            this.directoryWatcher.join(3000L);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        this.directoryWatcher = null;
    }

    public void terminate() {
        if (this.directoryWatcher != null) {
            LOGGER.info("Shutting down directory watcher.");
            stopDirectoryWatcher();
        }

        LOGGER.info("TaskManager terminated.");
    }

    public TaskFormat getTaskModel(String name, TaskType type) {
        List<TaskFormat> tasks = getTaskModelList();
        TaskFormat ret = null;
        for (TaskFormat task : tasks) {
            if (task.getName().equalsIgnoreCase(name) && task.getType() == type) {
                ret = new TaskFormat(task);
                break;
            }
        }
        return ret;
    }

    public List<TaskFormat> getTaskModel(String name) {
        List<TaskFormat> tasks = getTaskModelList();
        List<TaskFormat> ret = new ArrayList<>();
        for (TaskFormat task : tasks) {
            if (task.getName().equalsIgnoreCase(name)) {
                ret.add(new TaskFormat(task));
            }
        }
        return ret;
    }

    public List<TaskFormat> getTaskModel(TaskType type) {
        List<TaskFormat> tasks = getTaskModelList();
        List<TaskFormat> ret = new ArrayList<>();
        for (TaskFormat task : tasks) {
            if (task.getType() == type) {
                ret.add(new TaskFormat(task));
            }
        }
        return ret;
    }

    public List<TaskFormat> getTaskModelList() {
        List<TaskFormat> tasks = new ArrayList<>();
        List<Map<String, String>> taskList = null;
        try {
            taskList = taskTable.getAllTaskProperty();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        for (Map<String, String> result : taskList) {
            String type = result.get(TaskTableManager.Entry.type.name());
            String name = result.get(TaskTableManager.Entry.name.name());
            String params = result.get(TaskTableManager.Entry.param.name());

            TaskFormat task = new TaskFormat(TaskType.getType(type), name, params);
            tasks.add(task);
        }

        return tasks;
    }


    private List<String> getTaskModelNames(String fileName) {
        // Validate file name
        if (fileName == null || fileName.isEmpty() || !fileName.endsWith(".jar")) {
            LOGGER.error("Invalid file name received.");
            return null;
        }

        // Validate file
        File f = new File(fileName);
        if (f == null || !f.exists() || !f.isFile()) {
            LOGGER.error("{} does not exist or is invalid.", fileName);
            return null;
        }

        LOGGER.info("Reading {}", fileName);

        // Collect all class names available inside jar
        List<String> classNames = new ArrayList<>();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new FileInputStream(f));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                    String className = entry.getName().replace('/', '.');
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return classNames;
    }

    private boolean updateTaskModel(TaskModel model, String jarPath, String className, int removable) {
        try {
            taskTable.insertTask(model.getType().toString(),
                    model.getName(),
                    model.getDefaultParam().toString(),
                    jarPath,
                    className,
                    removable);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void updateTasksFromJar(String absJarPath, List<String> classNames, int removable) {
        TaskModelLoader loader = null;
        try {
            loader = new TaskModelLoader(absJarPath, this.getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (String s : classNames) {
            try {
                // Add to database if task model
                TaskModel tm = loader.newInstance(s);
                if (tm == null || !(tm instanceof TaskModel)) {
                    LOGGER.error("Failed to instantiate " + s + ". Possibly an abstract class.");
                    continue;
                }
                updateTaskModel(tm, absJarPath, tm.getClass().getCanonicalName(), removable);
                LOGGER.info("TaskModel {}/{}/{} updated.", new Object[] {tm.getType(), tm.getName(), absJarPath});
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate " + s + ". Possibly an abstract class.");
                LOGGER.error(e.getMessage());
            }
        }
    }

    public boolean scanTaskModel(String absPath) {
        if (absPath == null) {
            LOGGER.error("invalid path.");
            return false;
        }

        ArrayList<String> fileNames = this.directoryWatcher.scanFile(absPath);
        for (String fileName : fileNames) {
            List<String> classNames = getTaskModelNames(fileName);
            if (null != classNames) {
                updateTasksFromJar(fileName, classNames, DEFAULTTASK);
            }
        }
        return true;
    }

    public ErrorFormat fileCreatedEventReceiver(String fileName) {
        if (null == fileName) {
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS);
        }

        List<String> classNames = getTaskModelNames(fileName);
        if (null == classNames || classNames.isEmpty()) {
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "This file does not have class.");
        }

        if (null != classNames) {
            updateTasksFromJar(fileName, classNames, USERTASK);
        }
        return new ErrorFormat();
    }

    public ErrorFormat fileRemovedEventReceiver(String fileName) {
        // Validate file name
        if (fileName == null || fileName.isEmpty() || !fileName.endsWith(".jar")) {
            LOGGER.error("Invalid file name received.");
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "Invalid file name received.");
        }

        // Delete from database
        try {
            taskTable.deleteTaskByPath(fileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ErrorFormat();
    }

    public ErrorFormat addTask(String name, byte[] data) {
        String absPath = Settings.CUSTOM_JAR_PATH + name;
        ErrorFormat response;
        try {
            Path path = Paths.get(absPath);
            Files.write(path, data);

            // Insert Information of jar file to DB
            response = fileCreatedEventReceiver(absPath);
            if (response.isError()) {
                try {
                    Files.delete(path);
                    LOGGER.info(path + " was deleted.");
                } catch (Exception e) {
                    return new ErrorFormat(ErrorType.DPFW_ERROR_PERMISSION, e.getMessage());
                }
            }
        } catch (Exception e) {
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage());
        }
        return response;
    }

    public ErrorFormat deleteTask(TaskType type, String name) {
        Map<String, String> res;
        List<Map<String, String>> resList = null;
        String jarPath = null;
        int size = 0;

        try {
            resList = taskTable.getTaskByTypeAndName(type.toString(), name);
        } catch (Exception e) {
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, e.getMessage());
        }

        if (null == resList || 0 >= resList.size()) {
            LOGGER.info("No Tasks.");
            return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "No Tasks.");
        } else {
            res = resList.get(0);
            String removable = res.get(TaskTableManager.Entry.removable.name()).toString();
            if (DEFAULTTASK == Integer.parseInt(removable)) {
                LOGGER.info("Can't delete default tasks.");
                return new ErrorFormat(ErrorType.DPFW_ERROR_PERMISSION, "Can't delete default tasks.");
            }
        }

        try {
            jarPath = res.get(TaskTableManager.Entry.path.name()).toString();
            size = taskTable.getJarReferenceCount(jarPath);
            // remove from DB
            if (size > 1) {
                taskTable.deleteTaskByTypeAndName(type.toString(), name);

                LOGGER.info(type.toString() + "/" + name + " was deleted.");
            } else if (size == 1) {
                // And Remove file if not exist another task in same jar file.
                taskTable.deleteTaskByTypeAndName(type.toString(), name);

                if (null != jarPath) {
                    Path path = Paths.get(jarPath);
                    try {
                        Files.delete(path);
                        LOGGER.info(jarPath + " was deleted.");
                    } catch (Exception e) {
                        return new ErrorFormat(ErrorType.DPFW_ERROR_PERMISSION, e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            return new ErrorFormat(ErrorType.DPFW_ERROR_DB, e.getMessage());
        }
        return new ErrorFormat();
    }
}
