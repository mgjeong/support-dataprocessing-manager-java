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
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorFormat;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.error.ErrorType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.ComponentUISpecification.UIField.UIFieldType;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.topology.TopologyComponentBundle.TopologyComponentType;
import org.edgexfoundry.support.dataprocessing.runtime.db.TopologyTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.TaskModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManager implements DirectoryChangeEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
  private static TaskManager instance = null;

  private static DirectoryWatcher directoryWatcher = null;

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
        if (!entry.isDirectory() && entry.getName().endsWith(".class") && !entry.getName()
            .contains("$")) {
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

  private boolean updateTaskModel(TaskModel model, String jarPath, String className,
      int removable) {
    try {
      // Check if bundle already exists
      TopologyComponentBundle bundle = TopologyTableManager.getInstance()
          .getTopologyComponentBundle(model.getName(), TopologyComponentType.PROCESSOR,
              "DPFW");
      if (bundle == null) {
        // make new bundle
        bundle = new TopologyComponentBundle();
        bundle.setName(model.getName());
        bundle.setType(TopologyComponentType.PROCESSOR);
        //bundle.setSubType(model.getType().name());
        bundle.setSubType("DPFW");
      }
      bundle = updateTopologyComponentBundle(bundle, model);
      bundle.setBundleJar(jarPath);
      bundle.setTransformationClass(className);
      bundle.setBuiltin(removable == DEFAULTTASK);

      if (bundle.getId() == null) {
        TopologyTableManager.getInstance().addTopologyComponentBundle(bundle);
      } else {
        TopologyTableManager.getInstance().addOrUpdateTopologyComponentBundle(bundle);
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private TopologyComponentBundle updateTopologyComponentBundle(TopologyComponentBundle bundle,
      TaskModel model) {
    if (model.getName().equalsIgnoreCase("QUERY")) {
      bundle.setStreamingEngine("KAPACITOR");
    } else {
      bundle.setStreamingEngine("FLINK");
    }

    // UI component
    ComponentUISpecification uiSpecification = new ComponentUISpecification();
    TaskModelParam params = model.getDefaultParam();
    for (Map.Entry<String, Object> param : params.entrySet()) {
      // recursively add ui field
      addTaskUIField(param.getKey(), param.getValue(), uiSpecification);
    }

    // Add inrecord, outrecord
    UIField inrecord = new UIField();
    inrecord.setUiName("inrecord");
    inrecord.setFieldName("inrecord");
    inrecord.setTooltip("Enter inrecord");
    inrecord.setOptional(false);
    inrecord.setType(UIFieldType.ARRAYSTRING);
    uiSpecification.addUIField(inrecord);

    UIField outrecord = new UIField();
    outrecord.setUiName("outrecord");
    outrecord.setFieldName("outrecord");
    outrecord.setTooltip("Enter outrecord");
    outrecord.setOptional(false);
    outrecord.setType(UIFieldType.ARRAYSTRING);
    uiSpecification.addUIField(outrecord);

    bundle.setTopologyComponentUISpecification(uiSpecification);
    return bundle;
  }

  private void addTaskUIField(String key, Object value, ComponentUISpecification uiSpecification) {
    if (value instanceof TaskModelParam) {
      for (Map.Entry<String, Object> child : ((TaskModelParam) value).entrySet()) {
        addTaskUIField(key + "/" + child.getKey(), child.getValue(), uiSpecification);
      }
    } else {
      UIField uiField = new UIField();
      uiField.setUiName(key);
      uiField.setFieldName(key);
      uiField.setTooltip("Enter " + key);
      uiField.setOptional(false);
      if (value instanceof Number) {
        uiField.setType(UIFieldType.NUMBER);
      } else {
        uiField.setType(UIFieldType.STRING);
      }
      uiSpecification.addUIField(uiField);
    }
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
      if (null != classNames) {
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
