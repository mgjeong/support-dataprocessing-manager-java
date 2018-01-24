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

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.graph.JobGraphBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Launcher.class, StreamExecutionEnvironment.class, JobGraphBuilder.class})
public class LauncherTest {

  @Test
  public void test(){
    System.out.println("Nothing");
  }
  /*
  final static String TEST_DIR = System.getProperty("user.dir") + "/LauncherTestClass";
  final static String TEST_JAR = TEST_DIR + "/launcher_test_job.jar";
  final static String TEST_JSON = TEST_DIR + "/launcher_test_job.json";
  final static String JSON_CONTENT = "{\"workflowName\":\"launcher_test\"}";
  final static String[] ARGS_INVALID = {"--internal", TEST_JSON};
  final static String[] ARGS_FOR_EXTERNAL_CONFIG = {"--json", TEST_JSON};
  final static String[] ARGS_FOR_INTERNAL_CONFIG = {"--internal", "--json", TEST_JSON};

  @Test(expected = NullPointerException.class)
  public void testWhenImpossibleExecution() throws Exception {
    mockStatic(StreamExecutionEnvironment.class);
    PowerMockito.when(StreamExecutionEnvironment.class, "getExecutionEnvironment")
        .thenReturn(null);

    Launcher.main(ARGS_FOR_INTERNAL_CONFIG);
    Assert.fail("Failed: Illegal state; Null execution environment");
  }

  @Test(expected = RuntimeException.class)
  public void testWithoutAnyCommandLineArguments() throws Exception {
    Launcher.main(new String[]{});
    Assert.fail("Failed: Not any exception on a job without specifications");
  }

  @Test(expected = RuntimeException.class)
  public void testWithImproperArgs() throws Exception {
    Launcher.main(ARGS_INVALID);
    Assert.fail("Failed: Not any exception on a job with invalid specifications");
  }

  @BeforeClass
  public static void createTestFiles() throws Exception {
    File f = new File(TEST_JSON);
    if (f.exists()) {
      if (!f.delete()) {
        throw new RuntimeException("Cannot prepare test files");
      }
    } else {
      f.mkdirs();
      f.createNewFile();
    }

    JarFile testJar = new JarFile(f);
  }

  @After
  public void deleteTestFiles() {

  }

  @Test
  public void testWithExternalJsonFileNullInput() throws Exception {

  }

  @Test
  public void testWithExternalJsonFileInput() throws Exception {

  }

  @Test
  public void testWithInternalJsonFileNullInput() throws Exception {

  }

  @Test
  public void testWithInternalJsonFileInput() throws Exception {

  }

  @Test(expected = Exception.class)
  public void testWithInternalInput() throws Exception {
    Launcher.main(ARGS_FOR_INTERNAL_CONFIG);
  }


  @Test(expected = IllegalStateException.class)
  public void testExecute() throws Exception {
    mockStatic(JobGraphBuilder.class);
    JobGraphBuilder builder = PowerMockito.mock(JobGraphBuilder.class);
    whenNew(JobGraphBuilder.class).withAnyArguments().thenReturn(builder);

    JobGraph jobGraph = Mockito.mock(JobGraph.class);
    when(builder.getInstance(any(), any())).thenReturn(jobGraph);

    Mockito.doNothing().when(jobGraph).initialize();

    Launcher launcher = new Launcher();
    // Running with an empty job occurs IllegalStateException
    launcher.main(ARGS_FOR_EXTERNAL_CONFIG);
  }
*/
}
