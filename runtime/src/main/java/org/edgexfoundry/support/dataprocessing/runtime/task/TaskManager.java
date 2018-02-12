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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManager implements DirectoryChangeEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
  private static TaskManager instance = null;

  private static final int DEFAULTTASK = 0;
  private static final int USERTASK = 1;

  private WorkflowTableManager workflowTableManager = null;
  private DirectoryWatcher directoryWatcher = null;

  private TaskManager() {
  }

  public static TaskManager getInstance() {

    if (null == instance) {
      instance = new TaskManager();
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
    this.directoryWatcher.start();
  }

  public void initialize() {
    this.workflowTableManager = WorkflowTableManager.getInstance();

    startDirectoryWatcher(Settings.CUSTOM_JAR_PATH);
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

  private List<String> getTaskModelNames(String fileName) {
    // Validate file name
    if (fileName == null || fileName.isEmpty() || !fileName.endsWith(".jar")) {
      LOGGER.debug("Invalid file name received.");
      return Collections.emptyList();
    }

    // Validate file
    File f = new File(fileName);
    if (f == null || !f.exists() || !f.isFile()) {
      LOGGER.debug("{} does not exist or is invalid.", fileName);
      return Collections.emptyList();
    }

    LOGGER.info("Reading {}", fileName);
    // Collect all class names available inside jar
    List<String> classNames = new ArrayList<>();
    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(f))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        // Extract class files
        if (!entry.isDirectory()
            && entry.getName().endsWith(".class")
            && !entry.getName().contains("$")) {
          String className = entry.getName().replace('/', '.');
          classNames.add(className.substring(0, className.length() - ".class".length()));
        }
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }

    return classNames;
  }

  private boolean updateTaskModel(TaskModel model, String jarPath, String className,
      int removable) {
    try {
      // Check if bundle already exists
      WorkflowComponentBundle bundle = workflowTableManager
          .getWorkflowComponentBundle(model.getName(), WorkflowComponentBundleType.PROCESSOR,
              "DPFW");
      if (bundle == null) {
        // make new bundle
        bundle = new WorkflowComponentBundle();
        bundle.setName(model.getName());
        bundle.setType(WorkflowComponentBundleType.PROCESSOR);
        //bundle.setSubType(model.getType().name());
        bundle.setSubType("DPFW");
      }
      bundle = updateWorkflowComponentBundle(bundle, model);
      bundle.setBundleJar(jarPath);
      bundle.setTransformationClass(className);
      bundle.setBuiltin(removable == DEFAULTTASK);

      if (bundle.getId() == null) {
        workflowTableManager.addWorkflowComponentBundle(bundle);
      } else {
        workflowTableManager.addOrUpdateWorkflowComponentBundle(bundle);
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private WorkflowComponentBundle updateWorkflowComponentBundle(WorkflowComponentBundle bundle,
      TaskModel model) {
    if (model.getName().equalsIgnoreCase("QUERY")) {
      bundle.setStreamingEngine("KAPACITOR");
    } else {
      bundle.setStreamingEngine("FLINK");
    }

    // UI component
    ComponentUISpecification uiSpecification = new ComponentUISpecification();
    List<Field> fields = new ArrayList<>();

    Class clazz = model.getClass();
    do {
      Field[] declaredFields = clazz.getDeclaredFields();
      for (Field declaredField : declaredFields) {
        if (declaredField.isAnnotationPresent(TaskParam.class)) {
          fields.add(declaredField);
        }
      }

      Class newClazz = clazz.getSuperclass();
      if (clazz == newClazz) {
        break;
      } else {
        clazz = newClazz;
      }
    } while (clazz != null);

    for (Field field : fields) {
      TaskParam taskParam = field.getAnnotation(TaskParam.class);
      UIField uiField = makeUIField(field, taskParam);
      if (uiField != null) {
        uiSpecification.addUIField(uiField);
      }
    }

    bundle.setWorkflowComponentUISpecification(uiSpecification);
    return bundle;
  }

  private UIField makeUIField(Field taskField, TaskParam taskParam) {
    UIField uiField = new UIField();
    uiField.setUiName(taskParam.uiName());
    uiField.setType(taskParam.uiType());
    uiField.setOptional(taskParam.isOptional());
    uiField.setDefaultValue(taskParam.defaultValue());
    uiField.setTooltip(taskParam.tooltip());
    uiField.setFieldName(taskParam.key());
    uiField.setUserInput(!taskField.getType().isEnum());

    if (taskField.getType().isEnum()) {
      Object[] options = taskField.getType().getEnumConstants();
      for (Object option : options) {
        if (option != null) {
          uiField.addOption(option.toString());
        }
      }
    }
    return uiField;
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
        LOGGER.info("TaskModel {}/{}/{} updated.",
            new Object[]{tm.getType(), tm.getName(), absJarPath});
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
      if (classNames != null && !classNames.isEmpty()) {
        updateTasksFromJar(fileName, classNames, DEFAULTTASK);
      }
    }
    return true;
  }

  @Override
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

  @Override
  public ErrorFormat fileRemovedEventReceiver(String fileName) {
    // Validate file name
    if (fileName == null || fileName.isEmpty() || !fileName.endsWith(".jar")) {
      LOGGER.error("Invalid file name received.");
      return new ErrorFormat(ErrorType.DPFW_ERROR_INVALID_PARAMS, "Invalid file name received.");
    }

    // Delete from database
    try {
      // TODO:
      //taskTable.deleteTaskByPath(fileName);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return new ErrorFormat();
  }
}
