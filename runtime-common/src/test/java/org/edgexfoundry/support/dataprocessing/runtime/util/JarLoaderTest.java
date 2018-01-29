package org.edgexfoundry.support.dataprocessing.runtime.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JarLoaderTest {

  private static final File testDir = new File("./testDir/");
  private static final File testTaskModelJar = new File(testDir, "test.jar");

  @BeforeClass
  public static void initialize() throws Exception {
    if (testDir.exists() && !testDir.delete() || !testDir.mkdirs()) {
      throw new Exception("Failed to initialize test directory: " + testDir.getAbsolutePath());
    }

    testTaskModelJar.deleteOnExit();
    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(testTaskModelJar),
        new Manifest())) {
      String entry = SimpleClassTest.class.getName().replace('.', '/') + ".class";
      jos.putNextEntry(new JarEntry(entry));

      Class<?> testClass = Class.forName(SimpleClassTest.class.getName());
      writeClass(jos, testClass);
      Class<?> abstractClass = Class.forName(AbstractSimpleClassTest.class.getName());
      writeClass(jos, abstractClass);

      jos.closeEntry();
      jos.close();
    }
  }

  private static void writeClass(JarOutputStream jos, Class<?> clazz) throws IOException {
    final byte[] buf = new byte[128];
    try (InputStream classInputStream = clazz
        .getResourceAsStream(
            JarLoaderTest.class.getSimpleName() + "$" + clazz.getSimpleName() + ".class")) {
      int readLength = classInputStream.read(buf);
      while (readLength != -1) {
        jos.write(buf, 0, readLength);
        readLength = classInputStream.read(buf);
      }
    }
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    if (testDir.exists()) {
      String[] entries = testDir.list();
      for (String s : entries) {
        File currentFile = new File(testDir.getPath(), s);
        currentFile.delete();
      }
      testDir.delete();
    }
  }

  @Test
  public void testJarLoader() throws Exception {
    SimpleClassTest test = JarLoader
        .newInstance(testTaskModelJar,
            JarLoaderTest.class.getCanonicalName() + "$" + SimpleClassTest.class.getSimpleName(),
            getClass().getClassLoader(), SimpleClassTest.class);
    Assert.assertNotNull(test);
  }

  @Test
  public void testInvalidJarLoader() throws Exception {
    try {
      JarLoader.newInstance(testTaskModelJar,
          JarLoaderTest.class.getCanonicalName() + "$" + SimpleClassTest.class.getSimpleName(),
          getClass().getClassLoader(), String.class);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testAbstractJarLoader() throws Exception {
    try {
      JarLoader.newInstance(testTaskModelJar,
          JarLoaderTest.class.getCanonicalName() + "$" + AbstractSimpleClassTest.class
              .getSimpleName(),
          getClass().getClassLoader(), AbstractSimpleClassTest.class);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testInvalidParam() throws Exception {
    try {
      JarLoader.newInstance(null, "", null, null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      JarLoader.newInstance(testTaskModelJar, "", null, null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testConstructor() {
    Assert.assertNotNull(new JarLoader());
  }

  public static class SimpleClassTest {

    public SimpleClassTest() {

    }
  }

  public static abstract class AbstractSimpleClassTest {

    public AbstractSimpleClassTest() {

    }
  }
}