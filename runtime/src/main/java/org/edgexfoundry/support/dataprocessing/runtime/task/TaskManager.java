/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.db.WorkflowTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
  private static TaskManager instance = null;

  private enum TaskOwner {
    BUILT_IN, USER
  }

  private WorkflowTableManager workflowTableManager = null;
  private File customJarDirectory = null;

  private TaskManager() {
  }

  public static TaskManager getInstance() {

    if (null == instance) {
      instance = new TaskManager();
    }
    return instance;
  }

  public void initialize(String customJarPath) throws Exception {
    if (this.workflowTableManager == null) {
      this.workflowTableManager = WorkflowTableManager.getInstance();
    }

    this.customJarDirectory = new File(customJarPath);
    if (!this.customJarDirectory.exists() || !this.customJarDirectory.isDirectory()) {
      throw new InvalidParameterException("Directory not found: " + customJarPath);
    }
  }

  private List<String> getClassNames(File taskModelFile) throws Exception {
    if (taskModelFile == null || !taskModelFile.isFile()) {
      LOGGER.error("Invalid task model file received.");
      return Collections.emptyList();
    }

    LOGGER.debug("Reading {}", taskModelFile);
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
      throw e;
    }

    LOGGER.info("Found {} classNames from {}", classNames.size(), taskModelFile);
    return classNames;
  }

  private boolean insertTaskModel(TaskModel model, String jarPath, String className,
      TaskOwner taskOwner) throws Exception {
    // Check if bundle already exists
    WorkflowComponentBundle bundle = workflowTableManager
        .getWorkflowComponentBundle(model.getName(), model.getType().name());
    if (bundle != null) {
      LOGGER.error(
          "Task model for " + model.getName() + " / " + model.getType() + " already exists.");
      return false;
    }

    // make new bundle
    bundle = new WorkflowComponentBundle();
    bundle.setName(model.getName());
    bundle.setType(WorkflowComponentBundleType.PROCESSOR);
    bundle.setSubType(model.getType().name());

    bundle = updateWorkflowComponentBundle(bundle, model);
    bundle.setBundleJar(jarPath);
    bundle.setTransformationClass(className);
    bundle.setBuiltin(taskOwner == TaskOwner.BUILT_IN);

    workflowTableManager.addWorkflowComponentBundle(bundle);

    LOGGER.info("TaskModel {}/{}/{} inserted.",
        new Object[]{model.getType(), model.getName(), jarPath});
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
    uiField.setUserInput(true);

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

  public void scanBuiltinTaskModel(String absPath) throws Exception {
    if (StringUtils.isEmpty(absPath)) {
      throw new Exception("Builtin task model path is invalid.");
    }
    File taskDirectory = new File(absPath);
    if (!taskDirectory.exists() || !taskDirectory.isDirectory()) {
      throw new Exception(absPath + " is not a directory.");
    }

    File[] files = taskDirectory
        .listFiles(pathname -> pathname.getName().toLowerCase().endsWith(".jar"));
    for (File file : files) {
      insertTaskModels(file, TaskOwner.BUILT_IN);
    }
  }

  public int addCustomTask(String filename, InputStream inputStream) throws Exception {
    File customFile = saveFile(filename, inputStream);

    int added = insertTaskModels(customFile, TaskOwner.USER);
    if (added == 0) {
      boolean deleted = customFile.delete();// remove invalid custom task jar
      if (!deleted) {
        LOGGER.error("Failed to delete " + customFile.getPath());
      }
      throw new RuntimeException("No new custom tasks found in " + filename);
    } else {
      return added;
    }
  }

  public int updateCustomTask(String taskName, TaskType taskType, String filename,
      InputStream inputStream) throws Exception {
    if (StringUtils.isEmpty(taskName) || taskType == null) {
      throw new RuntimeException("Invalid task name or type.");
    }

    // Check if there exists a bundle with taskName and taskType
    WorkflowComponentBundle existingBundle = this.workflowTableManager
        .getWorkflowComponentBundle(taskName, taskType.name());
    if (existingBundle == null) {
      throw new RuntimeException(
          "Bundle for " + taskName + " / " + taskType.name() + " does not exist.");
    } else if (existingBundle.isBuiltin()) {
      throw new RuntimeException("Unable to update built in task.");
    }

    // Check if bundle is used by any workflow
    Collection<Long> workflowIds = this.workflowTableManager
        .listWorkflowIdsUsingWorkflowComponentBundle(existingBundle.getId());
    if (workflowIds.size() > 0) {
      throw new RuntimeException(
          "Bundle for " + taskName + " / " + taskType.name() + " is used in " +
              workflowIds.size() + " workflow(s).");
    }

    File customFile = saveFile(filename, inputStream);
    int updated = updateCustomTaskModel(existingBundle, customFile);
    if (updated == 0) {
      boolean deleted = customFile.delete(); // remove invalid custom task jar
      if (!deleted) {
        LOGGER.error("Failed to delete: " + customFile.getPath());
      }
      throw new RuntimeException("No custom tasks found in " + filename);
    } else {
      return updated;
    }
  }

  public void removeCustomTask(String taskName, TaskType taskType) {
    if (StringUtils.isEmpty(taskName) || taskType == null) {
      throw new RuntimeException("Invalid task name or type.");
    }

    // Check if there exists a bundle with taskName and taskType
    WorkflowComponentBundle existingBundle = this.workflowTableManager
        .getWorkflowComponentBundle(taskName, taskType.name());
    if (existingBundle == null) {
      throw new RuntimeException(
          "Bundle for " + taskName + " / " + taskType.name() + " does not exist.");
    } else if (existingBundle.isBuiltin()) {
      throw new RuntimeException("Unable to remove built in task.");
    }

    // Check if bundle is used by any workflow
    Collection<Long> workflowIds = this.workflowTableManager
        .listWorkflowIdsUsingWorkflowComponentBundle(existingBundle.getId());
    if (workflowIds.size() > 0) {
      throw new RuntimeException(
          "Bundle for " + taskName + " / " + taskType.name() + " is used in " +
              workflowIds.size() + " workflow(s).");
    }

    // Remove from database
    this.workflowTableManager.removeWorkflowComponentBundle(existingBundle.getId());

    // TODO: Is it a good idea to remove jar file if it is no longer used?
    try {
      Collection<WorkflowComponentBundle> existingBundles = this.workflowTableManager
          .listWorkflowComponentBundlesByJar(existingBundle.getBundleJar());
      if (existingBundles.isEmpty()) {
        // Remove jar file, since it is not used anymore
        File jarFile = new File(existingBundle.getBundleJar());
        if (jarFile.exists()) {
          boolean deleted = jarFile.delete();
          if (!deleted) {
            LOGGER.error("Failed to delete: " + jarFile.getPath());
          }
        }
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e); // Let the file exist, if delete fails.
    }
  }

  private int updateCustomTaskModel(WorkflowComponentBundle existingBundle, File customFile)
      throws Exception {

    // Check if jar file contains existing bundle
    List<String> classNames = getClassNames(customFile);
    if (classNames == null || classNames.isEmpty()) {
      LOGGER.info("No task models found in " + customFile.getName());
      return 0;
    }

    int updated = 0;
    for (String className : classNames) {
      TaskModel tm;

      try {
        tm = newInstance(customFile, className, TaskModel.class);
      } catch (Exception e) {
        LOGGER.error("Failed to load class " + className);
        LOGGER.error(e.getMessage(), e);
        continue;
      } catch (Error e) {
        LOGGER.error("(ERROR!) Failed to load class " + className);
        LOGGER.error(e.getMessage(), e);
        continue;
      }

      if (tm == null) {
        continue;
      } else if (tm.getName().equalsIgnoreCase(existingBundle.getName())
          && tm.getType().name().equalsIgnoreCase(existingBundle.getSubType())) {
        existingBundle.setBundleJar(customFile.getAbsolutePath());
        existingBundle.setTransformationClass(tm.getClass().getCanonicalName());
        this.workflowTableManager.addOrUpdateWorkflowComponentBundle(existingBundle);
        updated++;
        break;
      }
    }

    return updated;
  }

  private File saveFile(String filename, InputStream inputStream) throws IOException {
    if (StringUtils.isEmpty(filename)) {
      throw new RuntimeException("Invalid filename");
    } else if (inputStream == null) {
      throw new RuntimeException("Invalid file input stream.");
    }

    File customFile = new File(customJarDirectory, filename);
    // append number to filename if filename already exists
    for (int i = 0; customFile.exists(); i++) {
      customFile = new File(customJarDirectory, i + "_" + filename);
    }

    Files.copy(inputStream, customFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    IOUtils.closeQuietly(inputStream);
    return customFile;
  }

  private int insertTaskModels(File createdFile, TaskOwner taskOwner) throws Exception {
    List<String> classNames = getClassNames(createdFile);
    if (classNames == null || classNames.isEmpty()) {
      LOGGER.info("No task models found in " + createdFile.getName());
      return 0;
    }

    int added = 0;

    for (String className : classNames) {
      TaskModel tm;

      try {
//        tm = JarLoader.newInstance(createdFile, className, AbstractTaskModel.class);
        tm = newInstance(createdFile, className, TaskModel.class);
      } catch (Exception e) {
        LOGGER.error("Failed to load class " + className);
        LOGGER.error(e.getMessage(), e);
        continue;
      } catch (Error e) {
        LOGGER.error("(ERROR!) Failed to load class " + className);
        LOGGER.error(e.getMessage(), e);
        continue;
      }

      if (tm == null) {
        continue;
      }

      // insert
      try {
        boolean isAdded = insertTaskModel(tm, createdFile.getAbsolutePath(),
            tm.getClass().getCanonicalName(), taskOwner);

        if (isAdded) {
          added++;
        }
      } catch (Exception e) {
        LOGGER.error("Failed to insert task model( " + tm.getName() + "): "
            + e.getMessage(), e);
      }
    }
    return added;
  }

  public <T> T newInstance(File jarFile, String className, Class<T> clazz)
      throws Exception {
    if (jarFile == null || !jarFile.exists()) {
      throw new RuntimeException("Invalid jar file");
    } else if (StringUtils.isEmpty(className)) {
      throw new RuntimeException("Invalid classname for " + jarFile.getName());
    }

    URL url = jarFile.toURI().toURL();
    URLClassLoader taskClassLoader = URLClassLoader.newInstance(new URL[]{url},
        this.getClass().getClassLoader());

    Class classToInstantiate = Class.forName(className, true, taskClassLoader);
    if (Modifier.isAbstract(classToInstantiate.getModifiers())) {
      LOGGER.info(className + " is an abstract class.");
      return null;
    } else {
      if (hasParameterlessConstructor(classToInstantiate)) {
        Object o = classToInstantiate.newInstance();
        if (clazz.isInstance(o)) {
          return clazz.cast(o);
        } else {
          LOGGER.info(className + " is not an instance of " + clazz.getCanonicalName());
          return null;
        }
      } else {
        LOGGER.info(className + " does not have parameter-less constructor");
        return null;
      }
    }
  }

  private boolean hasParameterlessConstructor(Class clazz) {
    for (Constructor constructor : clazz.getConstructors()) {
      if (constructor.getParameterCount() == 0) {
        return true;
      }
    }

    return false;
  }
}
