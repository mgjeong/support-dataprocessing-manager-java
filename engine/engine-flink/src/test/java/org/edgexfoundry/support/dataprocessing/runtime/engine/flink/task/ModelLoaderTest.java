package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.edgexfoundry.support.dataprocessing.runtime.task.AbstractTaskModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelLoaderTest {

  private static final String TEST_DIR = ModelLoaderTest.class.getResource("/").getPath();
  private static final String TEST_CLASS = TestModel.class.getName();
  private static final String TEST_CLASS_ABSTRACT = AbstractTaskModel.class.getName();
  private static final String TEST_CLASS_INVALID = ModelLoader.class.getName();
  private static final String TEST_CLASS_NOT_EXIST = TEST_CLASS + "invalid";
  private static final String TEST_JAR = "TestModel.jar";
  private static final String TEST_JAR_PATH = TEST_DIR + "/" + TEST_JAR;

  @BeforeClass
  public static void generateTestJar() throws Exception {
    File inJarFile = new File(TEST_JAR_PATH);
    inJarFile.getParentFile().mkdirs();
    inJarFile.createNewFile();

    if (inJarFile.exists()) {
      inJarFile.delete();
    }
    inJarFile.createNewFile();

    JarOutputStream jos = new JarOutputStream(new FileOutputStream(inJarFile),
        new Manifest());
    writeClass(TEST_CLASS, jos);
    writeClass(TEST_CLASS_ABSTRACT, jos);
    writeClass(TEST_CLASS_INVALID, jos);
    jos.close();
  }

  private static void writeClass(String className, JarOutputStream os) throws Exception {
    String entry = className.replace('.', '/') + ".class";
    os.putNextEntry(new JarEntry(entry));
    Class target = Class.forName(className);
    InputStream is = target.getResourceAsStream(target.getSimpleName() + ".class");

    byte[] buf = new byte[128];
    int readLength = is.read(buf);
    while (readLength > -1) {
      os.write(buf);
      readLength = is.read(buf);
    }

    is.close();
    os.closeEntry();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    File jarFile = new File(TEST_JAR_PATH);
    if (jarFile.exists()) {
      jarFile.getParentFile().deleteOnExit();
      jarFile.delete();
    }

    File usedJarFile = new File(TEST_JAR);
    if (usedJarFile.exists()) {
      usedJarFile.delete();
    }
  }


  @Test(expected = NullPointerException.class)
  public void testNullJar() throws Exception {
    new ModelLoader(null, ClassLoader.getSystemClassLoader());
  }

  @Test
  public void testNewInstance() throws Exception {
    ModelLoader loader = new ModelLoader(TEST_JAR, ClassLoader.getSystemClassLoader());
    TaskModel loadedModel = loader.newInstance(TEST_CLASS);
    Assert.assertNotNull(loadedModel);
  }

  @Test
  public void testWithAbstractClass() throws Exception {
    ModelLoader loader = new ModelLoader(TEST_JAR, ClassLoader.getSystemClassLoader());
    TaskModel loadedModel = loader.newInstance(TEST_CLASS_ABSTRACT);

    File usedJar = new File(TEST_JAR);
    Assert.assertTrue(usedJar.exists());
    Assert.assertNull(loadedModel);
  }

  @Test(expected = ClassNotFoundException.class)
  public void testWithUnavailableClass() throws Exception {
    ModelLoader loader = new ModelLoader(TEST_JAR, ClassLoader.getSystemClassLoader());
    loader.newInstance(TEST_CLASS_NOT_EXIST);
  }
}
