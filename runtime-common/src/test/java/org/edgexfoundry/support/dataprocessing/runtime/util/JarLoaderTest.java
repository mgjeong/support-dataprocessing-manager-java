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
            SimpleClassTest.class);
    Assert.assertNotNull(test);
  }

  @Test
  public void testInvalidJarLoader() throws Exception {
    String obj = JarLoader.newInstance(testTaskModelJar,
        JarLoaderTest.class.getCanonicalName() + "$" + SimpleClassTest.class.getSimpleName(),
        String.class);
    Assert.assertNull(obj);
  }

  @Test
  public void testAbstractJarLoader() throws Exception {
    AbstractSimpleClassTest obj = JarLoader.newInstance(testTaskModelJar,
        JarLoaderTest.class.getCanonicalName() + "$" + AbstractSimpleClassTest.class
            .getSimpleName(),
        AbstractSimpleClassTest.class);
    Assert.assertNull(obj);
  }

  @Test
  public void testInvalidParam() throws Exception {
    try {
      JarLoader.newInstance(null, "", null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }

    try {
      JarLoader.newInstance(testTaskModelJar, "", null);
      Assert.fail("Should not reach here.");
    } catch (Exception e) {
      // success
    }
  }

  @Test
  public void testConstructor() {
    Assert.assertNotNull(new JarLoader());
  }

  @Test
  public void testParameterlessConstructor() throws Exception {
    ParameterClassTest clazz = JarLoader.newInstance(testTaskModelJar,
        JarLoaderTest.class.getCanonicalName() + "$" + ParameterClassTest.class.getSimpleName(),
        ParameterClassTest.class);
    Assert.assertNull(clazz);
  }

  public static class SimpleClassTest {

    public SimpleClassTest() {

    }
  }

  public static class ParameterClassTest {

    public ParameterClassTest(int sample) {

    }
  }

  public static abstract class AbstractSimpleClassTest {

    public AbstractSimpleClassTest() {

    }
  }
}