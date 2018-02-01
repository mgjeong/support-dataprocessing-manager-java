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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.Settings;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.edgexfoundry.support.dataprocessing.runtime.util.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManager implements FileAlterationListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
  private static TaskManager instance = null;

  private static final int DEFAULTTASK = 0;
  private static final int USERTASK = 1;

  private WorkflowTableManager workflowTableManager = null;
  private FileAlterationMonitor directoryWatcher = null;
  private File customJarDirectory = null;

  private TaskManager() {
  }

  public static TaskManager getInstance() {

    if (null == instance) {
      instance = new TaskManager();
    }
    return instance;
  }

  private void startDirectoryWatcher(String absPath) throws Exception {
    if (null == absPath) {
      throw new InvalidParameterException("Path is null.");
    }

    // stop directory watcher if running
    stopDirectoryWatcher();

    // start directory watcher
    customJarDirectory = new File(absPath);
    if (!customJarDirectory.exists()) {
      throw new InvalidParameterException("Directory not found: " + absPath);
    }

    FileAlterationObserver observer = new FileAlterationObserver(customJarDirectory,
        pathname -> pathname.getName().toLowerCase().endsWith(".jar"));
    observer.addListener(this);

    directoryWatcher = new FileAlterationMonitor(Settings.DIRECTORY_WATCHER_SCAN_INTERVAL);
    directoryWatcher.addObserver(observer);
    directoryWatcher.start();
  }

  public void initialize(String customJarPath) throws Exception {
    this.workflowTableManager = WorkflowTableManager.getInstance();

    startDirectoryWatcher(customJarPath);
  }

  private void stopDirectoryWatcher() {
    if (directoryWatcher != null) {
      try {
        directoryWatcher.stop();
        directoryWatcher = null;
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }

  public void terminate() {
    if (this.directoryWatcher != null) {
      LOGGER.info("Shutting down directory watcher.");
      stopDirectoryWatcher();
    }

    LOGGER.info("TaskManager terminated.");
  }

  private List<String> getTaskModels(File taskModelFile) {
    if (taskModelFile == null || !taskModelFile.isFile()) {
      LOGGER.error("Invalid task model file received.");
      return Collections.emptyList();
    }

    LOGGER.info("Reading {}", taskModelFile);
    // Collect all class names available inside jar
    List<String> classNames = new ArrayList<>();
    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(taskModelFile))) {
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

    LOGGER.info("Found {} classNames from {}", classNames.size(), taskModelFile);
    return classNames;
  }

  private boolean updateTaskModel(TaskModel model, String jarPath, String className,
      int removable) {
    try {
      // Check if bundle already exists
      WorkflowComponentBundle bundle = workflowTableManager
          .getWorkflowComponentBundle(model.getName(), WorkflowComponentBundleType.PROCESSOR,
              model.getType().name());
      if (bundle == null) {
        // make new bundle
        bundle = new WorkflowComponentBundle();
        bundle.setName(model.getName());
        bundle.setType(WorkflowComponentBundleType.PROCESSOR);
        bundle.setSubType(model.getType().name());
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

  public void scanTaskModel(String absPath) {
    if (StringUtils.isEmpty(absPath)) {
      LOGGER.error("invalid path.");
      return;
    }
    File taskDirectory = new File(absPath);
    if (!taskDirectory.isDirectory()) {
      LOGGER.error(absPath + " is not a directory.");
      return;
    }

    File[] files = taskDirectory
        .listFiles(pathname -> pathname.getName().toLowerCase().endsWith(".jar"));
    for (File file : files) {
      updateTaskModels(file, DEFAULTTASK);
    }
  }

  public void uploadCustomTask(String filename, InputStream inputStream) throws Exception {
    if (StringUtils.isEmpty(filename)) {
      throw new RuntimeException("Invalid filename");
    } else if (inputStream == null) {
      throw new RuntimeException("Invalid file input stream.");
    }

    File tmp = new File(customJarDirectory, filename + ".tmp");
    try {
      Files.copy(inputStream, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
      IOUtils.closeQuietly(inputStream);

      if (!validateCustomTask(tmp)) {
        throw new RuntimeException("No custom tasks found in " + filename);
      }

      // otherwise, rename file so that directory watcher can detect it
      File dest = new File(customJarDirectory, filename);
      tmp.renameTo(dest);
    } finally {
      tmp.delete();
    }
  }

  private boolean validateCustomTask(File file) {
    if (file == null || !file.isFile()) {
      return false;
    }

    try {
      List<String> classNames = getTaskModels(file);
      if (classNames == null || classNames.isEmpty()) {
        return false;
      }
      return true;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    }
  }

  private void updateTaskModels(File createdFile, int removable) {
    List<String> classNames = getTaskModels(createdFile);
    if (classNames == null || classNames.isEmpty()) {
      LOGGER.info("No task models found in " + createdFile.getName());
      return;
    }

    try {
      for (String className : classNames) {
        TaskModel tm = JarLoader
            .newInstance(createdFile, className, TaskModel.class);
        if (tm == null || !(tm instanceof TaskModel)) {
          LOGGER.error("Failed to instantiate " + className + ". Possibly an abstract class.");
          continue;
        }
        updateTaskModel(tm, createdFile.getAbsolutePath(), tm.getClass().getCanonicalName(),
            removable);
        LOGGER.info("TaskModel {}/{}/{} updated.",
            new Object[]{tm.getType(), tm.getName(), createdFile.getAbsolutePath()});
      }

    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  @Override
  public void onStart(FileAlterationObserver fileAlterationObserver) {
    // Nothing
  }

  @Override
  public void onDirectoryCreate(File file) {
    // ignored
  }

  @Override
  public void onDirectoryChange(File file) {
    // ignored
  }

  @Override
  public void onDirectoryDelete(File file) {
    // ignored
  }

  @Override
  public void onFileCreate(File file) {
    if (file == null || !file.isFile()) {
      LOGGER.error("File is invalid.");
      return;
    }
    LOGGER.info("File created: " + file.getAbsolutePath());
    updateTaskModels(file, USERTASK);
  }

  @Override
  public void onFileChange(File file) {
    // Nothing
  }

  @Override
  public void onFileDelete(File file) {
    if (file == null) {
      LOGGER.error("File is invalid.");
      return;
    }

    LOGGER.info("File deleted: " + file.getAbsolutePath());
  }

  @Override
  public void onStop(FileAlterationObserver fileAlterationObserver) {
    // Nothing
  }
}
