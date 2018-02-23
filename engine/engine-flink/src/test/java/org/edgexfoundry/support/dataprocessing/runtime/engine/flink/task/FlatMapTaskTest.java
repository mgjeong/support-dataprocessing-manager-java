package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.util.Collector;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.task.TestModel;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlatMapTaskTest {

  private static final String TEST_DIR = System.getProperty("user.dir")
      + "/FlatMapTaskTest";
  private static final String TEST_JAR = TEST_DIR + "/flatMapTaskTest.jar";
  private static final String TEST_CLASS = TestModel.class.getName();
  private static final String TEST_JAR_INVALID = TEST_DIR + "/flatMapTaskTestInvalid.jar";
  private static final String TEST_CLASS_INVALID = TEST_CLASS + "invalid";

  private static final String OUTER_KEY = "nestedOuterKey";
  private static final String INNER_KEY = "nestedInnerKey";
  private static final String NESTED_KEY = OUTER_KEY + "/" + INNER_KEY;
  private static final String INNER_VALUE = "nestedInnerValue";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private FlatMapTask flatMapTask;

  @BeforeClass
  public static void generateTestJar() throws Exception {
    File targetFile = new File(TEST_JAR);

    if (targetFile.exists()) {
      targetFile.delete();
    }
    targetFile.getParentFile().mkdirs();
    targetFile.createNewFile();

    final JarOutputStream jos = new JarOutputStream(new FileOutputStream(targetFile),
        new Manifest());
    final String entry = TEST_CLASS.replace('.', '/') + ".class";
    jos.putNextEntry(new JarEntry(entry));

    Class<?> testClass = Class.forName(TEST_CLASS);
    final byte[] buf = new byte[128];
    final InputStream classInputStream = testClass
        .getResourceAsStream(testClass.getSimpleName() + ".class");
    int readLength = classInputStream.read(buf);
    while (readLength != -1) {
      jos.write(buf, 0, readLength);
      readLength = classInputStream.read(buf);
    }

    classInputStream.close();
    jos.closeEntry();
    jos.close();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    File jarFile = new File(TEST_JAR);
    if (jarFile.exists()) {
      jarFile.getParentFile().deleteOnExit();
      jarFile.delete();
    }
  }

  private Map<String, Object> applyProperties(String jarFile, String className) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("jar", jarFile);
    properties.put("className", className);
    properties.put(NESTED_KEY, INNER_VALUE);

    flatMapTask = new FlatMapTask(properties);

    return properties;
  }

  private void setRuntimeContextForTest() {
    ClassLoader classLoader = this.getClass().getClassLoader();
    RuntimeContext runtimeContext = mock(RuntimeContext.class);
    when(runtimeContext.getUserCodeClassLoader()).thenReturn(classLoader);
    flatMapTask.setRuntimeContext(runtimeContext);
  }

  @Test
  public void testOpenAndTaskMap() throws Exception {
    applyProperties(TEST_JAR, TEST_CLASS);
    setRuntimeContextForTest();

    flatMapTask.open(null);

    Method m = FlatMapTask.class.getDeclaredMethod("makeTaskModelParam");
    m.setAccessible(true);

    Map<String, Object> nestedParams = (Map<String, Object>) m.invoke(flatMapTask);
    Map<String, Object> innerParam = (Map<String, Object>) nestedParams.get(OUTER_KEY);
    Assert.assertEquals(innerParam.get(INNER_KEY), INNER_VALUE);

    DataSet dataSet = DataSet.create();
    Collector<DataSet> collector = mock(Collector.class);
    flatMapTask.flatMap(dataSet, collector);
  }

  @Test
  public void testOpenInvalidJar() throws Exception {
    applyProperties(TEST_JAR_INVALID, TEST_CLASS);
    setRuntimeContextForTest();

    expectedException.expect(NullPointerException.class);
    flatMapTask.open(null);
  }

  @Test
  public void testOpenInvalidClass() throws Exception {
    applyProperties(TEST_JAR, TEST_CLASS_INVALID);
    setRuntimeContextForTest();

    expectedException.expect(ClassNotFoundException.class);
    flatMapTask.open(null);
  }

  @Test
  public void testOpenNullValue() throws Exception {
    applyProperties(null, null);
    setRuntimeContextForTest();

    expectedException.expect(NullPointerException.class);
    flatMapTask.open(null);
  }

  @Test
  public void testOpenWithNoArgs() throws Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put("className", TEST_CLASS);
    flatMapTask = new FlatMapTask(properties);

    setRuntimeContextForTest();

    expectedException.expect(IllegalStateException.class);
    flatMapTask.open(null);
  }
}
