package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelLoader.class);
  private static final String modelArchive = "./";
  private URLClassLoader urlClassLoader = null;
  private ClassLoader classLoader = null;
  private String jarPath = null;

  public ModelLoader(String jarPath, ClassLoader classLoader) throws Exception {
    if (null == jarPath) {
      return;
    }

    this.jarPath = jarPath;
    this.classLoader = classLoader;
  }

  public TaskModel newInstance(String modelName) throws Exception {
    loadJar(jarPath);
    Class<TaskModel> cls = getClassInstance(modelName);

    if (Modifier.isAbstract(cls.getModifiers())) {
      // if abstract class, we cannot instantiate
      return null;
    } else {
      Object obj = cls.newInstance();
      if (obj instanceof TaskModel) {
        return (TaskModel) obj;
      } else {
        return null;
      }
    }
  }

  public Class getClassInstance(String className)
      throws ClassNotFoundException, NoClassDefFoundError {
    LOGGER.info("Attempting to load " + className);
    return urlClassLoader.loadClass(className);

  }

  public void loadJar(String jarPath) throws Exception {
    File targetJar = new File(modelArchive + jarPath);
    if (!targetJar.exists()) {
      InputStream jarStream = getClass().getResourceAsStream("/" + jarPath);
      Files.copy(jarStream, targetJar.getAbsoluteFile().toPath());
    }

    this.urlClassLoader = new URLClassLoader(new URL[]{targetJar.toURI().toURL()},
        this.classLoader);

    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(this.classLoader, new Object[]{targetJar.toURI().toURL()});

    LOGGER.info("URL ClassLoader loaded: " + jarPath);

  }

  public void unloadJar() throws IOException {
    urlClassLoader.close();
  }

}
